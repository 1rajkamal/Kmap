package com.breadcrumbradar.app.data.local

import androidx.room.*
import com.breadcrumbradar.app.data.model.SavedLocation
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {

    @Query("SELECT * FROM saved_locations ORDER BY timestamp DESC")
    fun getAllLocationsFlow(): Flow<List<SavedLocation>>

    @Query("SELECT * FROM saved_locations ORDER BY timestamp DESC")
    suspend fun getAllLocations(): List<SavedLocation>

    @Query("SELECT * FROM saved_locations WHERE id = :id LIMIT 1")
    suspend fun getLocationById(id: Long): SavedLocation?

    @Query("SELECT * FROM saved_locations WHERE id = :id LIMIT 1")
    fun getLocationFlow(id: Long): Flow<SavedLocation?>

    @Query("SELECT * FROM saved_locations WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteLocationsFlow(): Flow<List<SavedLocation>>

    @Query("SELECT * FROM saved_locations ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLocationsFlow(limit: Int = 5): Flow<List<SavedLocation>>

    @Query("SELECT COUNT(*) FROM saved_locations")
    fun getLocationCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: SavedLocation): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(locations: List<SavedLocation>): List<Long>

    @Update
    suspend fun updateLocation(location: SavedLocation)

    @Delete
    suspend fun deleteLocation(location: SavedLocation)

    @Query("DELETE FROM saved_locations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM saved_locations")
    suspend fun deleteAll()

    @Query("UPDATE saved_locations SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE saved_locations SET lastVisitedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateLastVisited(id: Long, timestamp: Long = System.currentTimeMillis())
}
