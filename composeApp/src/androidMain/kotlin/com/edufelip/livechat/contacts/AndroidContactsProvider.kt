package com.edufelip.livechat.contacts

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import com.edufelip.livechat.domain.models.Contact
import java.util.Locale

object AndroidContactsProvider {
    fun fetch(context: Context): List<Contact> {
        return runCatching { queryContacts(context) }.getOrDefault(emptyList())
    }

    private fun queryContacts(context: Context): List<Contact> {
        val resolver = context.contentResolver
        val projection =
            arrayOf(
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
            )

        val cursor =
            resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " ASC",
            ) ?: return emptyList()

        return cursor.use { mapCursor(it) }
    }

    private fun mapCursor(cursor: Cursor): List<Contact> {
        val idIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)
        val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY)
        val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val normalizedIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER)
        val photoIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)

        if (numberIndex == -1) return emptyList()

        val contacts = mutableListOf<Contact>()
        val seen = mutableSetOf<String>()

        while (cursor.moveToNext()) {
            val rawNumber = cursor.getString(numberIndex)?.trim().orEmpty()
            if (rawNumber.isBlank()) continue

            val localeIso = Locale.getDefault().country.takeIf { it.isNotBlank() }
            val normalizedFromCursor =
                if (normalizedIndex != -1) {
                    cursor.getString(normalizedIndex)?.trim()
                } else {
                    null
                }
            val canonical =
                normalizedFromCursor
                    ?.takeIf { it.isNotBlank() }
                    ?: PhoneNumberUtils.formatNumberToE164(rawNumber, localeIso)
                    ?: rawNumber.filterNot { it.isWhitespace() }

            val key = canonical
            if (!seen.add(key)) continue

            val id = if (idIndex != -1) cursor.getInt(idIndex) else contacts.size
            val name = cursor.getString(nameIndex)?.takeIf { it.isNotBlank() } ?: rawNumber
            val photo = if (photoIndex != -1) cursor.getString(photoIndex) else null

            contacts.add(
                Contact(
                    id = id,
                    name = name,
                    phoneNo = canonical,
                    photo = photo,
                    isRegistered = false,
                ),
            )
        }

        return contacts
    }
}
