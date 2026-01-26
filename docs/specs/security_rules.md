# Security Rules

LiveChat uses Firebase Security Rules to enforce data access policies at the infrastructure level.

## Firestore Rules

The Firestore database is organized to protect user privacy and prevent unauthorized message access.

### Key Policies
1.  **User Profiles**: Users can only read and write their own profile data (`/users/{userId}`).
2.  **Privacy & Blocked Contacts**: Access is restricted to the owner of the account.
3.  **Presence**: Any authenticated user can read the presence status of others (subject to the recipient's privacy settings), but only the owner can update their own presence.
4.  **Inbox Actions**:
    - **Queue Structure**: Action items live under `/inboxes/{ownerId}/items/{itemId}` and are consumed by the owner.
    - **Read Access**: Only the inbox owner can read or delete items from their inbox.
    - **Write Access**: Any authenticated, active user can *create* an action item in another user's inbox, as long as the payload is valid.
    - **Immutability**: Items are append-only. Updates are disallowed; the owner deletes after processing.

### Inbox Action Payloads
- **message**: `sender_id`, `receiver_id`, `message_id`, `content_type`, `content`, `created_at`.
- **delivered/read**: `sender_id`, `receiver_id`, `action_message_id`, `created_at`.

## Storage Rules

Firebase Storage manages media artifacts like images and voice messages.

### Path Convention
`messages/{receiverId}/{senderId}/{fileName}`

### Key Policies
- **Read Access**: Only the sender or the receiver can download the file.
- **Write Access**: Only the sender can upload a file, and it must be under 25MB.
- **Cleanup**: Both the sender and receiver have permission to delete the media (typically done after the recipient has successfully downloaded and cached it locally).

## Validation Functions
The rules include helper functions like `isSignedIn()`, `isActiveUser()`, `isValidMessageItem()`, and `isValidReceiptItem()` to keep logic consistent and reduce duplication.
