package com.breadcrumbradar.app.ui.screens.places

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breadcrumbradar.app.data.model.DistanceUnit
import com.breadcrumbradar.app.data.model.LocationCategory
import com.breadcrumbradar.app.data.model.SavedLocation
import com.breadcrumbradar.app.data.repository.LocationRepository
import com.breadcrumbradar.app.sensor.GeoUtils
import com.breadcrumbradar.app.sensor.LocationState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PlacesUiState(
    val locations: List<SavedLocation> = emptyList(),
    val filteredLocations: List<SavedLocationWithDistance> = emptyList(),
    val selectedCategory: LocationCategory? = null,
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.NEAREST,
    val isLoading: Boolean = false,
    val message: String? = null
)

data class SavedLocationWithDistance(
    val location: SavedLocation,
    val distanceMeters: Double = 0.0,
    val formattedDistance: String = ""
)

enum class SortOption(val displayName: String) {
    NEAREST("Nearest First"),
    NEWEST("Newest"),
    NAME("Name (A-Z)"),
    FAVORITES("Favorites First")
}

class PlacesViewModel(
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlacesUiState())
    val uiState: StateFlow<PlacesUiState> = _uiState.asStateFlow()

    private var currentLocationState = LocationState()
    private var currentDistanceUnit = DistanceUnit.METRIC

    init {
        loadLocations()
    }

    fun updateCurrentLocation(locState: LocationState, unit: DistanceUnit) {
        currentLocationState = locState
        currentDistanceUnit = unit
        applyFiltersAndSort()
    }

    private fun loadLocations() {
        viewModelScope.launch {
            locationRepository.getAllLocationsFlow().collect { list ->
                _uiState.update { it.copy(locations = list) }
                applyFiltersAndSort()
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFiltersAndSort()
    }

    fun onCategorySelected(category: LocationCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
        applyFiltersAndSort()
    }

    fun onSortOptionChanged(option: SortOption) {
        _uiState.update { it.copy(sortOption = option) }
        applyFiltersAndSort()
    }

    private fun applyFiltersAndSort() {
        val state = _uiState.value
        val userLat = currentLocationState.latitude
        val userLon = currentLocationState.longitude
        val hasFix = currentLocationState.hasFix

        var list = state.locations

        // Category filter
        if (state.selectedCategory != null) {
            list = list.filter { it.category == state.selectedCategory.name }
        }

        // Search query
        if (state.searchQuery.isNotBlank()) {
            val q = state.searchQuery.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                it.notes.lowercase().contains(q) ||
                it.category.lowercase().contains(q)
            }
        }

        // Map with calculated distances
        val withDistance = list.map { loc ->
            val dist = if (hasFix) {
                GeoUtils.calculateDistanceMeters(userLat, userLon, loc.latitude, loc.longitude)
            } else 0.0
            val formatted = if (hasFix) GeoUtils.formatDistance(dist, currentDistanceUnit) else "GPS required"
            SavedLocationWithDistance(loc, dist, formatted)
        }

        // Sort
        val sorted = when (state.sortOption) {
            SortOption.NEAREST -> withDistance.sortedBy { it.distanceMeters }
            SortOption.NEWEST -> withDistance.sortedByDescending { it.location.timestamp }
            SortOption.NAME -> withDistance.sortedBy { it.location.name.lowercase() }
            SortOption.FAVORITES -> withDistance.sortedWith(
                compareByDescending<SavedLocationWithDistance> { it.location.isFavorite }
                    .thenBy { it.distanceMeters }
            )
        }

        _uiState.update { it.copy(filteredLocations = sorted) }
    }

    fun toggleFavorite(location: SavedLocation) {
        viewModelScope.launch {
            locationRepository.toggleFavorite(location.id, location.isFavorite)
        }
    }

    fun deleteLocation(location: SavedLocation) {
        viewModelScope.launch {
            locationRepository.deleteLocation(location)
            _uiState.update { it.copy(message = "Deleted ${location.name}") }
        }
    }

    fun saveLocation(location: SavedLocation) {
        viewModelScope.launch {
            if (location.id == 0L) {
                locationRepository.insertLocation(location)
                _uiState.update { it.copy(message = "Location saved: ${location.name}") }
            } else {
                locationRepository.updateLocation(location)
                _uiState.update { it.copy(message = "Updated ${location.name}") }
            }
        }
    }

    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = locationRepository.exportToJson()
            onResult(json)
        }
    }

    fun importBackup(jsonString: String) {
        viewModelScope.launch {
            val result = locationRepository.importFromJson(jsonString)
            result.onSuccess { count ->
                _uiState.update { it.copy(message = "Successfully imported $count locations") }
            }.onFailure {
                _uiState.update { it.copy(message = "Failed to import backup: Invalid JSON") }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
