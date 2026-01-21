# Backend Functions (Firebase Functions)

LiveChat leverages Firebase Cloud Functions to perform sensitive operations and bridge functionality that requires administrative privileges.

## Core Functions

### `phoneExists`
- **Purpose**: Checks if a single phone number is registered on the platform.
- **Security**: Requires an authenticated user session.
- **Logic**: Queries Firebase Auth for the phone number and cross-references with the Firestore `users` collection to ensure the account isn't marked as deleted.

### `phoneExistsMany`
- **Purpose**: Optimized batch checking for multiple phone numbers. Used primarily during Contact Sync.
- **Batching**: Processes numbers in chunks (e.g., 100 at a time) to avoid timeout and resource limits.
- **Logic**: Returns a list of registered phone numbers and their corresponding Firebase UIDs.

## Deployment & Development

- **Language**: JavaScript (Node.js).
- **Location**: `/functions` directory.
- **Testing**: Functions can be tested locally using the Firebase Emulator Suite.

## Technical Details

- Uses `firebase-admin` SDK for direct access to Auth and Firestore.
- Implements `onCall` (v2) handlers for easy invocation from the KMP client.
- Handles "soft deleted" users by checking the `is_deleted` flag in Firestore.
