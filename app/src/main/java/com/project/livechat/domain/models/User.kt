package com.project.livechat.domain.models

class User (
    val name: String,
    val phoneNo: String,
    val description: String? = null,
    val photo: String? = null
)