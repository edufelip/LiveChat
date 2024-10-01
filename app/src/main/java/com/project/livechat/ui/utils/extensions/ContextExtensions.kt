package com.project.livechat.ui.utils.extensions

import android.content.Context
import android.provider.ContactsContract
import android.widget.Toast
import com.project.livechat.domain.models.Contact

fun Context.getAllContacts(): ArrayList<Contact> {
    val contactList = arrayListOf<Contact>()
    val cr = this.contentResolver
    val cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
    if ((cursor?.count ?: 0) > 0) {
        while (cursor != null && cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)
                .takeIf { it >= 0 } ?: 0)
            val name =
                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    .takeIf { it >= 0 } ?: 0)
            if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                    .takeIf { it >= 0 } ?: 0) > 0
            ) {
                val pCur = cr.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    arrayOf(id),
                    null
                )
                while (pCur?.moveToNext() == true) {
                    val phoneNumber = pCur.getString(pCur.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    ).takeIf { it >= 0 } ?: 0)
                    contactList.add(Contact(id.toInt(), name = name, phoneNo = phoneNumber))
                }
                pCur?.close()
            }
        }
    }
    cursor?.close()
    return contactList
}

fun Context.toastShort(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}