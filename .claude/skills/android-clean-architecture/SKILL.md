---
name: android-clean-architecture
description: >
  Scaffold and design Android projects using Clean Architecture, MVVM, or MVI patterns.
  Use this skill whenever the user asks to set up an Android project structure, create
  feature modules, define layers (data/domain/presentation), set up dependency injection
  with Hilt, design UseCases/Repositories/ViewModels, or apply architectural patterns.
  Also trigger for multi-module Android projects, architecture diagrams, package structure
  decisions, or any question about separation of concerns in Android. If the user mentions
  Clean Arch, MVVM, MVI, Hilt, Repository pattern, UseCase, or modularization — use this skill.
---

# Android Clean Architecture Skill

## Architecture Overview

```
app/
├── core/                    # Shared across features
│   ├── common/              # Extensions, utils, base classes
│   ├── ui/                  # Shared UI components, theme
│   └── network/             # Retrofit setup, interceptors
├── data/                    # Data layer (per feature or shared)
│   ├── remote/              # API service, DTOs
│   ├── local/               # Room DB, DAOs, entities
│   └── repository/          # Repository implementations
├── domain/                  # Pure Kotlin — NO Android deps
│   ├── model/               # Domain models
│   ├── repository/          # Repository interfaces
│   └── usecase/             # Business logic
└── presentation/            # UI layer
    ├── viewmodel/
    ├── screen/              # Composables or Fragments
    └── mapper/              # Domain → UI model mappers
```

---

## Layer Rules (Strict)

| Layer          | Depends On | Must NOT depend on                  |
|----------------|------------|-------------------------------------|
| `domain`       | Nothing    | `data`, `presentation`, Android SDK |
| `data`         | `domain`   | `presentation`                      |
| `presentation` | `domain`   | `data` directly                     |

> **Golden Rule**: Domain layer is pure Kotlin. Zero Android imports.

---

## Domain Layer

### Domain Model
```kotlin
// domain/model/User.kt
data class User(
    val id: String,
    val name: String,
    val email: String
)
```

### Repository Interface
```kotlin
// domain/repository/UserRepository.kt
interface UserRepository {
    fun getUsers(): Flow<List<User>>
    suspend fun getUserById(id: String): Result<User>
    suspend fun saveUser(user: User): Result<Unit>
}
```

### UseCase
```kotlin
// domain/usecase/GetUsersUseCase.kt
class GetUsersUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(): Flow<List<User>> = repository.getUsers()
}

// domain/usecase/GetUserByIdUseCase.kt
class GetUserByIdUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(id: String): Result<User> =
        repository.getUserById(id)
}
```

---

## Data Layer

### DTO & Mapper
```kotlin
// data/remote/dto/UserDto.kt
@Serializable
data class UserDto(
    @SerialName("user_id") val id: String,
    @SerialName("full_name") val name: String,
    val email: String
)

// data/remote/mapper/UserMapper.kt
fun UserDto.toDomain() = User(id = id, name = name, email = email)
fun User.toDto() = UserDto(id = id, name = name, email = email)
```

### Repository Implementation
```kotlin
// data/repository/UserRepositoryImpl.kt
class UserRepositoryImpl @Inject constructor(
    private val api: UserApi,
    private val dao: UserDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : UserRepository {

    override fun getUsers(): Flow<List<User>> =
        dao.getAllUsers().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getUserById(id: String): Result<User> =
        withContext(dispatcher) {
            runCatching { api.getUser(id).toDomain() }
        }
}
```

---

## Presentation Layer (MVVM)

### UiState + ViewModel
```kotlin
// presentation/viewmodel/UserViewModel.kt
data class UserUiState(
    val users: List<UserUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface UserUiEvent {
    data class ShowSnackbar(val message: String) : UserUiEvent
    data object NavigateToDetail : UserUiEvent
}

@HiltViewModel
class UserViewModel @Inject constructor(
    private val getUsersUseCase: GetUsersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<UserUiEvent>()
    val events = _events.receiveAsFlow()

    init { loadUsers() }

    private fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getUsersUseCase()
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { users ->
                    _uiState.update { it.copy(users = users.toUiModel(), isLoading = false) }
                }
        }
    }
}
```

---

## Dependency Injection (Hilt)

### Application Setup
```kotlin
// MyApp.kt
@HiltAndroidApp
class MyApp : Application()

// AndroidManifest.xml → android:name=".MyApp"
```

### Module Bindings
```kotlin
// di/RepositoryModule.kt
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}

// di/NetworkModule.kt
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi =
        retrofit.create(UserApi::class.java)
}
```

---

## Multi-Module Project Structure

```
root/
├── app/                    # Entry point, DI wiring, NavGraph
├── core/
│   ├── core-ui/            # Design system, shared composables
│   ├── core-network/       # Retrofit, interceptors
│   ├── core-database/      # Room setup
│   └── core-common/        # Extensions, utils, base classes
├── feature/
│   ├── feature-home/       # Self-contained feature module
│   │   ├── api/            # Public contract (exposed to app)
│   │   └── impl/           # Internal implementation
│   ├── feature-profile/
│   └── feature-settings/
└── build-logic/            # Convention plugins for Gradle
```

### Module dependencies rule:
- `feature` modules depend on `core` modules only
- `feature` modules NEVER depend on other `feature` modules
- `app` module wires everything together

---

## MVI Pattern (Alternative to MVVM)

```kotlin
// Unidirectional data flow
sealed interface UserIntent {
    data object LoadUsers : UserIntent
    data class SelectUser(val id: String) : UserIntent
    data object Retry : UserIntent
}

class UserMviViewModel @Inject constructor(
    private val getUsersUseCase: GetUsersUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(UserUiState())
    val state = _state.asStateFlow()

    fun processIntent(intent: UserIntent) {
        when (intent) {
            is UserIntent.LoadUsers -> loadUsers()
            is UserIntent.SelectUser -> selectUser(intent.id)
            is UserIntent.Retry -> loadUsers()
        }
    }
}
```

---

## Common Pitfalls
| Pitfall                                  | Fix                                                 |
|------------------------------------------|-----------------------------------------------------|
| Android imports in domain layer          | Domain = pure Kotlin only                           |
| ViewModel directly calling repository    | Always go through UseCases                          |
| Exposing MutableStateFlow from ViewModel | Use `asStateFlow()` before exposing                 |
| Feature modules depending on each other  | Route through `app` module or shared `core`         |
| Missing `@Singleton` on repository       | Leads to multiple instances — always scope properly |
