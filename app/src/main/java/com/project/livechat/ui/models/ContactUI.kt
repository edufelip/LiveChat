package com.project.livechat.ui.models

import com.project.livechat.domain.models.Contact

data class ContactUI(
    val id: Int,
    val name: String,
    val phoneNo: String,
    val description: String? = null,
    val photo: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        (other as? ContactUI)?.let {
            return (
                    this.id == it.id &&
                            this.name == it.name &&
                            this.description == it.description &&
                            this.phoneNo == it.photo
                    )
        } ?: return false
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + phoneNo.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (photo?.hashCode() ?: 0)
        return result
    }
}

fun Contact.toContactUI(): ContactUI {
    return ContactUI(
        id = this.id,
        name = this.name,
        phoneNo = this.phoneNo,
        description = this.description,
        photo = this.photo,
    )
}