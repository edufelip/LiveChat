package com.project.livechat.domain.models

data class Contact(
    val name: String,
    val phoneNo: String,
    val description: String? = null,
    val photo: String? = null
) {
    override fun equals(other: Any?): Boolean {
        (other as? Contact)?.let {
            return (
                this.name == it.name &&
                this.description == it.description &&
                this.phoneNo == it.photo
            )
        } ?: return false
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + phoneNo.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (photo?.hashCode() ?: 0)
        return result
    }
}