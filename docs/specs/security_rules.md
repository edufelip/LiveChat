# Security Rules

LiveChat uses Firebase Security Rules to enforce data access policies at the infrastructure level.

## Firestore Rules

The Firestore database is organized to protect user privacy and prevent unauthorized message access.

### Key Policies
1.  **User Profiles**: Users can only read and write their own profile data (`/users/{userId}`).
2.  **Privacy & Blocked Contacts**: Access is restricted to the owner of the account.
3.  **Presence**: Any authenticated user can read the presence status of others (subject to the recipient's privacy settings), but only the owner can update their own presence.
4.  **Messaging**:
    - **Inboxes**: Messages are stored in a structure like `/conversations/{recipientId}/messages/{messageId}`.
    - **Read Access**: Only the recipient (the "inbox owner") can read messages in their inbox.
    - **Write Access**: Any authenticated user can *create* a message in another user's inbox, provided the message follows a strict validation schema (sender ID matches auth UID, content type is valid, etc.).

## Storage Rules

Firebase Storage manages media artifacts like images and voice messages.

### Path Convention
`messages/{receiverId}/{senderId}/{fileName}`

### Key Policies
- **Read Access**: Only the sender or the receiver can download the file.
- **Write Access**: Only the sender can upload a file, and it must be under 25MB.
- **Cleanup**: Both the sender and receiver have permission to delete the media (typically done after the recipient has successfully downloaded and cached it locally).

## Validation Functions
The rules include helper functions like `isSignedIn()`, `isActiveUser()`, and `isValidMessage()` to maintain clean and reusable security logic.
