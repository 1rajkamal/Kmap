package com.breadcrumbradar.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trail_sessions")
data class TrailSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = 0L,
    val totalDistanceMeters: Double = 0.0,
    val pointCount: Int = 0,
    val isActive: Boolean = true,
    val isPaused: Boolean = false
)
