# RaceHub 🏁

RaceHub is a Formula 1 companion app for **Android** and **iOS**. It shows the race calendar, driver and constructor standings, and a circuit guide for each Grand Prix. It also has a community forum where fans can start threads, comment, and like posts.

The app is built with **Kotlin Multiplatform (KMP)**. All business logic, networking, persistence, and session handling live in one shared Kotlin module. Each platform has its own native UI: **Jetpack Compose** on Android and **SwiftUI** on iOS.

---

## Screenshots

### Android

|                        Race (light)                        |                           Race (dark)                           |                              Race detail                               |                            Full calendar                            |
|:----------------------------------------------------------:|:---------------------------------------------------------------:|:----------------------------------------------------------------------:|:-------------------------------------------------------------------:|
| <img src="docs/screenshots/android-race.png" width="200"/> | <img src="docs/screenshots/android-race-dark.png" width="200"/> | <img src="docs/screenshots/android-race-detail-dark.png" width="200"/> | <img src="docs/screenshots/android-schedule-dark.png" width="200"/> |

|                            Forum                            |                              Thread detail                               |                        Profile (light)                        |                           Profile (dark)                           |
|:-----------------------------------------------------------:|:------------------------------------------------------------------------:|:-------------------------------------------------------------:|:------------------------------------------------------------------:|
| <img src="docs/screenshots/android-forum.png" width="200"/> | <img src="docs/screenshots/android-thread-detail-dark.png" width="200"/> | <img src="docs/screenshots/android-profile.png" width="200"/> | <img src="docs/screenshots/android-profile-dark.png" width="200"/> |

<sub>Taken on the Android build (Pixel emulator).</sub>

### iOS

|                          Login                          |                          Sign up                         |
|:-------------------------------------------------------:|:--------------------------------------------------------:|
| <img src="docs/screenshots/ios-login.png" width="200"/> | <img src="docs/screenshots/ios-signup.png" width="200"/> |

|                      Race (light)                      |                         Race (dark)                         |                            Race detail                             |                          Full calendar                          |
|:------------------------------------------------------:|:-----------------------------------------------------------:|:------------------------------------------------------------------:|:---------------------------------------------------------------:|
| <img src="docs/screenshots/ios-race.png" width="200"/> | <img src="docs/screenshots/ios-race-dark.png" width="200"/> | <img src="docs/screenshots/ios-race-detail-dark.png" width="200"/> | <img src="docs/screenshots/ios-schedule-dark.png" width="200"/> |

|                          Forum                          |                            Thread detail                             |                      Profile (light)                      |                         Profile (dark)                         |
|:-------------------------------------------------------:|:--------------------------------------------------------------------:|:---------------------------------------------------------:|:--------------------------------------------------------------:|
| <img src="docs/screenshots/ios-forum.png" width="200"/> | <img src="docs/screenshots/ios-thread-detail-dark.png" width="200"/> | <img src="docs/screenshots/ios-profile.png" width="200"/> | <img src="docs/screenshots/ios-profile-dark.png" width="200"/> |

<sub>Taken on the SwiftUI build (iPhone 17 Pro simulator).</sub>

---

## Features

### Authentication
- **Sign in** with email and password. The session token is stored **encrypted at rest** (AES-256 `EncryptedSharedPreferences` on Android, the Keychain on iOS), so users stay signed in across launches.
- **Sign up** with username, email, password, and country (the country picker lists every ISO country).
- **Email verification by OTP.** After sign-up, a one-time code is emailed to the user, who verifies it in the app before they can sign in. Signing in to an unverified account sends a fresh code and opens the same screen. Codes can be resent.
- **Forgot password.** Request a reset OTP, then set a new password with it.
- **Sign out** always clears the session on the device, and revokes the token on the server when it can (so it works offline too).

### Race
- **Next race card** with the round, date, country flag, circuit outline, and the full weekend timetable (FP1, FP2, FP3, Qualifying, Race).
- **Standings** with a Drivers / Constructors toggle, team colour accents, and points.
- **Full calendar** for the season with each race's status, date, weather, and circuit map.
- **Race detail** with location, circuit, local start time, weather, a large circuit map, and track facts (laps, corners, distance, lap record).
- **Offline cache.** Races, standings, and trending threads are saved in a local SQLDelight database. If the network is down, the app shows the last data it cached.

### Forum
- Thread feed with **Latest**, **Most popular**, and **Most commented** filters.
- **Create threads** in categories such as General Discussion.
- **Thread detail** with the full post, likes, replies, and an inline reply box.

### Profile & settings
- Profile header with avatar initials, role badge, and stats (posts, saved, country).
- Account details (email, username, join date).
- Recent and saved posts.
- **Appearance**: choose **System**, **Dark**, or **Light** theme. The choice is kept across launches.

---

## Architecture

RaceHub follows **Clean Architecture**, with the **MVI** (Model-View-Intent) pattern in the presentation layer. Everything except the views is shared: each screen has **one Kotlin ViewModel** in `shared`, used by the Compose screen on Android and the SwiftUI view on iOS.

```
┌─────────────────────────────┐   ┌─────────────────────────────┐
│  composeApp (Android)       │   │  iosApp (iOS)               │
│  Jetpack Compose screens    │   │  SwiftUI views              │
│  koinViewModel()            │   │  SharedViewModelHost        │
└──────────────┬──────────────┘   └──────────────┬──────────────┘
               │   state / intents / effects     │  Shared.xcframework
               ▼                                 ▼  (KMP-NativeCoroutines)
┌──────────────────────────────────────────────────────────────┐
│  shared (Kotlin Multiplatform)                               │
│                                                              │
│  presentation ─ per screen: Contract (State, Intent, Effect, │
│                 Mutation), pure Reducer, ViewModel           │
│  domain       ─ models, business rules, repository           │
│                 interfaces, use cases, DataResult/DataError  │
│  data         ─ Ktor GraphQL client, DTOs + mappers,         │
│                 repository impls, SQLDelight cache,          │
│                 session storage (expect/actual)              │
│  di           ─ Koin modules; iOS entry points               │
└──────────────────────────────────────────────────────────────┘
```

**How MVI works here.** Every screen has a contract in `shared/.../<feature>/presentation`:

- `State`: an immutable snapshot the view renders as-is.
- `Intent`: a sealed set of user actions, sent with `viewModel.onIntent(...)`.
- `Effect`: one-off events such as navigation or an error snackbar, delivered on a separate `effects` flow so they are never replayed.
- `Mutation` + `Reducer`: the ViewModel turns intents and finished async work into internal mutations, and a pure reducer is the only place state changes. Reducers are tested with plain input/output assertions.

**Error handling.** Repositories are the error boundary: network, server and parsing failures come back as `DataResult.Failure(DataError.Network | Server | Unknown)`, and cached data is served when a refresh fails. The UI maps `DataError` to localized text. Auth use cases return `AuthResult` / `EmailVerificationResult` / `PasswordResetResult`, which carry the server's message.

**iOS bridging.** [KMP-NativeCoroutines](https://github.com/rickclephas/KMP-NativeCoroutines) exposes each ViewModel's `StateFlow` and effect `Flow` to Swift (`@NativeCoroutinesState` / `@NativeCoroutines`). `SharedViewModelHost` (in `iosApp/iosApp/Common`) republishes the state as `@Published`, exposes effects as a Combine subject, and clears the Kotlin ViewModel when the SwiftUI screen goes away. The Swift package version must match the Gradle plugin version.

**Platform-specific code** goes through `expect`/`actual`:

| Concern         | Android                                       | iOS                                      |
|-----------------|-----------------------------------------------|------------------------------------------|
| HTTP engine     | OkHttp                                        | Darwin (NSURLSession)                    |
| SQLite driver   | `AndroidSqliteDriver`                         | `NativeSqliteDriver`                     |
| Session storage | `EncryptedSharedPreferences`                  | Keychain                                 |
| DI bootstrap    | `KoinInitializer.setApplication()` + `init()` | `KoinInitializer.shared.start(baseUrl:)` |
| ViewModel owner | Navigation 3 back-stack entry (`koinViewModel()`) | `ViewModelOwner` via `SharedViewModels`  |

---

## Tech stack

| Area                 | Library                                                   |
|----------------------|-----------------------------------------------------------|
| Language             | Kotlin 2.4 (Multiplatform), Swift                         |
| Android UI           | Jetpack Compose, Material 3, Compose Resources            |
| iOS UI               | SwiftUI                                                   |
| Shared ViewModels    | AndroidX Lifecycle ViewModel (KMP), KMP-NativeCoroutines  |
| Networking           | Ktor client 3 + kotlinx.serialization (GraphQL over HTTP) |
| Persistence          | SQLDelight 2                                              |
| Dates                | kotlinx-datetime                                          |
| Dependency injection | Koin 4                                                    |
| Concurrency          | Kotlin Coroutines & Flow                                  |
| Secure storage       | AndroidX Security Crypto                                  |
| Testing / coverage   | kotlin-test, kotlinx-coroutines-test, Kover               |
| Build                | Gradle (Kotlin DSL), version catalog, AGP 9, KSP          |

Android `minSdk 24`, `targetSdk 37`. iOS deployment target 18.2, Apple Silicon simulators (the x64 Apple targets are not built).

---

## Project structure

```
RaceHub/
├── composeApp/                     # Android app: Compose screens only
│   ├── compose_stability.conf      # Marks shared models/state stable for Compose
│   └── src/androidMain/kotlin/org/gce/racehub/
│       ├── login/  signup/  emailverification/  forgotpassword/
│       ├── home/                   # Tab host, schedule, standings, create thread
│       ├── race/                   # Race tab, race detail, circuit drawings
│       ├── forum/                  # Forum feed, thread detail
│       ├── profile/
│       ├── theme/                  # AppColors, Dimens, ThemeManager
│       ├── ui/                     # Shared UI helpers (error messages)
│       ├── navigation/Routes.kt    # Navigation 3 destinations (serializable NavKeys)
│       └── App.kt                  # Theme + NavDisplay root
│
├── iosApp/                         # iOS app: SwiftUI views only
│   └── iosApp/
│       ├── Common/                 # SharedViewModelHost, AuthTextField, error messages
│       └── {Login,SignUp,EmailVerification,ForgotPassword,Home,Race,Forum,Profile,Theme}/
│
├── shared/                         # Kotlin Multiplatform module
│   └── src/
│       ├── commonMain/kotlin/org/gce/racehub/
│       │   ├── core/{domain,data}/        # DataResult/DataError; GraphQL types, safeCall
│       │   ├── auth/{domain,data,di,presentation}/  # Login, sign-up, OTP, password reset, session
│       │   ├── race/{domain,data,di,presentation}/  # Races, standings, weekend schedule
│       │   ├── forum/{domain,data,di,presentation}/    # Threads, comments, likes
│       │   ├── profile/{domain,data,di,presentation}/  # Signed-in user's profile
│       │   ├── home/presentation/         # Tab shell
│       │   ├── di/                        # Koin init + presentationModule
│       │   ├── db/                        # SQLDelight local data source
│       │   └── util/                      # Dispatchers, Logger
│       ├── commonMain/sqldelight/         # RaceHubDatabase.sq
│       ├── androidMain/ · iosMain/        # expect/actual + iOS entry points
│       └── commonTest/                    # Use case, reducer, ViewModel and mapper tests + fakes
│
└── docs/screenshots/
```

---

## Getting started

### Prerequisites
- **JDK 21** (Xcode's build phase uses `openjdk@21` from Homebrew)
- **Android Studio** (latest stable) with Android SDK 37
- **Xcode** 26+ for the iOS app (macOS only)

### Backend
The app talks to a GraphQL API. The base URL is set when Koin starts:

- Android: `composeApp/src/androidMain/kotlin/org/gce/racehub/RaceHubApplication.kt`
- iOS: `iosApp/iosApp/iOSApp.swift`

Change it there to point at your own backend.

### Run on Android
From the IDE, pick the `composeApp` run configuration. Or use the terminal:

```shell
./gradlew :composeApp:installDebug     # build and install on a connected device/emulator
```

### Run on iOS
Open `iosApp/iosApp.xcodeproj` in Xcode and run the `iosApp` scheme. A build phase runs `./gradlew :shared:embedAndSignAppleFrameworkForXcode`, which compiles the shared Kotlin code into the `Shared` framework for you.

---

## Testing

The shared module has unit tests for the use cases, the domain rules, every reducer and every shared ViewModel (intent handling, async ordering, effects), plus the DTO mappers and `UserSession`; those use fake repositories. The repositories themselves are tested against a fake API (Ktor `MockEngine`, `data/FakeApi.kt`) and a real in-memory SQLDelight database, covering error mapping and the cache-vs-network rules. Nothing needs a network or a device.

```shell
./gradlew :shared:testDebugUnitTest          # run shared tests on the JVM
./gradlew :shared:iosSimulatorArm64Test      # run the same tests on the iOS simulator
./gradlew :composeApp:testDebugUnitTest      # Android-only tests
./gradlew :shared:koverHtmlReport            # coverage report → shared/build/reports/kover/html
```

The Kover report leaves out HTTP client construction, generated SQLDelight code, DI wiring, and DTOs, so the coverage number reflects the domain, data and presentation logic.

---

## Adding a feature

The race feature (`shared/.../race/`) is the reference implementation.

1. **Domain**: add the model, any business rules, a repository method returning `DataResult`, and a use case in `shared/src/commonMain/.../<feature>/domain`.
2. **Data**: implement the repository method (DTO, mapper, network call via `safeCall`, cache) in `.../data`.
3. **Presentation**: in `.../<feature>/presentation`, add a `Contract` (State, Intent, Effect, internal Mutation), a pure `Reducer`, and a ViewModel. Annotate `state` with `@NativeCoroutinesState` and `effects` with `@NativeCoroutines`.
4. **DI**: register the use case in the feature's Koin module and the ViewModel in `presentationModule`; add a factory to `SharedViewModels` (iosMain) for iOS.
5. **UI**: Android: a Compose screen using `koinViewModel()`, and the state classes in `composeApp/compose_stability.conf`. iOS: a `SharedViewModelHost` factory in `SharedViewModelHost.swift` and a SwiftUI view that renders `state` and reacts to `effects`.
6. **Tests**: reducer tests and ViewModel tests in `shared/src/commonTest`, using the fakes in `fake/`.
