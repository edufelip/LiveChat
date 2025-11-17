package com.edufelip.livechat.shared.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "phone_no")
    val phoneNo: String,
    val description: String?,
    val photo: String?,
    @ColumnInfo(name = "is_registered")
    val isRegistered: Boolean,
    @ColumnInfo(name = "firebase_uid")
    val firebaseUid: String?,
)
