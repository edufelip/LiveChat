# OTP Verification Test Cases

| ID | Title | Description | Expected Result |
|---|---|---|---|
| OTP-001 | Valid Code | Enter correct 6-digit code. | Successful login and navigation. |
| OTP-002 | Invalid Code | Enter an incorrect 6-digit code. | Error "Invalid verification code" is shown. |
| OTP-003 | Auto-Submit | Type 6th digit. | Verification starts immediately (loading state). |
| OTP-004 | Resend Wait | Check resend button during cooldown. | Button is disabled and shows remaining seconds. |
| OTP-005 | Resend Click | Click resend after 60s. | New code is sent, timer resets to 60. |
