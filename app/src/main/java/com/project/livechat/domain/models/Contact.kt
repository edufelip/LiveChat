package com.project.livechat.domain.models

class Contact (
    val name: String,
    val phoneNo: String,
    val description: String? = null,
    val photo: String? = null
)