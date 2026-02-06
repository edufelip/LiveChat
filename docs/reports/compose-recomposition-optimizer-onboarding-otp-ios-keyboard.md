# Compose Recomposition Optimization Report

## Summary

- Goal: Improve OTP onboarding keyboard UX on iOS while keeping Compose recomposition behavior efficient.
- Measurement artifacts used (Layout Inspector / compiler reports / benchmarks / traces): Static heuristics only (no runtime metrics captured in this pass).
- Scope (modules/screens): `app` module, onboarding Phone/OTP flow composables.

## Files Changed

- [x] `app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/onboarding/steps/OtpStep.kt`
- [x] `app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/onboarding/OnboardingFlowScreen.kt`

## Changes by Rule

### Rule A — Read State in the Narrowest Scope

- **File:** `app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/onboarding/steps/OtpStep.kt`
- **Composables:** `OTPStep`, `OtpCodeField`
- **Pattern observed:** Focus and OTP bounds state are consumed only in the OTP container scope.
- **Fix applied:** Kept focus/bounds state local to OTP UI and avoided lifting this state up to flow-level screen state.
- **Why it reduces recomposition:** Limits invalidation to OTP screen subtree instead of onboarding parent structures.
- **Risk level:** Low
- **Validation:** Code inspection and successful module compilation.

### Rule D — Defer Hot Reads to Layout/Draw

- **File:** `app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/onboarding/steps/OtpStep.kt`
- **Composables:** `OTPStep`
- **Pattern observed:** Pointer coordinates compared against input bounds for dismiss behavior.
- **Fix applied:** Used `onGloballyPositioned` and `boundsInRoot()` for geometry and pointer checks in pointer input path.
- **Why it reduces recomposition:** Avoids introducing composition-time state derivations from keyboard interactions.
- **Risk level:** Low
- **Validation:** Code inspection and successful module compilation.

### Rule F — Improve Parameter Stability

- **File:** `app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/onboarding/OnboardingFlowScreen.kt`
- **Composables / Models:** `OnboardingFlowScreen`, `UiTestOnboardingFlow`
- **Pattern observed (compiler report or heuristic):** Platform branch around `imePadding()` added conditional modifier selection in composition.
- **Fix applied:** Unified modifier chain with unconditional `imePadding()` to reduce per-recomposition branching and align behavior across platforms.
- **Why it reduces recomposition:** Keeps modifier composition path consistent and simpler across targets.
- **Risk level:** Low
- **Validation:** Code inspection and successful module compilation.

## Compiler Reports Summary (If Available)

- Restartable but not skippable (before → after): Not measured.
- Notable unstable parameters (before → after): Not measured.
- Strong skipping config changes: None.

## Recommended Follow-Ups

- [ ] Run Layout Inspector recomposition/skip counters on onboarding OTP step in iOS simulator.
- [ ] Run `./gradlew :app:androidDeviceTest` on a connected device/emulator for onboarding UI assertions.
- [ ] Capture before/after trace during OTP keyboard open/close interactions to validate frame pacing.
