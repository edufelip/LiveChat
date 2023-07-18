package com.project.livechat.data.db.entities

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.project.livechat.domain.models.Contact
import kotlinx.parcelize.Parcelize

@Keep
@Entity(tableName = "contact_table")
@Parcelize
data class ContactRoom(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    val name: String,
    val phoneNo: String,
    val description: String? = null,
    val photo: String? = null
) : Parcelable {
    fun toContact(): Contact {
        with (this) {
            return Contact(
                name = name,
                phoneNo = phoneNo,
                description = description,
                photo = photo
            )
        }
    }

    companion object {
        fun fromContact(contact: Contact): ContactRoom {
            return with(contact) {
                ContactRoom(
                    id = 0,
                    name = this.name,
                    phoneNo = this.phoneNo,
                    description = this.description,
                    photo = this.photo
                )
            }
        }
    }
}