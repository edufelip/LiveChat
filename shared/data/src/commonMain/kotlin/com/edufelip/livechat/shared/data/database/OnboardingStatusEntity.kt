package com.edufelip.livechat.shared.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "onboarding_status")
data class OnboardingStatusEntity(
    @PrimaryKey
    val id: Int = 0,
    val complete: Boolean = false,
    @ColumnInfo(name = "welcome_seen")
    val welcomeSeen: Boolean = false,
)
