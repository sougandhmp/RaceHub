---
name: android-code-review
description: >
  Perform thorough, architecture-aware code reviews for Android projects. Use this skill
  whenever the user asks to review Android code, check for bugs or issues, audit Kotlin
  code quality, inspect for memory leaks, evaluate architecture compliance, review Compose
  code, check coroutine/threading correctness, or assess code for production readiness.
  Trigger for any "review my code", "what's wrong with this", "is this correct", "check
  this ViewModel/Repository/Composable", or similar requests on Android code. Always use
  this skill when reviewing Kotlin or Android-specific code quality — don't just eyeball it.
---

# Android Code Review Skill

## Review Checklist

Run through all applicable categories below. Report issues by severity:
- 🔴 **Critical** — Bug, crash, data loss, security issue
- 🟠 **Major** — Memory leak, architectural violation, significant perf issue
- 🟡 **Minor** — Code smell, missed best practice, testability concern
- 🟢 **Suggestion** — Optional improvement, style, readability

---

## 1. Memory Leaks

```kotlin
// 🔴 LEAK: Holding Activity/Context in ViewModel
class BadViewModel : ViewModel() {
    private lateinit var context: Context  // ← NEVER do this
}

// ✅ Use applicationContext via Hilt or AndroidViewModel sparingly
class GoodViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel()

// 🔴 LEAK: Anonymous inner class in Fragment holding Fragment reference
class MyFragment : Fragment() {
    private val callback = object : SomeCallback {
        override fun onResult() { this@MyFragment.doSomething() } // ← Leaks fragment
    }
    // ✅ Use WeakReference or cancel in onDestroyView
}

// 🟠 Coroutine launched in wrong scope
class MyViewModel : ViewModel() {
    fun doWork() {
        GlobalScope.launch { /* 🔴 Not cancelled when VM cleared */ }
        viewModelScope.launch { /* ✅ Cancelled automatically */ }
    }
}
```

**Check for:**
- Context stored in ViewModel, Singleton, or static field
- Listeners/callbacks not removed in lifecycle callbacks
- `GlobalScope` usage
- Bitmaps not recycled (if not using Coil/Glide)
- `Handler` not removed in `onDestroy`

---

## 2. Lifecycle & Coroutines

```kotlin
// 🔴 Collecting flow in wrong scope (leaks, shows errors after back navigation)
class MyFragment : Fragment() {
    override fun onViewCreated(...) {
        lifecycleScope.launch {
            viewModel.uiState.collect { /* 🟠 Runs even when view is off-screen */ }
        }
        // ✅ Correct:
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { /* ✅ Only active when STARTED */ }
            }
        }
    }
}

// 🔴 Suspend function doing disk/network on Main thread
suspend fun loadData(): List<Item> {
    return database.getAll() // 🔴 No dispatcher — runs on caller's dispatcher
}
// ✅ Enforce dispatcher in repository/data layer
suspend fun loadData(): List<Item> = withContext(Dispatchers.IO) {
    database.getAll()
}
```

**Check for:**
- `lifecycleScope.launch` without `repeatOnLifecycle` for flows in Fragments
- Missing `Dispatchers.IO` in data layer
- `runBlocking` on main thread
- Not cancelling jobs that should be cancellable
- Exception handling: uncaught exceptions in `launch` (use `CoroutineExceptionHandler`)

---

## 3. Architecture Violations

```kotlin
// 🔴 ViewModel importing Android UI
import android.view.View  // ← Never in ViewModel
import androidx.compose.runtime.*  // ← Avoid in ViewModel

// 🟠 Repository importing ViewModel or UI classes
// domain/repository → should never see 'ViewModel', 'Fragment', 'Activity'

// 🔴 ViewModel directly calling Retrofit/Room (skipping Repository + UseCase)
class BadViewModel : ViewModel() {
    @Inject lateinit var api: UserApi  // ← ViewModel should use UseCases
    fun load() = viewModelScope.launch { api.getUsers() }
}

// 🟡 Exposing MutableStateFlow publicly
class BadViewModel : ViewModel() {
    val uiState = MutableStateFlow(UiState())  // 🟡 Should be private
}
class GoodViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()  // ✅ Read-only externally
}
```

**Check for:**
- Android/UI imports in domain layer
- Direct DB/API calls from ViewModel
- Mutable state exposed from ViewModel
- Navigation logic in ViewModel (should emit events, not navigate directly)
- Business logic in Composables or Fragments

---

## 4. Jetpack Compose Issues

```kotlin
// 🟠 Side effects without proper effect handlers
@Composable
fun BadScreen(viewModel: MyViewModel) {
    viewModel.loadData() // 🔴 Called on every recomposition!
}
// ✅ Use LaunchedEffect
@Composable
fun GoodScreen(viewModel: MyViewModel) {
    LaunchedEffect(Unit) { viewModel.loadData() }
}

// 🟡 State not hoisted
@Composable
fun BadInput() {
    var text by remember { mutableStateOf("") }  // 🟡 Hard to test/reuse
    TextField(value = text, onValueChange = { text = it })
}

// 🔴 Collecting flow without lifecycle awareness
@Composable
fun BadScreen(viewModel: MyViewModel) {
    val state by viewModel.uiState.collectAsState()  // 🟡 Not lifecycle-aware
    // ✅ Use collectAsStateWithLifecycle()
}

// 🟠 Forgot paddingValues in Scaffold
Scaffold { /* paddingValues ignored → content hidden under top bar */ }
// ✅
Scaffold { paddingValues ->
    Column(modifier = Modifier.padding(paddingValues)) { }
}
```

**Check for:**
- Function calls / data fetches directly in composable body (not in effect handlers)
- `collectAsState()` instead of `collectAsStateWithLifecycle()`
- Missing `key` in `LazyColumn` items
- `remember` without `key` when key should change recomposition
- Hardcoded colors/dimensions instead of theme tokens
- `paddingValues` from Scaffold not applied

---

## 5. Threading & Concurrency

```kotlin
// 🔴 Race condition: shared mutable state without synchronization
class BadRepository {
    private var cache = mutableListOf<Item>()  // 🔴 Not thread-safe
    suspend fun add(item: Item) { cache.add(item) }
}
// ✅ Use StateFlow, Mutex, or confine to single dispatcher
class GoodRepository {
    private val _cache = MutableStateFlow<List<Item>>(emptyList())
    private val mutex = Mutex()
    suspend fun add(item: Item) = mutex.withLock {
        _cache.update { it + item }
    }
}

// 🟡 Unnecessary thread switching
suspend fun getData() = withContext(Dispatchers.IO) {
    withContext(Dispatchers.Default) {  // 🟡 Double-switch, usually unnecessary
        processData()
    }
}
```

---

## 6. Security

- 🔴 API keys / secrets hardcoded in source code or `BuildConfig`  
  → Use `local.properties` + server-side secrets management
- 🔴 Sensitive data logged with `Log.d()`  
  → Strip logs in release (`isLoggable` check or Timber with release tree)
- 🟠 HTTP traffic allowed (`android:usesCleartextTraffic="true"`)  
  → HTTPS only; use Network Security Config for exceptions
- 🔴 `allowBackup="true"` without `fullBackupContent` exclusions  
  → Exclude sensitive files/databases from backup
- 🟠 SQL injection via raw query string concatenation in Room  
  → Always use parameterized queries (`:param` syntax in Room)
- 🟡 SharedPreferences for sensitive data  
  → Use `EncryptedSharedPreferences` (Jetpack Security)

---

## 7. Performance

```kotlin
// 🟠 Doing heavy work in Main thread
override fun onCreate(...) {
    val data = File("large.json").readText()  // 🔴 Blocking main thread
    val parsed = Json.decodeFromString<List<Item>>(data)  // 🔴 CPU on main thread
}

// 🟡 Unnecessary recompositions — unstable lambdas
@Composable
fun ParentComposable(viewModel: MyViewModel) {
    ChildComposable(onClick = { viewModel.doAction() })  // 🟡 New lambda every recomposition
    // ✅ Use remember { { viewModel.doAction() } } or pass stable reference
}

// 🟠 Loading full list when only paging needed
val allItems by viewModel.getAllItems().collectAsState(emptyList())  // 🟠 Could be thousands
// ✅ Use Paging 3 library for large datasets
```

---

## 8. Testing Concerns

- 🟡 ViewModel with hard-coded dependencies (not injected) → untestable
- 🟡 No interface/abstraction on Repository → can't mock in tests
- 🟡 `Dispatchers.Main` or `Dispatchers.IO` hardcoded in ViewModel → inject `CoroutineDispatcher`
- 🟡 Side effects directly in `init {}` → hard to control in tests

```kotlin
// ✅ Inject dispatcher for testability
@HiltViewModel
class MyViewModel @Inject constructor(
    private val useCase: GetDataUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel()
```

---

## Review Output Template

When reviewing code, structure your feedback as:

```
## Code Review Summary

**Overall**: [One-line verdict]

### 🔴 Critical Issues
1. [Issue] — [Why it's a problem] — [Fix]

### 🟠 Major Issues
1. ...

### 🟡 Minor Issues / Suggestions
1. ...

### ✅ What's Good
- [Acknowledge good patterns found]
```
