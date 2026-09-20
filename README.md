# 📱 App-KMP

A reusable **Kotlin Multiplatform (KMP)** base project for Android and iOS, built with **Compose Multiplatform**.

The goal of this repository is to provide a solid starting point for mobile applications, with shared UI, authentication, networking, persistence, security, testing, and CI/CD already in place.

The project uses **[api-bun](https://github.com/Brunno13/api-bun)** as its backend for authentication and application APIs.

---

## ✨ Features

- Shared UI for Android and iOS with Compose Multiplatform
- Login, registration and password recovery
- User profile, profile editing and avatar
- Biometric authentication
- Active session management and session revocation
- Secure credential storage
- Room Multiplatform persistence
- Ktor + Ktorfit networking
- Shared navigation and ViewModels
- Automated tests, coverage and static analysis
- Self-hosted Android and iOS CI/CD

---

## 🛠️ Tech Stack

| Area | Technology |
| --- | --- |
| Multiplatform | Kotlin Multiplatform |
| UI | Compose Multiplatform |
| Architecture | Presentation / Domain / Data |
| State | ViewModel + StateFlow |
| Navigation | Navigation Compose |
| Dependency Injection | Koin |
| Networking | Ktor Client |
| Typed API | Ktorfit |
| Serialization | kotlinx.serialization |
| Database | Room KMP / SQLite |
| Settings | Multiplatform Settings |
| Android Security | Android Keystore + AES-GCM |
| iOS Security | Keychain |
| Tests | kotlin.test / kotlinx.coroutines test |
| Coverage | JaCoCo |
| Static Analysis | Detekt |
| Duplication | PMD CPD |
| Android Analysis | Android Lint |
| CI/CD | Woodpecker CI |

### Current baseline

- Gradle `9.5.1`
- AGP `9.3.1`
- Kotlin / KMP `2.4.20`
- Compose Multiplatform `1.12.0`
- `compileSdk 37`
- `targetSdk 36`
- `minSdk 24`
- Room `2.8.5`
- SQLite `2.7.1`
- Koin `4.2.2`
- Ktor `3.5.2`
- Ktorfit `2.7.5`
- Navigation Compose `2.9.2`
- Lifecycle `2.11.0`
- kotlinx.serialization `1.11.0`
- Multiplatform Settings `1.3.0`

---

## 🏗️ Project Architecture

The project follows a layered structure to keep UI, business rules and infrastructure separated.

```text
Presentation
├── Screens
├── Components
├── Navigation
└── ViewModels
        │
        ▼
Domain
├── Models
├── Errors
└── Repository contracts
        │
        ▼
Data
├── Repository implementations
├── Remote / Ktorfit
├── Local / Room
└── Secure credential storage
```

Platform-specific code is isolated from shared code:

```text
shared/src/
├── commonMain/      # Shared UI, domain, data and application logic
├── androidMain/     # Android-specific integrations
├── iosMain/         # iOS-specific integrations
└── commonTest/      # Shared tests
```

The main application entry points remain platform-specific:

```text
app-kmp/
├── androidApp/
├── iosApp/
├── shared/
├── scripts/
└── .woodpecker/
```

### Backend

```text
app-kmp
   │
   │ Ktor / Ktorfit
   ▼
api-bun
```

Backend repository: **[Brunno13/api-bun](https://github.com/Brunno13/api-bun)**.

---

## 🔐 Security

Authentication credentials are intentionally kept outside Room entities and UI models.

### Android

Sensitive values are stored using:

```text
Android Keystore
      │
      ▼
AES-256-GCM
      │
      ▼
Encrypted payloads in SharedPreferences
```

### iOS

Sensitive credentials use the platform-specific secure storage implementation backed by **Keychain**.

### Sessions

Room stores session metadata only.

Authentication/session tokens are not stored in:

- Room entities
- presentation models
- logs
- user-facing error messages

Invalid or expired sessions trigger cleanup of local credentials, cookies, cached user state and session metadata.

---

## 🚀 Running Locally

### Prerequisites

For Android development:

- JDK 17
- Android Studio
- Android SDK compatible with the project

For iOS development:

- macOS
- Xcode
- Kotlin/Native toolchain

The application also requires access to the **[api-bun](https://github.com/Brunno13/api-bun)** backend.

### 1. Clone the project

```bash
git clone https://github.com/Brunno13/app-kmp.git
cd app-kmp
```

### 2. Start the backend

Clone and run `api-bun` following the instructions in its own repository:

```text
https://github.com/Brunno13/api-bun
```

Make sure the App-KMP backend URL points to an address reachable from the target device or simulator.

### 3. Run Android

Open the project root in Android Studio, wait for Gradle sync to finish, select the Android application configuration, then run it on an emulator or physical device.

You can also build the current Android production-debug variant from the terminal:

```bash
./gradlew :androidApp:assembleProductionDebug
```

On Windows:

```powershell
.\gradlew.bat :androidApp:assembleProductionDebug
```

### 4. Run iOS

On macOS, open the Xcode project inside `iosApp/`, select an iPhone simulator or physical device and run the application normally from Xcode.

To validate the shared iOS ARM64 framework from the terminal:

```bash
./gradlew :shared:linkReleaseFrameworkIosArm64
```

Production package:

```bash
./scripts/build_ios.sh
```

### 5. Run tests

macOS / Linux:

```bash
./gradlew :shared:testAndroidHostTest
```

Windows:

```powershell
.\gradlew.bat :shared:testAndroidHostTest
```

---

## 🧪 Testing & Quality

Current validated baseline:

```text
99 tests
0 failures
0 errors

Business coverage: PASS
Detekt commonMain: 0 findings
Detekt androidMain: 0 findings
Android Lint actionable warnings: 0
Android quality gate: PASS
macOS / iOS tests: PASS
```

The project currently validates:

- unit tests
- business coverage
- Detekt on authored `commonMain`
- Detekt on authored `androidMain`
- Android Lint
- CPD duplication baseline
- Gitleaks
- Android build
- iOS build and tests

Production pipelines successfully generate:

```text
app-kmp-production.apk
app-kmp-ios-production.zip
```

---

## 📐 Project Guidelines

The project follows a few practical rules to keep the base reusable and maintainable.

### Shared-first

- Put reusable UI, domain and data logic in `commonMain`.
- Add code to `androidMain` or `iosMain` only when platform APIs are actually required.
- Keep platform-specific behavior behind small abstractions or `expect` / `actual` implementations.

### Architecture

- Presentation depends on domain contracts, not infrastructure details.
- Domain models should not depend on UI or platform APIs.
- Data implementations own networking, persistence and secure storage details.
- Prefer constructor/DI-driven dependencies instead of service locators inside business code.

### Security

- Never expose authentication tokens to UI models.
- Never persist session tokens in Room.
- Never log credentials, cookies or authentication tokens.
- Keep credential storage behind the shared `AuthCredentialStore` abstraction.

### Code Quality

- Add tests for business behavior, edge cases and regressions.
- Do not create tests only to inflate coverage.
- Do not use global suppressions only to make static-analysis findings disappear.
- Do not create artificial abstractions only to reduce CPD.
- Treat dependency/version advisories as diagnostic information, not automatic failures.
- Upgrade dependency families incrementally and validate behavior after each meaningful change.

### Before opening a PR

Run the relevant tests and quality checks and make sure platform-specific behavior remains functional.

The CI pipeline is the final source of truth for the project quality gates.

---

## ⚙️ CI/CD

The project uses **self-hosted Woodpecker CI**.

```text
Pull Request / Push / Tag
        │
        ├── Linux / AMD64
        │     ├── tests
        │     ├── business coverage
        │     ├── Detekt
        │     ├── Android Lint
        │     ├── security checks
        │     └── Android build
        │
        └── macOS
              ├── KMP / iOS tests
              ├── Kotlin/Native compile
              ├── iOS arm64 framework
              └── production package
```

Android and iOS production pipelines are currently validated.

---

## 🗺️ Roadmap

### ✅ Completed

- [x] Kotlin Multiplatform Android + iOS foundation
- [x] Compose Multiplatform shared UI
- [x] Shared navigation
- [x] Koin dependency injection
- [x] Ktor + Ktorfit networking
- [x] kotlinx.serialization
- [x] Room KMP persistence
- [x] Multiplatform Settings
- [x] Login
- [x] Registration
- [x] Password recovery
- [x] Logout
- [x] Profile
- [x] Profile editing
- [x] Avatar
- [x] Biometric authentication on Android
- [x] Biometric authentication on iOS
- [x] Active session management
- [x] Session revocation
- [x] Secure credential storage
- [x] Android Keystore + AES-GCM
- [x] iOS Keychain storage
- [x] Room schema versioning
- [x] Unit tests
- [x] Business coverage gate
- [x] Detekt
- [x] CPD baseline
- [x] Android Lint
- [x] Android quality gate
- [x] Self-hosted Android CI
- [x] Self-hosted macOS / iOS CI
- [x] Android production pipeline
- [x] iOS production pipeline
- [x] Final functional validation on iOS

### 🔜 Future Features

- [ ] Feature menu
- [ ] Maps
- [ ] Wallet
- [ ] CarPlay / Android Auto
- [ ] Push notifications
- [ ] Google Calendar integration
- [ ] Apple Calendar integration

These features belong to future functional development cycles and are intentionally separate from the current code-quality and infrastructure work.

---

## 📦 Production Artifacts

Android:

```text
app-kmp-production.apk
```

iOS:

```text
app-kmp-ios-production.zip
```

---

## 📚 References

- [api-bun](https://github.com/Brunno13/api-bun)
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/)
- [Ktor](https://ktor.io/)
- [Koin](https://insert-koin.io/)
- [Room KMP](https://developer.android.com/kotlin/multiplatform/room)

---

This repository is intended to remain a reusable KMP foundation and evolve incrementally as new platform capabilities and application features are added.
