# OTP Verification Specifications

## UI Components
- **OtpStep**: `app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/onboarding/steps/OtpStep.kt`

## Logic Implementation
- **Presenter**: `PhoneAuthPresenter.kt`
    - Method `verifyCode(code: String)`
    - Method `resendCode(presentationContext: PhoneAuthPresentationContext)`
- **Timer**: Managed via a Coroutine Job in `PhoneAuthPresenter` using a `countdownSeconds` StateFlow.

## Use Cases (Domain)
- `VerifyOtpUseCase`
- `ResendPhoneVerificationUseCase`

## Constants
- `OTP_LENGTH = 6`
- `DEFAULT_COUNTDOWN_SECONDS = 60`
