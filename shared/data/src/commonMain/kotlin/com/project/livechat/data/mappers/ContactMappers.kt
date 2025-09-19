package com.project.livechat.data.mappers

import com.project.livechat.domain.models.Contact
import com.project.livechat.shared.data.database.LiveChatDatabase
import com.project.livechat.shared.data.database.Contacts

fun Contacts.toDomain(): Contact = Contact(
    id = id.toInt(),
    name = name,
    phoneNo = phone_no,
    description = description,
    photo = photo
)

fun Contact.toInsertParams(): InsertContactParams = InsertContactParams(
    name = name,
    phoneNo = phoneNo,
    description = description,
    photo = photo
)

data class InsertContactParams(
    val name: String,
    val phoneNo: String,
    val description: String?,
    val photo: String?
)

fun LiveChatDatabase.insertContact(param: InsertContactParams) {
    contactsQueries.insertContact(
        name = param.name,
        phone_no = param.phoneNo,
        description = param.description,
        photo = param.photo
    )
}

fun LiveChatDatabase.updateContact(contact: Contact) {
    contactsQueries.updateContactByPhone(
        name = contact.name,
        description = contact.description,
        photo = contact.photo,
        phone_no = contact.phoneNo
    )
}
