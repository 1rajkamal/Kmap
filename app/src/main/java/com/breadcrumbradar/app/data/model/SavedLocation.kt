package com.breadcrumbradar.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_locations")
data class SavedLocation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String = LocationCategory.CUSTOM.name,
    val iconName: String = "place",
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val accuracy: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val isFavorite: Boolean = false,
    val lastVisitedTimestamp: Long = 0L,
    val customColorHex: String? = null
)
