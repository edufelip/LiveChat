package com.edufelip.livechat.data.mappers

import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.shared.data.database.ContactEntity

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
