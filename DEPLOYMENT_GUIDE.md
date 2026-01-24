# Cloud Function Deployment Guide

## Issue
Firebase CLI in WSL2 is experiencing timeout issues when trying to analyze and deploy Cloud Functions. This is a known WSL2 limitation with Firebase CLI.

## Solution Options

### Option 1: Deploy via Firebase Console (EASIEST ✅)

1. **Navigate to Firebase Console:**
   - URL: https://console.firebase.google.com/project/livechat-3ad1d/functions

2. **Create New Function:**
   - Click "Create Function" button
   
3. **Configure Function Settings:**
   - **Name:** `onNewMessage`
   - **Region:** `us-central1` (or your preferred region)
   - **Trigger Type:** Cloud Firestore
   - **Event Type:** `google.cloud.firestore.document.v1.created`
   - **Document Path:** `conversations/{recipientId}/messages/{messageId}`
   - **Runtime:** Node.js 20

4. **Add Function Code:**
   - Copy the entire content from: `functions/messaging.js`
   - Paste into the inline editor
   - Make sure Firebase Admin is initialized (add `admin.initializeApp()` if needed)

5. **Deploy:**
   - Click "Deploy"
   - Wait for deployment to complete (usually 2-3 minutes)

### Option 2: Deploy from Native Environment

If you have access to a native Linux, macOS, or Windows (non-WSL) environment with Firebase CLI installed:

```bash
cd /path/to/livechat
firebase deploy --only functions:onNewMessage
```

### Option 3: Use Google Cloud CLI (if installed)

```bash
gcloud functions deploy onNewMessage \
  --gen2 \
  --runtime=nodejs20 \
  --region=us-central1 \
  --source=functions \
  --entry-point=onNewMessage \
  --trigger-event-filters=type=google.cloud.firestore.document.v1.created \
  --trigger-event-filters=database='(default)' \
  --trigger-event-filters-path-pattern=document='conversations/{recipientId}/messages/{messageId}' \
  --project=livechat-3ad1d
```

### Option 4: Deploy via VS Code Firebase Extension

1. Install "Firebase Explorer" extension in VS Code
2. Sign in to Firebase
3. Navigate to Functions
4. Right-click on `onNewMessage` function
5. Select "Deploy Function"

## Function Details

### Trigger Path
```
conversations/{recipientId}/messages/{messageId}
```

### Event Type
```
google.cloud.firestore.document.v1.created
```

### Runtime
- Node.js 20

### Dependencies
- firebase-admin: ^12.1.0
- firebase-functions: ^7.0.2

### Environment Variables
None required (uses default Firebase project credentials)

## Verification Steps

After deployment:

1. **Check Function Status:**
   ```bash
   firebase functions:log --only onNewMessage
   ```

2. **Test the Function:**
   - Send a test message in the app
   - Check Cloud Functions logs for execution
   - Verify notification is received on recipient device

3. **Monitor Errors:**
   - Firebase Console → Functions → Logs
   - Look for any errors or warnings

## Firestore Security Rules

✅ **Already Deployed**

The required security rules for the `devices` collection have been successfully deployed.

## What's Already Working

✅ FCM token registration on Android
✅ Device token storage in Firestore (`users/{userId}/devices/{deviceId}`)
✅ Firestore security rules
✅ Android FCM Service (`LiveChatMessagingService`)
✅ MainActivity notification handling
✅ Token refresh handling

## What's Pending

⚠️ Cloud Function deployment (this guide)
⚠️ iOS APNs integration (future task)

## Testing Checklist

After deploying the Cloud Function:

- [ ] Build and install app on two test devices
- [ ] Verify FCM tokens are stored in Firestore
- [ ] User A sends message to User B
- [ ] User B's app is in background or screen locked
- [ ] User B receives system notification
- [ ] Tapping notification opens the correct conversation
- [ ] Check Cloud Function logs for execution details

## Troubleshooting

### Function not triggering
- Verify trigger path matches: `conversations/{recipientId}/messages/{messageId}`
- Check Firestore rules allow writes to this path
- Verify message document has required fields

### Notifications not received
- Check FCM token is valid in Firestore
- Verify `is_active == true` for device
- Check Cloud Function logs for errors
- Verify FCM service account has correct permissions

### Invalid tokens
- Function automatically cleans up invalid tokens
- Check logs for cleanup messages
- Deleted tokens are removed from Firestore

## Contact

If you encounter issues:
- Check Firebase Console logs
- Review Cloud Functions documentation: https://firebase.google.com/docs/functions
- WSL2 Firebase CLI issues: https://github.com/firebase/firebase-tools/issues
