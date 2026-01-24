const { onDocumentCreated } = require('firebase-functions/v2/firestore');
const admin = require('firebase-admin');

/**
 * Cloud Function that triggers when a new message is created in a user's conversation inbox.
 * Sends push notifications to all active devices registered for the recipient.
 * 
 * Trigger: conversations/{recipientId}/messages/{messageId}
 */
exports.onNewMessage = onDocumentCreated('conversations/{recipientId}/messages/{messageId}', async (event) => {
  const messageSnapshot = event.data;
  const { recipientId, messageId } = event.params;

  if (!messageSnapshot) {
    console.log('No message data found');
    return null;
  }

  const messageData = messageSnapshot.data();
  
  // Skip action messages (read receipts, delivered receipts, etc.)
  if (messageData.payload_type === 'action') {
    console.log(`Skipping action message: ${messageId}`);
    return null;
  }

  // Only process actual messages
  if (messageData.payload_type !== 'message') {
    console.log(`Skipping non-message payload_type: ${messageData.payload_type}`);
    return null;
  }

  const senderId = messageData.sender_id;
  const messageType = messageData.type; // 'text', 'image', 'audio'
  const messageContent = messageData.content || '';

  console.log(`Processing message ${messageId} from ${senderId} to ${recipientId}`);

  try {
    // Fetch recipient's active device tokens
    const devicesSnapshot = await admin.firestore()
      .collection('users')
      .doc(recipientId)
      .collection('devices')
      .where('is_active', '==', true)
      .get();

    if (devicesSnapshot.empty) {
      console.log(`No active devices found for recipient ${recipientId}`);
      return null;
    }

    const tokens = [];
    const deviceMap = {}; // Map tokens to device IDs for cleanup

    devicesSnapshot.forEach((doc) => {
      const deviceData = doc.data();
      if (deviceData.fcm_token) {
        tokens.push(deviceData.fcm_token);
        deviceMap[deviceData.fcm_token] = doc.id;
      }
    });

    if (tokens.length === 0) {
      console.log(`No FCM tokens found for recipient ${recipientId}`);
      return null;
    }

    console.log(`Found ${tokens.length} active tokens for recipient ${recipientId}`);

    // Fetch sender's display name
    let senderName = 'Someone';
    try {
      const senderDoc = await admin.firestore()
        .collection('users')
        .doc(senderId)
        .get();
      
      if (senderDoc.exists) {
        const senderData = senderDoc.data();
        senderName = senderData.display_name || senderData.name || senderName;
      }
    } catch (error) {
      console.error(`Failed to fetch sender name for ${senderId}:`, error);
    }

    // Format notification body based on message type
    let notificationBody = '';
    switch (messageType) {
      case 'text':
        notificationBody = messageContent.length > 100 
          ? messageContent.substring(0, 100) + '...' 
          : messageContent;
        break;
      case 'image':
        notificationBody = '📷 Image';
        break;
      case 'audio':
        notificationBody = '🎤 Audio message';
        break;
      default:
        notificationBody = 'New message';
    }

    // Construct the FCM message
    const message = {
      notification: {
        title: senderName,
        body: notificationBody,
      },
      data: {
        conversation_id: senderId, // In direct messages, conversationId = senderId
        message_id: messageId,
        sender_name: senderName,
        message_type: messageType,
      },
      android: {
        priority: 'high',
        notification: {
          channelId: 'messages',
          sound: 'notification_popcorn.wav',
          clickAction: 'FLUTTER_NOTIFICATION_CLICK',
        },
      },
      apns: {
        payload: {
          aps: {
            sound: 'notification_popcorn.wav',
            badge: 1,
          },
        },
      },
      tokens: tokens,
    };

    // Send notification to all tokens
    const response = await admin.messaging().sendEachForMulticast(message);

    console.log(`Successfully sent ${response.successCount} notifications`);
    console.log(`Failed to send ${response.failureCount} notifications`);

    // Clean up invalid tokens
    if (response.failureCount > 0) {
      const invalidTokens = [];
      response.responses.forEach((resp, idx) => {
        if (!resp.success) {
          const error = resp.error;
          console.error(`Failed to send to token ${idx}:`, error.code, error.message);
          
          // Remove tokens that are no longer valid
          if (error.code === 'messaging/invalid-registration-token' ||
              error.code === 'messaging/registration-token-not-registered') {
            const token = tokens[idx];
            const deviceId = deviceMap[token];
            if (deviceId) {
              invalidTokens.push(deviceId);
            }
          }
        }
      });

      // Delete invalid device tokens from Firestore
      if (invalidTokens.length > 0) {
        console.log(`Cleaning up ${invalidTokens.length} invalid tokens`);
        const batch = admin.firestore().batch();
        invalidTokens.forEach((deviceId) => {
          const deviceRef = admin.firestore()
            .collection('users')
            .doc(recipientId)
            .collection('devices')
            .doc(deviceId);
          batch.delete(deviceRef);
        });
        await batch.commit();
        console.log('Invalid tokens cleaned up successfully');
      }
    }

    return { success: true, sentCount: response.successCount };
  } catch (error) {
    console.error('Error sending notification:', error);
    return { success: false, error: error.message };
  }
});
