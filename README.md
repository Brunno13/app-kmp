# 📱 KMP App (Android & iOS)

A cross-platform mobile application (Android and iOS) built with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**. The project adopts the **MVVM (Model-View-ViewModel)** architecture and an **Offline-First** approach, ensuring smooth navigation without internet access and robust application state management.

### 🚀 Key Features
* **Authentication & Network:** Consumption of the `better-auth` API using **Ktorfit** (Retrofit-like syntax) for typed HTTP calls.
* **Architecture (MVVM):** Clear separation of concerns using the official Jetpack ViewModel pattern for KMP and `StateFlow` for reactivity.
* **Offline Persistence:** Reactive local database using **Room Multiplatform** as the Single Source of Truth.
* **Security:** Encrypted native storage for session tokens (`EncryptedSharedPreferences` / `Keychain`) using **Multiplatform Settings**.
* **Global Localization & UI:** Native i18n support (English/Portuguese) using **Compose Resources**, zero "magic strings", and centralized navigation constants.
* **Robust Error Handling:** Strongly typed, domain-driven error management (`AppResult`/`AppError`) mapping network and authentication failures directly to localized UI states.
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
- [x] **Flavors:** Environment variable configuration (Staging/Production) directly in Gradle Build Variants.
- [x] **Authentication UI:** Development of login screens, active session management, and error mapping.
- [x] **State Management:** ViewModel construction based on `StateFlow`.
- [x] **Secure Storage:** Keychain (iOS) and EncryptedSharedPreferences (Android) configuration for authentication tokens.
- [x] **Global Localization & Error Handling:** Implementation of Compose Resources for i18n (En/Pt), centralized navigation constants (`Routes`), and strongly typed error mapping (`AppError`) from Domain to UI.
- [x] **Design System:** Creation of reusable visual components using Compose, including global theming (Colors, Shapes, Dimens) and shared widgets (`AppButton`, `AppTextField`, `MenuCard`).
- [x] **User Preferences & Theming:** Global dynamic theme engine (Light/Dark/Auto) linked to multiplatform `Settings` and State hoisting at the app root level.
- [x] **CI/CD Automation:** Woodpecker CI pipelines (`release` and `test`) running via local macOS agent, powered by Bash scripts mapping Gradle tasks for APK generation and Kotlin Native iOS compilation.

### ⏳ Next Steps
- [ ] **Native Integration:** Biometrics implementation via `expect/actual` (Face ID / Touch ID).
- [ ] **E2E Testing:** Creation of interactive test flows using Maestro scripts.

---

### 🛠️ Tech Stack
* **Framework:** Kotlin Multiplatform (KMP) + Compose Multiplatform
* **Language:** Kotlin
* **Dependency & Build Management:** Gradle (Version Catalogs `.toml` + KSP)
* **Navigation:** Jetpack Navigation Compose Multiplatform
* **Network & API:** Ktorfit + Ktor Client (with Kotlinx Serialization)
* **Database & Offline-First:** Room Multiplatform + Bundled SQLite
* **Global State & Lifecycle:** Jetpack ViewModel + StateFlow (Kotlin Coroutines)
* **Dependency Injection:** Koin
* **Security & Biometrics:** Multiplatform Settings (EncryptedSharedPreferences/Keychain) + Native APIs via `expect/actual`
* **Forms & Validation:** StateFlow + Konform (Native validation)
* **Resilience & Internationalization:** Compose Multiplatform Resources
* **Automated Testing:** Kotest (Unit/Integration) + Maestro (Multiplatform E2E)
* **UI Documentation:** Compose `@Preview` (Isolated on Android) / Showkase
* **Architecture & Quality:** MVVM (Model-View-ViewModel) + Single Source of Truth
* **CI/CD:** Woodpecker CI (Cross-builds via `./gradlew` and `xcodebuild`)

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
        │   └── kotlin/.../di/PlatformModule.kt # Android Room Builder & EncryptedSharedPreferences
        │
        ├── iosMain/      # iOS-specific expected implementations (expect/actual)
        │   └── kotlin/.../di/PlatformModule.kt # iOS Room Builder & Keychain Settings
        │
        └── commonMain/   # Shared Kotlin codebase
            ├── composeResources/ # Global Localization (i18n XMLs) & Shared Assets
            │
            └── kotlin/com/brunno/appkmp/
                ├── App.kt             # Compose Multiplatform Entry Point (NavHost)
                │
                ├── di/                # Dependency Injection Setup (Koin)
                │   ├── KoinModule.kt      # Core module injection (ViewModels, Repositories, DB)
                │   ├── NetworkModule.kt   # Ktorfit & Ktor HTTP client injection
                │   └── PlatformModule.kt  # Expect/Actual definitions for native dependencies
                │
                ├── presentation/      # UI Layer & Presentation Logic
                │   ├── theme/         # Design System (Theme.kt, Color.kt, Shape.kt, Dimens.kt)
                │   ├── components/    # Reusable UI Widgets (AppButton, AppTextField, MenuCard, etc)
                │   ├── navigation/    # Centralized routing constants (Routes.kt)
                │   ├── screens/       # Compose Multiplatform Screens (Home, Login, Profile, etc)
                │   ├── viewmodels/    # Jetpack ViewModels managing UI State (StateFlow)
                │   └── utils/         # UI Helpers (e.g., ErrorMapper.kt for string mapping)
                │
                ├── domain/            # Core Business Rules (Independent of Frameworks)
                │   ├── error/         # Strongly typed error handling (AppError, AppResult)
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
                    │   └── models/        # DTOs for JSON Serialization
                    │
                    └── repository/    # Hybrid Data Orchestration
                        └── AuthRepositoryImpl.kt # Ktorfit + Room + Secure Storage integration
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

---

## 🚀 How to Run and Build Locally

### Prerequisites
* **Java Development Kit (JDK 17+)** installed.
* **For Android:** Android Studio configured with a Virtual Device (Emulator) or a physical Android device connected.
* **For iOS (macOS only):** Xcode installed, Command Line Tools configured, and the iOS Simulator active.

### 💻 Development Mode (Live Reload & Debugging)

Since this is a Kotlin Multiplatform project, the best development experience is achieved using **Android Studio** (with the KMP plugin) or **JetBrains Fleet**.

1. **Android (Staging/Production):**
    * Open the project in Android Studio.
    * Select the `composeApp` run configuration.
    * Choose your active Build Variant (e.g., `stagingDebug` or `productionDebug`).
    * Click **Run** (Shift + F10) to deploy to the Emulator.
    * *Alternatively, via CLI:*
   
      ```bash
      ./gradlew :composeApp:installStagingDebug
      ```

2. **iOS:**
    * Open the project in Android Studio and select the `iosApp` run configuration.
    * *Alternatively, via Xcode:* Open `iosApp/iosApp.xcodeproj` in Xcode, select your target simulator, and click **Run** (Cmd + R).

### 📦 Generating Binaries Locally
Automated bash scripts detect the environment and configure local build tasks via Gradle. Ensure your scripts have execution permissions (`chmod +x scripts/*.sh`).

#### 🤖 Android (APK Generation via Gradle)
The script automatically selects the correct Gradle product flavor (`staging` or `production`) based on the environment variable.

* **Generate Staging APK (Allows local HTTP APIs):**

    ```bash
    APP_ENV=staging ./scripts/build_android.sh
    ```

* **Generate Production APK (Requires secure HTTPS):**

    ```bash
    APP_ENV=production ./scripts/build_android.sh
    ```
  
* **Result:** Files will be exported to the project root as `app-kmp-staging.apk` or `app-kmp-production.apk`.

#### 🍎 iOS (Framework Generation via Kotlin Native)
*Requires macOS*. The script runs the Kotlin Native compiler to link the Release Framework.

* **Generate Staging iOS package:**

    ```bash
    APP_ENV=staging ./scripts/build_ios.sh
    ```

* **Generate Production iOS package:**

    ```bash
    APP_ENV=production ./scripts/build_ios.sh
    ```
  
* **Result:** The compiled framework will be compressed and exported to the root as `app-kmp-ios-staging.zip` or `app-kmp-ios-production.zip`.

### 🧪 Testing & Linting

#### **Code Quality (Lint)**
Run the linter to verify architecture and code formatting rules:


    ./scripts/lint.sh


#### **Unit & Integration Tests**
Run the fast unit and integration tests across the shared Kotlin codebase:


    ./scripts/test.sh


---

## 📦 CI/CD, Automation and Mirroring

The Woodpecker CI pipeline (`.woodpecker/release.yml`) automates APK/Framework generation, artifact packaging, and repository mirroring.

**Release Pipeline Workflow:**
1. **Trigger:** Creation of a Tag (e.g., `v1.0.5`) on Gitea starts the production pipeline.
2. **Native Agent:** Execution occurs on a macOS agent (`darwin/arm64`) to enable Apple Silicon processing and iOS compilation.
3. **Dual Compilation:**
    * Injection of the `APP_ENV=production` variable.
    * **Android:** Executes `./scripts/build_android.sh` to package the Release APK.
    * **iOS:** Executes `./scripts/build_ios.sh` to compile the Kotlin Native framework.
4. **Internal Release:** Woodpecker generates the git changelog, creates a versioned Release on the internal Gitea server, and attaches the compiled artifacts (`.apk` and `.zip`).
5. **Mirroring (GitHub):** The source code associated with the Tag is forcibly pushed to GitHub, where an identical public Release is created via API, attaching the same binaries.

**Staging Pipeline Workflow (`.woodpecker/test.yml`):**
* Runs automatically on every push to the `main` branch.
* Executes Linter and Unit Tests.
* Ensures code health before any manual merges or production tags.

---

## 🔐 Security and Local Traffic

* **In Staging (`.staging` flavor):** The app allows cleartext HTTP traffic to enable communication with local development servers and staging APIs without SSL certificates.
* **In Production:** Builds enforce secure HTTPS connections for all remote communications.

---
