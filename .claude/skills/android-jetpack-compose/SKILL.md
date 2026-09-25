---
name: android-jetpack-compose
description: >
  Expert guidance for building Jetpack Compose UIs in Android. Use this skill whenever
  the user asks to build Compose screens, components, layouts, navigation, theming,
  animations, or any Compose-based UI. Also trigger for questions about state management
  in Compose, recomposition issues, previews, accessibility, Material3 design, or
  migrating from XML to Compose. If the user mentions composables, LazyColumn, Scaffold,
  ViewModel + Compose, or any Jetpack Compose concept — use this skill.
---

# Android Jetpack Compose UI Skill

## Environment & Versions
- **Compose BOM**: `2024.06.00` (use BOM to keep all Compose libs in sync)
- **Material3** only — never use Material2 (`androidx.compose.material`)
- **Kotlin**: 1.9+, use coroutines and flows freely
- **minSdk**: 24+ recommended for full Compose support
- **compileSdk**: 34+

## Gradle Setup (if needed)
```kotlin
// build.gradle.kts (app)
android {
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
```

---

## Core Principles

### 1. State Hoisting — Always
- Composables should be **stateless** where possible
- Hoist state to the nearest common ancestor or ViewModel
- Pass state down, events up (lambda callbacks)

```kotlin
// ✅ Good — stateless, testable
@Composable
fun CounterButton(count: Int, onIncrement: () -> Unit) {
    Button(onClick = onIncrement) { Text("Count: $count") }
}

// ❌ Bad — state trapped inside
@Composable
fun CounterButton() {
    var count by remember { mutableStateOf(0) }
    Button(onClick = { count++ }) { Text("Count: $count") }
}
```

### 2. ViewModel Integration
```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MyScreenContent(state = uiState, onAction = viewModel::handleAction)
}

// Sealed class for UI state
sealed interface MyUiState {
    data object Loading : MyUiState
    data class Success(val data: List<Item>) : MyUiState
    data class Error(val message: String) : MyUiState
}
```

### 3. `remember` and `derivedStateOf`
```kotlin
// Use derivedStateOf to avoid unnecessary recompositions
val isButtonEnabled by remember {
    derivedStateOf { email.isNotEmpty() && password.length >= 8 }
}

// rememberSaveable for state that survives config changes
var inputText by rememberSaveable { mutableStateOf("") }
```

---

## Common Patterns

### Screen Structure (Scaffold)
```kotlin
@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Home") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigate("detail") }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        // Always consume paddingValues!
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            items(itemsList, key = { it.id }) { item ->
                ItemRow(item = item)
            }
        }
    }
}
```

### Navigation (NavHost)
```kotlin
@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController, startDestination = "home") {
        composable("home") {
            HomeScreen(onNavigate = { navController.navigate("detail/$it") })
        }
        composable("detail/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: return@composable
            DetailScreen(id = id, onBack = { navController.popBackStack() })
        }
    }
}
```

### Side Effects
```kotlin
// LaunchedEffect — run coroutine tied to key
LaunchedEffect(userId) {
    viewModel.loadUser(userId)
}

// SideEffect — sync with non-Compose world on every recomposition
SideEffect { analytics.setScreen("HomeScreen") }

// DisposableEffect — cleanup on leave
DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event -> /* handle */ }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
}
```

---

## Theming (Material3)
```kotlin
// Theme.kt
@Composable
fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) darkColorScheme(...) else lightColorScheme(...)
    MaterialTheme(colorScheme = colorScheme, typography = AppTypography, content = content)
}

// Use theme tokens, never hardcode colors
Text(text = "Hello", color = MaterialTheme.colorScheme.primary)
Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface))
```

---

## Previews
```kotlin
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "Dark")
@Preview(showBackground = true, name = "Light")
@Composable
fun MyComponentPreview() {
    AppTheme { MyComponent(data = previewData) }
}
```

---

## Performance Tips
- Use `key` in `LazyColumn`/`LazyRow` items to stabilize recompositions
- Mark data classes `@Stable` or `@Immutable` if Compose can't infer stability
- Avoid reading State inside lambdas passed to layout modifiers — use `Modifier.graphicsLayer { }` or `Modifier.drawBehind { }` for animations
- Profile with **Layout Inspector** and **Composition Tracing** in Android Studio

---

## Common Pitfalls
| Pitfall                                     | Fix                                                            |
|---------------------------------------------|----------------------------------------------------------------|
| Forgot `padding(paddingValues)` in Scaffold | Always apply `paddingValues` from Scaffold content lambda      |
| State mutation outside `remember`           | Wrap in `remember { mutableStateOf(...) }`                     |
| `collectAsState()` not lifecycle-aware      | Use `collectAsStateWithLifecycle()`                            |
| Multiple recompositions on list             | Add `key = { item.id }` to `items()`                           |
| Hardcoded colors/sizes                      | Use `MaterialTheme.colorScheme` and `MaterialTheme.typography` |
| Reading ViewModel state directly in lambda  | Capture state in a val first                                   |
