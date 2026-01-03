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

  const normalized = [
    ...new Set(
      phones
        .filter((phone) => typeof phone === 'string' && phone.trim().length > 0)
        .map((phone) => phone.trim()),
    ),
  ];

  if (normalized.length === 0) {
    return { registered: [] };
  }

  const registered = [];
  const result = [];
  const errors = [];
  const chunkSize = 100;
  let successfulChunks = 0;

  for (let index = 0; index < normalized.length; index += chunkSize) {
    const chunk = normalized.slice(index, index + chunkSize);
    try {
      const response = await admin.auth().getUsers(
        chunk.map((phone) => ({
          phoneNumber: phone,
        })),
      );
      successfulChunks += 1;
      const deleted = await fetchDeletedUserIds(response.users.map((user) => user.uid));
      response.users.forEach((userRecord) => {
        if (deleted.has(userRecord.uid)) return;
        const phoneNumber = userRecord.phoneNumber;
        if (!phoneNumber) return;
        registered.push(phoneNumber);
        result.push({
          phone: phoneNumber,
          uid: userRecord.uid,
        });
      });
    } catch (error) {
      errors.push({ phones: chunk, code: error.code, message: error.message });
    }
  }

  if (successfulChunks === 0) {
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
    const deleted = await fetchDeletedUserIds([userRecord.uid]);
    if (deleted.has(userRecord.uid)) {
      return { exists: false, uid: null };
    }
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

async function fetchDeletedUserIds(uids) {
  if (!Array.isArray(uids) || uids.length === 0) return new Set();
  const chunks = [];
  const chunkSize = 10;
  for (let index = 0; index < uids.length; index += chunkSize) {
    chunks.push(uids.slice(index, index + chunkSize));
  }

  const deletedIds = new Set();
  for (const chunk of chunks) {
    const snapshot = await admin.firestore()
      .collection('users')
      .where(admin.firestore.FieldPath.documentId(), 'in', chunk)
      .where('is_deleted', '==', true)
      .get();
    snapshot.docs.forEach((doc) => deletedIds.add(doc.id));
  }
  return deletedIds;
}
