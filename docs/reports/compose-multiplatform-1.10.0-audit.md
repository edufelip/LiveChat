# Compose Multiplatform 1.10.0 Upgrade and Adoption Report (LiveChat)

Date: 2026-02-01
Audience: Staff+ engineers and tech leads
Scope: Compose Multiplatform 1.10.0 adoption review, repo audit, and upgrade plan

## Executive Summary
Compose Multiplatform is already set to 1.10.0 in this repo. The main risk is not the version bump itself but the new 1.10.0 deprecations and new APIs that we are not yet using. The largest immediate action items are:

1. Migrate the deprecated Compose Gradle dependency aliases and preview annotation imports.
2. Replace `PredictiveBackHandler` usage with the Navigation Event API.
3. Validate iOS window insets and IME behavior changes.

Optional, higher-effort improvements include evaluating Navigation 3 adoption and aligning AndroidX Compose tooling/test versions to the 1.10.0 baseline.

## Current State Snapshot
Versioning and plugin state
- Compose Multiplatform: `1.10.0` in `gradle/libs.versions.toml`.
- Kotlin: `2.2.20` in `gradle/libs.versions.toml`.
- Compose Hot Reload plugin: `1.0.0` in `gradle/libs.versions.toml`, applied in `app/build.gradle.kts`.
- Compose dependencies are declared via the version catalog using `org.jetbrains.compose.*` coordinates (no deprecated `compose.*` aliases).
- Preview annotation uses the unified `androidx.compose.ui.tooling.preview.Preview` in `commonMain`.
- AndroidX Compose UI test libraries use `composeUi = 1.9.3` in `gradle/libs.versions.toml` (tooling previews now use `org.jetbrains.compose.ui:ui-tooling`).
- `material-icons-extended` is pinned to `1.7.3` because no `1.10.0` artifact is published in the Compose dev repository.

Repo evidence (selected)
- Compose plugin versions: `gradle/libs.versions.toml`.
- Hot Reload plugin applied: `app/build.gradle.kts`.
- Unified Preview annotation imports: multiple files under `app/src/commonMain/...` (see Appendix).
- Navigation Event back handler: `app/src/androidMain/kotlin/com/edufelip/livechat/ui/common/navigation/SettingsBackHandler.android.kt`.

## Feature-by-Feature Review (Compose Multiplatform 1.10.0)
Legend: Adopt, Adoptable, Monitor, N/A

| Feature or Change | Status | Repo Evidence | Recommendation |
| --- | --- | --- | --- |
| Unified `@Preview` annotation in `commonMain` | Adopted | Imports now use `androidx.compose.ui.tooling.preview.Preview` | Completed: migrated imports and preview runtime dependency. |
| Deprecated Compose Gradle dependency aliases (`compose.runtime`, etc.) | Adopted | `app/build.gradle.kts` uses version-catalog `org.jetbrains.compose.*` libs | Completed: replaced deprecated aliases with explicit coordinates. |
| Deprecated `PredictiveBackHandler` | Adopted | `SettingsBackHandler.android.kt` now uses Navigation Event API | Completed: migrated to `NavigationBackHandler`. |
| Support for Navigation 3 | Monitor | No navigation library dependencies; custom route wiring | Consider only if we want a cross-platform, list-driven back stack model. This is optional and not required for 1.10.0. |
| Bundled Compose Hot Reload | Monitor | `org.jetbrains.compose.hot-reload` applied | Evaluate removing the explicit plugin if bundled behavior is sufficient, or keep it if required by tooling. |
| Minimum Kotlin version 2.2.20 for native/web targets | Adopted | Kotlin 2.2.20 already set | No change required. |
| Autosizing interop views (SwingPanel, UIKitView) | N/A | No SwingPanel or UIKitView usage | No change required. |
| Stable Popup/Dialog properties and Popup overload errors | N/A | No `Popup` or `Dialog` calls found | No change required. |
| Skia updated to Milestone 138 | Monitor | Applies to iOS and desktop render paths | Run iOS visual regression checks and watch for font/layout changes. |
| iOS WindowInsetsRulers and captionBar @Composable | Monitor | `WindowInsets` used in `CallsScreen.kt`, `PhoneStep.kt` | Validate safe area and IME inset behavior on iOS. |
| iOS improved IME configuration (PlatformImeOptions) | Adoptable | No platform-specific IME config | Optional enhancement if we need custom keyboard input views or accessories. |
| Web and desktop-specific behavior updates | N/A | No web/desktop targets | No change required. |

## Recommended Actions (Prioritized)

1. [x] Migrate deprecated Compose dependency aliases.
2. [x] Migrate all preview annotations to the new unified `androidx.compose.ui.tooling.preview.Preview`.
3. [x] Replace `PredictiveBackHandler` with Navigation Event API.
4. [ ] Validate iOS insets and IME behavior after 1.10.0.
5. [ ] Decide whether to remove the explicit Compose Hot Reload plugin.
6. [ ] Optional: align AndroidX Compose UI tooling/test versions to 1.10.x and evaluate Navigation 3.

## Upgrade Plan (Repo-Specific, Implemented)

### 1) Replace Compose dependency aliases with explicit coordinates
Goal: Remove deprecated `compose.*` aliases and use explicit version-catalog entries.

Implemented changes
- Add library entries in `gradle/libs.versions.toml` for:
  - `org.jetbrains.compose.runtime:runtime` (1.10.0)
  - `org.jetbrains.compose.foundation:foundation` (1.10.0)
  - `org.jetbrains.compose.material:material` (1.10.0)
  - `org.jetbrains.compose.material3:material3` (1.10.0-alpha05)
  - `org.jetbrains.compose.animation:animation` (1.10.0)
  - `org.jetbrains.compose.material:material-icons-extended` (1.7.3)
  - `org.jetbrains.compose.components:components-resources` (1.10.0)
  - `org.jetbrains.compose.ui:ui-tooling-preview` (1.10.0)

- Updated `app/build.gradle.kts` to use version-catalog `org.jetbrains.compose.*` entries instead of `compose.*`.
  - Exception: `material-icons-extended` uses `1.7.3` due to artifact availability.

Notes
- Compose Multiplatform 1.10.0 publishes official versions for each artifact. The catalog should match those coordinates to avoid deprecation warnings and future build breaks.

### 2) Migrate `@Preview` usage to the unified annotation
Goal: Remove deprecated Preview annotation usage and align with 1.10.0 guidance.

Implemented changes
- Replace imports across `app/src/commonMain`:
  - From `org.jetbrains.compose.ui.tooling.preview.Preview`
  - To `androidx.compose.ui.tooling.preview.Preview`
- Replaced the preview runtime dependency in `commonMain` from `compose.components.uiToolingPreview` to `org.jetbrains.compose.ui:ui-tooling-preview`.
- Updated `debugImplementation` tooling dependency to `org.jetbrains.compose.ui:ui-tooling` for the Android target.

Notes
- The new annotation is fully multiplatform in 1.10.0, while the old one is deprecated.

### 3) Replace `PredictiveBackHandler`
Goal: Use Navigation Event APIs for predictive back gesture handling.

Implemented changes
- Add dependency: `org.jetbrains.androidx.navigationevent:navigationevent-compose:1.0.0`.
- Updated `SettingsBackHandler.android.kt` to:
  - Create a `NavigationEventState` via `rememberNavigationEventState`.
  - Use `NavigationBackHandler` with `onBackCancelled` and `onBackCompleted`.

Notes
- This is required to align with Compose 1.10.0 deprecation guidance.

### 4) Validate iOS insets and IME changes
Goal: Catch layout regressions introduced by the shared inset pipeline and IME updates.

Planned validation
- Test `CallsScreen` and `PhoneStep` on iOS for safe-area and IME padding behavior.
- Watch for changes in keyboard-related layout shifts in onboarding and settings flows.

### 5) Compose Hot Reload plugin decision
Goal: Decide whether the explicit plugin remains needed now that it is bundled.

Planned validation
- Check whether the explicit plugin is still required for our toolchain and Android-only targets.
- If not, remove `alias(libs.plugins.compose.hot.reload)` and the version catalog entry.

### 6) Optional follow-ups
- Align `androidx.compose.ui:ui-tooling` and `ui-test` artifacts with the Compose 1.10.0 baseline after verifying compatibility.
- Evaluate Navigation 3 adoption only if we want a cross-platform back-stack abstraction and a shared navigation model.

## Risks and Considerations
- Preview migration is a widespread import update across many files and should be done as a focused refactor to avoid merge conflicts.
- Predictive back changes may affect gesture feel; validate on Android 13+ devices.
- iOS layout changes could surface new spacing issues due to updated insets behavior.

## Verification Checklist
- `./gradlew :app:assembleDebug`
- `./gradlew :app:testDebugUnitTest`
- `./gradlew :app:lint`
- Manual smoke tests:
  - Preview rendering in Android Studio
  - Android back gesture behavior in settings screens
  - iOS safe-area and keyboard interaction on onboarding and calls screens

## Appendix: Preview Annotation Usage (Sample Files)
- `app/src/commonMain/kotlin/com/edufelip/livechat/ui/app/LiveChatApp.kt`
- `app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/conversations/detail/screens/ConversationDetailScreen.kt`
- `app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/settings/account/AccountSettingsRoute.kt`
- `app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/contacts/screens/ContactsScreen.kt`
- `app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/calls/screens/CallsScreen.kt`

## References
- Compose Multiplatform 1.10.0 release notes (Kotlin docs): `https://kotlinlang.org/docs/multiplatform/whats-new-compose-110.html`
- Compose previews in KMP: `https://kotlinlang.org/docs/multiplatform/compose-previews.html`
- Compose Multiplatform GitHub release notes: `https://github.com/JetBrains/compose-multiplatform/releases`
