package com.project.livechat.data.mappers

import com.project.livechat.domain.models.Contact
import com.project.livechat.shared.data.database.ContactEntity

fun ContactEntity.toDomain(): Contact =
    Contact(
        id = id.toInt(),
        name = name,
        phoneNo = phoneNo,
        description = description,
        photo = photo,
        isRegistered = isRegistered,
    )

fun Contact.toEntity(): ContactEntity =
    ContactEntity(
        name = name,
        phoneNo = phoneNo,
        description = description,
        photo = photo,
        isRegistered = isRegistered,
    )
