/**
 * Example: Authentication Layer Integration
 *
 * All dependencies are wired via Koin. ViewModels use `by inject()` — you
 * never instantiate repositories or use cases directly in application code.
 */

// ============================================================================
// Option 1: Development / Testing (fake repository, no network)
// ============================================================================

// In RaceHubApplication.onCreate() — swap to fake DI for UI development:
//
//   KoinInitializer.initForTesting()  // uses AuthRepositoryImpl (hard-coded creds)
//
// Test credentials: driver@racehub.com / race123

// ============================================================================
// Option 2: Production (network repository)
// ============================================================================

// In RaceHubApplication.onCreate():
//
//   KoinInitializer.setApplication(this)
//   KoinInitializer.init(baseUrl = "https://api.racehub.com")

// ============================================================================
// Option 3: ViewModel integration (Kotlin/Android)
// ============================================================================

/*
class LoginViewModel : ViewModel(), KoinComponent {

    private val loginUseCase: LoginUseCase by inject()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val result = loginUseCase.execute(email, password)
            if (result.isSuccess) {
                // navigate to home
            } else {
                // show result.error
            }
        }
    }
}
*/

// ============================================================================
// Option 4: iOS/Swift integration (via KMP export)
// ============================================================================

/*
 import Shared

 class iOSLoginViewModel: ObservableObject {
     @Published var isLoading = false
     @Published var errorMessage: String?

     func login(email: String, password: String) {
         isLoading = true
         // Call the shared use case directly; Koin is initialised in iOSApp.swift
         let useCase = AuthDependencyProvider().loginUseCase
         // ...
     }
 }
 */
