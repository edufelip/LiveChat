# Onboarding Test Cases

| ID | Title | Description | Expected Result |
|---|---|---|---|
| ONB-001 | Invalid Phone | Enter a phone number with insufficient digits. | "Continue" button remains disabled or shows error. |
| ONB-002 | Country Search | Search for "Brazil" in country picker. | Brazil (+55) is shown and selectable. |
| ONB-003 | Request OTP | Enter valid number and tap continue. | SMS is triggered, UI moves to OTP step. |
| ONB-004 | Session Check | Kill app and restart while logged in. | App opens directly to Home/Chats. |
| ONB-005 | Empty Phone | Attempt to continue with no phone number. | Validation error is displayed. |
