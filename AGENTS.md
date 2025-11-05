# Repository Guidelines

## Project Structure & Module Organization
- `composeApp/`: Kotlin Multiplatform UI (Jetpack Compose + Compose for iOS); Android-specific code in `androidMain`, shared UI in `commonMain`, iOS entry points in `iosMain`.
- `shared/domain/`: Domain layer with use cases, presenters, models, Koin modules, and common tests under `commonTest`.
- `shared/data/`: Data layer (repositories, Ktor clients, SQLDelight) with platform targets; settings-backed onboarding persistence here.
- `iosApp/`: Native Swift runner wiring the shared modules for iOS.
- `app/`: Android host app (activities, manifests, Gradle config).

## Build, Test, and Development Commands
- `./gradlew assembleDebug`: Builds Android debug APK; requires `JAVA_HOME` (JDK 17) and Android SDK.
- `./gradlew shared:domain:commonTest`: Runs domain-layer unit tests on all supported targets.
- `./gradlew composeApp:iosX64Test`: Executes iOS simulator tests for shared code.
- `./gradlew detekt ktlintFormat`: Run repository linting/formatting (add if configured locally).

## Coding Style & Naming Conventions
- Kotlin: 4-space indent, KMP-compatible APIs, prefer `val` over `var`, `PascalCase` for classes/composables, `lowerCamelCase` for functions/variables.
- Compose: Keep composables side-effect free, use `remember`/`rememberSaveable` as in `LiveChatApp`.
- Dependency injection: Resolve presenters and repositories via Koin modules in `shared/domain` or `shared/data`.

## Testing Guidelines
- Unit tests live beside code (`commonTest`) using Kotlin test + Turbine for coroutines.
- Test names follow `functionUnderTest_expectedOutcome` convention.
- Run `./gradlew shared:domain:commonTest` before submitting; add platform tests if touching iOS/Android code.

## Commit & Pull Request Guidelines
- Commit messages follow `<scope>: <summary>` (e.g., `app: refactor shell and persist onboarding state`).
- Reference issues in the body when applicable; keep commits focused.
- Pull requests should include: description of changes, screenshots for UI updates, test plan (`./gradlew shared:domain:commonTest`), and mention of any follow-up work.

## Security & Configuration Tips
- Do not commit secrets; `google-services.json` and `GoogleService-Info.plist` are git-ignored—keep them local.
- Configure Firebase and platform settings through environment-specific files outside version control.
