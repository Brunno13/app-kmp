# 📱 KMP App (Android & iOS)

A cross-platform mobile application (Android and iOS) built with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**. The project adopts the **MVVM (Model-View-ViewModel)** architecture and an **Offline-First** approach, ensuring smooth navigation without internet access and robust application state management.

### 🚀 Key Features
* **Authentication & Network:** Consumption of the `better-auth` API using **Ktorfit** (Retrofit-like syntax) for typed HTTP calls.
* **Architecture (MVVM):** Clear separation of concerns using the official Jetpack ViewModel pattern for KMP and `StateFlow` for reactivity.
* **Offline Persistence:** Reactive local database using **Room Multiplatform** as the Single Source of Truth.
* **Security:** Encrypted native storage for session tokens using `Multiplatform Settings`.
* **Automated Testing:** Unit tests for native business rules and E2E visual flow automation with `Maestro`.
* **Infrastructure & CI/CD:** Isolated environments (Staging and Production) with automated cross-builds via Woodpecker CI.

---

## 🗺️ Project Roadmap

### ✅ Completed
- [x] **Stack Definition:** Transition from React Native to the Kotlin Multiplatform (KMP) ecosystem.
- [x] **Tool Mapping:** Adoption of Room (Database), Ktorfit (Network), and Koin (Dependency Injection).
- [x] **Architecture Design:** Establishment of the MVVM pattern to replace FSD.
- [x] **Initial Setup:** Base project initialization using KMP Wizard with Compose Multiplatform.
- [x] **Dependency Management:** Configuration of `libs.versions.toml` with core libraries.
- [x] **Dependency Injection:** Koin setup in `commonMain` to manage Repositories and ViewModels.
- [x] **Navigation:** Implementation of the navigation engine for Tabs and Stacks.
- [x] **Local Persistence:** Room Multiplatform database setup and entity schemas.
- [x] **Network Layer:** Ktorfit configuration and API route mapping (`/sign-in`, etc).
- [x] **Hybrid Repositories:** Priority logic implementation (fetch locally from Room or fetch from Ktorfit and save locally).

### ⏳ Next Steps
- [ ] **Secure Storage:** Keychain (iOS) and EncryptedSharedPreferences (Android) configuration for authentication tokens.
- [ ] **Design System:** Creation of reusable visual components using Compose.
- [ ] **State Management:** ViewModel construction based on `StateFlow`.
- [ ] **Authentication UI:** Development of login screens and error mapping.
- [ ] **Native Integration:** Biometrics implementation via `expect/actual` (Face ID / Touch ID).
- [ ] **CI/CD Migration:** Adaptation of Woodpecker CI routines to run `./gradlew assemble` and `xcodebuild`.
- [ ] **E2E Testing:** Creation of interactive test flows using Maestro scripts.
- [ ] **Flavors:** Environment variable configuration (Staging/Production) directly in Gradle Build Variants.

---

### 🛠️ Tech Stack
* **Framework:** Kotlin Multiplatform (KMP) + Compose Multiplatform
* **Language:** Kotlin
* **Dependency & Build Management:** Gradle (Version Catalogs `.toml` + KSP)
* **Navigation:** Jetpack Navigation Compose Multiplatform
* **Network & API:** Ktorfit + Ktor Client (com Kotlinx Serialization)
* **Database & Offline-First:** Room Multiplatform + Bundled SQLite
* **Global State & Lifecycle:** Jetpack ViewModel + StateFlow (Kotlin Coroutines)
* **Dependency Injection:** Koin
* **Security & Biometrics:** Multiplatform Settings (EncryptedSharedPreferences/Keychain) + Native APIs via `expect/actual`
* **Forms & Validation:** StateFlow + Konform (Validação nativa)
* **Resilience & Internationalization:** Compose Multiplatform Resources
* **Automated Testing:** Kotest (Unit/Integration) + Maestro (Multiplatform E2E)
* **UI Documentation:** Compose `@Preview` (Isolado no Android) / Showkase
* **Architecture & Quality:** MVVM (Model-View-ViewModel) + Single Source of Truth
* **CI/CD:** Woodpecker CI (Builds cruzados via `./gradlew` e `xcodebuild`)

---

## 🏗️ Project Architecture

The project strictly follows the **MVVM (Model-View-ViewModel)** pattern adapted for **Kotlin Multiplatform (KMP)**. It focuses on a *Single Source of Truth* (Offline-First) and maximizes code sharing across platforms. The structure is organized into native entry points and a robust shared module containing UI, business logic, and data access.

The main folder structure is as follows:

```text
/ (Project Root)
├── androidApp/           # Native Android environment and entry points
│   └── src/main/
│       ├── AndroidManifest.xml # Android settings and entry point registration
│       └── java/.../
│           ├── AppApplication.kt # Global application class (Koin Android Context init)
│           └── MainActivity.kt   # Android host activity and isolated Compose @Previews
│
├── iosApp/               # Native iOS environment and entry points (Xcode workspace)
│   └── iosApp/
│       └── iOSApp.swift  # iOS lifecycle and Koin initialization
│
└── shared/               # Core KMP module (100% shared business logic and UI)
    └── src/
        ├── androidMain/  # Android-specific expected implementations (expect/actual)
        │   └── kotlin/.../di/PlatformModule.android.kt # Android Room Database Builder
        │
        ├── iosMain/      # iOS-specific expected implementations (expect/actual)
        │   └── kotlin/.../di/PlatformModule.ios.kt     # iOS Room Database Builder (NSDocumentDirectory)
        │
        └── commonMain/   # Shared Kotlin codebase (UI, ViewModels, Repositories, Database)
            └── kotlin/com/brunno/appkmp/
                ├── App.kt             # Compose Multiplatform Entry Point & Jetpack Navigation (NavHost)
                │
                ├── di/                # Dependency Injection Setup (Koin)
                │   ├── KoinModule.kt      # Core module injection (ViewModels, Repositories, DB)
                │   ├── NetworkModule.kt   # Ktorfit & Ktor HTTP client injection
                │   └── PlatformModule.kt  # Expect/Actual definitions for native dependencies
                │
                ├── presentation/      # UI Layer & Presentation Logic
                │   ├── viewmodels/    # Jetpack ViewModels managing UI State (StateFlow)
                │   └── screens/       # Compose Multiplatform Screens (Home, Login, Profile)
                │
                ├── domain/            # Core Business Rules (Independent of Frameworks)
                │   ├── models/        # Pure Kotlin Data Classes (Domain representations)
                │   └── repository/    # Repository Interfaces (AuthRepository.kt)
                │
                └── data/              # Data Layer (Network, Local Storage, Repositories)
                    ├── local/         # Offline-First Engine (Room Multiplatform)
                    │   ├── AppDatabase.kt # SQLite Bundled Configuration
                    │   ├── UserDao.kt     # Data Access Objects (SQL Queries)
                    │   └── UserEntity.kt  # Room Table Schemas
                    │
                    ├── remote/        # Network Engine (Ktorfit)
                    │   ├── AuthApi.kt     # API Interfaces (@POST, @GET)
                    │   └── models/        # Data Transfer Objects (DTOs) for JSON Serialization
                    │
                    └── repository/    # Hybrid Data Orchestration (Single Source of Truth)
                        └── AuthRepositoryImpl.kt # Fetches from Ktorfit, saves to Room, serves to Domain
```
### 📏 Architecture Guidelines

To maintain clean, scalable, and loosely coupled code across platforms, we follow three basic rules based on MVVM and Clean Architecture:

**1. Dependency Flow (Clean Architecture)**
* **`presentation`** ➔ Can import from `domain`. (UI and ViewModels only interact with business rules and interfaces).
* **`data`** ➔ Can import from `domain`. (Implements repository interfaces and maps remote/local data to pure models).
* **`domain`** ➔ 100% Pure Kotlin. Must never import from `presentation` or `data` (Completely agnostic of UI, databases, or HTTP clients).

**2. Layer Encapsulation (Inversion of Control)**
* It is **strictly prohibited** for the UI or ViewModels to directly instantiate data sources, network clients, or repository implementations. Everything must be injected through Domain Interfaces using our DI tree (`Koin`).
* ❌ *Wrong:* `val repository = AuthRepositoryImpl(api, dao)`
* ✅ *Correct:* `val repository: AuthRepository = get()` (Injected transparently)

**3. Smart Hybrid Persistence (Single Source of Truth)**
* **Cache and Offline Mode:** The UI strictly observes the local database (`Room Multiplatform` + `Bundled SQLite`). The network (`Ktorfit`) updates the database in the background, and the database reactively updates the UI via `StateFlow`.
* **Security:** Session tokens and sensitive keys are never saved in plain text or standard DBs. They are stored using device-native encryption (`Keychain` on iOS, `EncryptedSharedPreferences` on Android) abstracted via `expect/actual` or Multiplatform Settings.

