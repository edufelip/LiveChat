package com.project.livechat.data.models

import com.project.livechat.domain.models.Contact

class ContactResponse(
    val id: Int,
    val name: String,
    val phoneNo: String,
    val description: String? = null,
    val photo: String? = null
) {
    fun toContact(): Contact {
        return with(this) {
            Contact(
                id = id,
                name = name,
                phoneNo = phoneNo,
                description = description,
                photo = photo
            )
        }
    }
}