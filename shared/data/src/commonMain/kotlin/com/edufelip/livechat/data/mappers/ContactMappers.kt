package com.edufelip.livechat.data.mappers

import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.shared.data.database.ContactEntity

fun ContactEntity.toDomain(): Contact =
    Contact(
        id = id,
        name = name,
        phoneNo = phoneNo,
        description = description,
        photo = photo,
        isRegistered = isRegistered,
        firebaseUid = firebaseUid,
    )

fun Contact.toEntity(): ContactEntity =
    ContactEntity(
        id = id,
        name = name,
        phoneNo = phoneNo,
        description = description,
        photo = photo,
        isRegistered = isRegistered,
        firebaseUid = firebaseUid,
    )
