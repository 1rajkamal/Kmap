package com.breadcrumbradar.app.data.repository

import com.breadcrumbradar.app.data.local.LocationDao
import com.breadcrumbradar.app.data.model.SavedLocation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class LocationRepository(private val locationDao: LocationDao) {

    private val gson = Gson()

    fun getAllLocationsFlow(): Flow<List<SavedLocation>> = locationDao.getAllLocationsFlow()

    fun getFavoriteLocationsFlow(): Flow<List<SavedLocation>> = locationDao.getFavoriteLocationsFlow()

    fun getRecentLocationsFlow(limit: Int = 5): Flow<List<SavedLocation>> =
        locationDao.getRecentLocationsFlow(limit)

    fun getLocationCountFlow(): Flow<Int> = locationDao.getLocationCountFlow()

    suspend fun getLocationById(id: Long): SavedLocation? = withContext(Dispatchers.IO) {
        locationDao.getLocationById(id)
    }

    suspend fun insertLocation(location: SavedLocation): Long = withContext(Dispatchers.IO) {
        locationDao.insertLocation(location)
    }

    suspend fun updateLocation(location: SavedLocation) = withContext(Dispatchers.IO) {
        locationDao.updateLocation(location)
    }

    suspend fun deleteLocation(location: SavedLocation) = withContext(Dispatchers.IO) {
        locationDao.deleteLocation(location)
    }

    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        locationDao.deleteById(id)
    }

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        locationDao.deleteAll()
    }

    suspend fun toggleFavorite(id: Long, currentFavorite: Boolean) = withContext(Dispatchers.IO) {
        locationDao.setFavorite(id, !currentFavorite)
    }

    suspend fun markVisited(id: Long) = withContext(Dispatchers.IO) {
        locationDao.updateLastVisited(id, System.currentTimeMillis())
    }

    suspend fun exportToJson(): String = withContext(Dispatchers.IO) {
        val list = locationDao.getAllLocations()
        gson.toJson(list)
    }

    suspend fun importFromJson(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val listType = object : TypeToken<List<SavedLocation>>() {}.type
            val imported: List<SavedLocation> = gson.fromJson(jsonString, listType) ?: emptyList()
            if (imported.isNotEmpty()) {
                val prepared = imported.map { it.copy(id = 0) } // create new entries
                locationDao.insertAll(prepared)
            }
            imported.size
        }
    }
}
