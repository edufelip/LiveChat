const { onCall, HttpsError } = require('firebase-functions/v2/https');
const admin = require('firebase-admin');

admin.initializeApp();

exports.phoneExistsMany = onCall(async (req) => {
  if (!req.auth) {
    throw new HttpsError('unauthenticated', 'Login required');
  }

  const phones = req.data?.phones;
  if (!Array.isArray(phones) || phones.length === 0) {
    throw new HttpsError('invalid-argument', 'phones must be a non-empty array of strings');
  }

  const normalized = phones
    .filter((phone) => typeof phone === 'string' && phone.trim().length > 0)
    .map((phone) => phone.trim());

  if (normalized.length === 0) {
    return { registered: [] };
  }

  const registered = [];
  const result = [];
  const errors = [];

  await Promise.all(
    normalized.map(async (phone) => {
      try {
        const userRecord = await admin.auth().getUserByPhoneNumber(phone);
        if (userRecord) {
          registered.push(phone);
          result.push({
            phone,
            uid: userRecord.uid,
          });
        }
      } catch (error) {
        if (error.code === 'auth/user-not-found') {
          return;
        }
        errors.push({ phone, code: error.code, message: error.message });
      }
    }),
  );

  if (errors.length === normalized.length) {
    throw new HttpsError('internal', 'Failed to verify phone numbers', { errors });
  }

  return { registered, matches: result };
});

exports.phoneExists = onCall(async (req) => {
  if (!req.auth) {
    throw new HttpsError('unauthenticated', 'Login required');
  }

  const phone = req.data?.phone;
  if (!phone || typeof phone !== 'string') {
    throw new HttpsError('invalid-argument', 'phone must be a string');
  }

  try {
    const userRecord = await admin.auth().getUserByPhoneNumber(phone.trim());
    return { exists: Boolean(userRecord), uid: userRecord?.uid ?? null };
  } catch (error) {
    if (error.code === 'auth/user-not-found') {
      return { exists: false, uid: null };
    }
    throw new HttpsError('internal', 'Failed to verify phone number', {
      message: error.message,
      code: error.code,
    });
  }
});
