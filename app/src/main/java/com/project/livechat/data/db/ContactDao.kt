package com.project.livechat.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.project.livechat.data.db.entities.ContactRoom
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contact_table")
    fun getAllContacts(): Flow<List<ContactRoom>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactRoom)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllContacts(contactsList: List<ContactRoom>)

    @Query("UPDATE contact_table SET name = :name, description = :description, photo = :photo WHERE phoneNo = :phoneNo")
    suspend fun updateContact(name: String, phoneNo: String, description: String, photo: String)

    @Query("DELETE FROM contact_table WHERE phoneNo in (:contactPhoneNoList)")
    suspend fun deleteContacts(contactPhoneNoList: List<String>)
}