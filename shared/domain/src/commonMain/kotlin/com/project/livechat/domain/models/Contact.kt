package com.project.livechat.domain.models

data class Contact(
    val id: Int,
    val name: String,
    val phoneNo: String,
    val description: String? = null,
    val photo: String? = null,
    val isRegistered: Boolean = false,
) {
    override fun equals(other: Any?): Boolean {
        val contact = other as? Contact ?: return false
        return name == contact.name &&
            phoneNo == contact.phoneNo &&
            description == contact.description &&
            photo == contact.photo &&
            isRegistered == contact.isRegistered
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + phoneNo.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (photo?.hashCode() ?: 0)
        result = 31 * result + isRegistered.hashCode()
        return result
    }
}
