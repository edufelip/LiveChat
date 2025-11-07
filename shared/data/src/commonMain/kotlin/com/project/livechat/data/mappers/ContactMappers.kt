package com.project.livechat.data.mappers

import com.project.livechat.domain.models.Contact
import com.project.livechat.shared.data.database.Contacts
import com.project.livechat.shared.data.database.LiveChatDatabase

fun Contacts.toDomain(): Contact =
    Contact(
        id = id.toInt(),
        name = name,
        phoneNo = phone_no,
        description = description,
        photo = photo,
        isRegistered = is_registered == 1L,
    )

fun Contact.toInsertParams(): InsertContactParams =
    InsertContactParams(
        name = name,
        phoneNo = phoneNo,
        description = description,
        photo = photo,
        isRegistered = if (isRegistered) 1L else 0L,
    )

data class InsertContactParams(
    val name: String,
    val phoneNo: String,
    val description: String?,
    val photo: String?,
    val isRegistered: Long,
)

fun LiveChatDatabase.insertContact(param: InsertContactParams) {
    contactsQueries.insertContact(
        name = param.name,
        phone_no = param.phoneNo,
        description = param.description,
        photo = param.photo,
        is_registered = param.isRegistered,
    )
}

fun LiveChatDatabase.updateContact(contact: Contact) {
    contactsQueries.updateContactByPhone(
        name = contact.name,
        description = contact.description,
        photo = contact.photo,
        is_registered = if (contact.isRegistered) 1L else 0L,
        phone_no = contact.phoneNo,
    )
}
