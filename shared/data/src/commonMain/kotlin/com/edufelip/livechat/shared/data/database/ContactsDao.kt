package com.edufelip.livechat.shared.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactsDao {
    @Query("SELECT * FROM contacts ORDER BY name")
    fun observeContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts ORDER BY name")
    suspend fun getAll(): List<ContactEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(contact: ContactEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(contacts: List<ContactEntity>): List<Long>

    @Query(
        """
        UPDATE contacts
        SET name = :name,
            description = :description,
            photo = :photo,
            is_registered = :isRegistered,
            firebase_uid = :firebaseUid
        WHERE phone_no = :phoneNo
        """,
    )
    suspend fun updateContactByPhone(
        name: String,
        description: String?,
        photo: String?,
        isRegistered: Boolean,
        firebaseUid: String?,
        phoneNo: String,
    ): Int

    @Query(
        """
        UPDATE contacts
        SET name = :name,
            description = :description,
            photo = :photo,
            is_registered = :isRegistered,
            firebase_uid = :firebaseUid,
            phone_no = :phoneNo
        WHERE id = :id
        """,
    )
    suspend fun updateContactById(
        id: Long,
        name: String,
        description: String?,
        photo: String?,
        isRegistered: Boolean,
        firebaseUid: String?,
        phoneNo: String,
    ): Int

    @Query("DELETE FROM contacts WHERE phone_no IN (:phones)")
    suspend fun deleteContactsByPhone(phones: List<String>)
}
