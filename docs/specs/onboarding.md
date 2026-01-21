# Onboarding Specifications

## Architecture
The onboarding flow is implemented as a multi-step state machine within `OnboardingFlowScreen.kt`.

## UI Components
- **WelcomeScreen**: `app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/onboarding/WelcomeScreen.kt`
- **PhoneStep**: `app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/onboarding/steps/PhoneStep.kt`
- **CountryPickerDialog**: `app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/onboarding/dialogs/CountryPickerDialog.kt`

## Domain Logic
- **Presenter**: `PhoneAuthPresenter.kt`
- **Use Cases**:
    - `RequestPhoneVerificationUseCase`: Handles the initial SMS request.
    - `ObserveOnboardingStatusUseCase`: Monitors the user's progress through the flow.

## Data Integration
- **Auth Bridge**: Uses `PhoneAuthBridge` (Android/iOS native implementations) to interface with Firebase Authentication.
- **Persistence**: Onboarding completion status is stored in the local Room database via `OnboardingStatusEntity`.
