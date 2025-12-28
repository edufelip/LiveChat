# Firebase Test Scenario Matrix

This matrix defines the authoritative set of Firebase scenarios that must be covered by tests.  
Each scenario is mapped to a test type and an owning test file. Add new scenarios here first.

Legend:
- **Unit**: JVM/iOS Kotlin tests (fast, isolated, mocked bridges)
- **Contract**: validates platform bridge payloads/mappings
- **Emulator**: Firebase Emulator Suite (Android + iOS)
- **UI/E2E**: app-level smoke tests

Status: `TODO` | `IN-PROGRESS` | `DONE`

## Auth (Phone / FirebaseAuth)

| ID | Scenario | Expected | Test Type | Target | Owner File | Status |
| --- | --- | --- | --- | --- | --- | --- |
| AUTH-01 | Send verification code with valid E.164 | Code request succeeds | Emulator | Android + iOS | (new) AuthEmulatorTests | TODO |
| AUTH-02 | Send verification code invalid number | Maps to InvalidPhoneNumber | Emulator | Android + iOS | (new) AuthEmulatorTests | TODO |
| AUTH-03 | Verify valid code | Session becomes authenticated | Emulator | Android + iOS | (new) AuthEmulatorTests | TODO |
| AUTH-04 | Verify invalid code | Maps to InvalidVerificationCode | Emulator | Android + iOS | (new) AuthEmulatorTests | TODO |
| AUTH-05 | Auth rate-limited | Maps to TooManyRequests | Emulator | Android + iOS | (new) AuthEmulatorTests | TODO |
| AUTH-06 | Network failure | Maps to NetworkError | Unit | Shared (iOS/Android) | `IosPhoneAuthRepositoryTest` + Android counterpart | TODO |
| AUTH-07 | Unauthenticated Firestore/Functions/Storage call | Permission denied | Emulator | Android + iOS | (new) AuthEmulatorTests | TODO |
| AUTH-08 | Token refresh returns valid ID token | Session updated | Contract | Android + iOS | Bridge contract tests | TODO |

## Firestore: Messages (Inbox)

| ID | Scenario | Expected | Test Type | Target | Owner File | Status |
| --- | --- | --- | --- | --- | --- | --- |
| MSG-01 | Send text message | Firestore doc created with required fields | Emulator | Android + iOS | (new) MessagesEmulatorTests | TODO |
| MSG-02 | Send image message | Storage upload + Firestore doc with remote URL | Emulator | Android + iOS | (new) MessagesEmulatorTests | IN-PROGRESS |
| MSG-03 | Send audio message | Storage upload + Firestore doc with remote URL | Emulator | Android + iOS | (new) MessagesEmulatorTests | TODO |
| MSG-04 | Receive message | Local model mapped and emitted | Unit | Shared | `FirebaseMessagesRemoteDataTest` | IN-PROGRESS |
| MSG-05 | Pull history | Returns ordered messages | Emulator | Android + iOS | (new) MessagesEmulatorTests | TODO |
| MSG-06 | Delivered action processed | Status updated | Unit | Shared | Repository tests | TODO |
| MSG-07 | Read action processed | Status updated | Unit | Shared | Repository tests | TODO |
| MSG-08 | Malformed action deleted | Not emitted; delete attempted | Unit | Shared | `FirebaseMessagesRemoteDataTest` | DONE |
| MSG-09 | Firestore permission denied on pull | Empty list + log | Unit | Shared | `FirebaseMessagesRemoteDataTest` | TODO |
| MSG-10 | Duplicate message payloads | Deduplicated | Unit | Shared | Repository tests | TODO |
| MSG-11 | Action arrives before message | No crash; eventual status | Emulator | Android + iOS | (new) MessagesEmulatorTests | TODO |
| MSG-12 | Delete remote message after processing | Firestore delete invoked | Contract | Android + iOS | Bridge contract tests | TODO |

## Storage: Media

| ID | Scenario | Expected | Test Type | Target | Owner File | Status |
| --- | --- | --- | --- | --- | --- | --- |
| STO-01 | Upload image under size limit | Upload succeeds, URL returned | Emulator | Android + iOS | (new) StorageEmulatorTests | TODO |
| STO-02 | Upload audio under size limit | Upload succeeds, URL returned | Emulator | Android + iOS | (new) StorageEmulatorTests | TODO |
| STO-03 | Upload over size limit | Permission denied | Emulator | Android + iOS | (new) StorageEmulatorTests | TODO |
| STO-04 | Download media | Bytes returned, local file saved | Emulator | Android + iOS | (new) StorageEmulatorTests | IN-PROGRESS |
| STO-05 | Delete media after read | Remote delete succeeds | Emulator | Android + iOS | (new) StorageEmulatorTests | IN-PROGRESS |
| STO-06 | Delete media idempotent | No failure if already deleted | Emulator | Android + iOS | (new) StorageEmulatorTests | TODO |
| STO-07 | Invalid URL | Error mapped + no crash | Unit | Shared | `FirebaseMessagesRemoteDataTest` | TODO |

## Contacts & Invites (Firestore + Functions)

| ID | Scenario | Expected | Test Type | Target | Owner File | Status |
| --- | --- | --- | --- | --- | --- | --- |
| CON-01 | phoneExists (single) registered | Returns exists=true | Emulator | Android + iOS | (new) ContactsEmulatorTests | IN-PROGRESS |
| CON-02 | phoneExists (single) unregistered | Returns exists=false | Emulator | Android + iOS | (new) ContactsEmulatorTests | IN-PROGRESS |
| CON-03 | phoneExistsMany mixed list | Returns registered + matches | Emulator | Android + iOS | (new) ContactsEmulatorTests | IN-PROGRESS |
| CON-04 | phoneExistsMany invalid payload | Functions error mapped | Emulator | Android + iOS | (new) ContactsEmulatorTests | IN-PROGRESS |
| CON-05 | phoneExistsMany partial failure | Returns partial matches | Emulator | Android + iOS | (new) ContactsEmulatorTests | TODO |
| CON-06 | Invite contact (Firestore REST) | Firestore doc created | Emulator | Android + iOS | (new) InvitesEmulatorTests | TODO |
| CON-07 | Invite contact without auth | Permission denied | Emulator | Android + iOS | (new) InvitesEmulatorTests | TODO |
| CON-08 | Invite malformed payload | Request fails | Unit | Shared | `FirebaseRestContactsRemoteDataTest` | DONE |

## Cross-Cutting / Infra

| ID | Scenario | Expected | Test Type | Target | Owner File | Status |
| --- | --- | --- | --- | --- | --- | --- |
| INF-01 | Emulator config applied (Auth/Firestore/Storage/Functions) | SDK uses emulator endpoints | Contract | Android + iOS | Emulator config tests | TODO |
| INF-02 | Missing Firebase config in app | Graceful error | Unit | Shared | Config tests | TODO |
