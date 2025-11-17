package com.edufelip.livechat.shared.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OnboardingStatusDao {
    @Query("SELECT complete FROM onboarding_status WHERE id = 0")
    fun observe(): Flow<Boolean?>

    @Query("SELECT complete FROM onboarding_status WHERE id = 0")
    suspend fun current(): Boolean?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(status: OnboardingStatusEntity)
}
