package com.breadcrumbradar.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breadcrumbradar.app.data.model.DistanceUnit
import com.breadcrumbradar.app.data.model.SavedLocation
import com.breadcrumbradar.app.data.repository.LocationRepository
import com.breadcrumbradar.app.sensor.GeoUtils
import com.breadcrumbradar.app.sensor.LocationState
import com.breadcrumbradar.app.ui.screens.places.SavedLocationWithDistance
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val savedLocationsCount: Int = 0,
    val recentLocations: List<SavedLocationWithDistance> = emptyList(),
    val allLocationsWithDistance: List<SavedLocationWithDistance> = emptyList(),
    val activeDestination: SavedLocation? = null
)

class HomeViewModel(
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var rawLocations: List<SavedLocation> = emptyList()
    private var currentLocationState = LocationState()
    private var currentDistanceUnit = DistanceUnit.METRIC

    init {
        viewModelScope.launch {
            locationRepository.getAllLocationsFlow().collect { list ->
                rawLocations = list
                updateCalculations()
            }
        }
        viewModelScope.launch {
            locationRepository.getLocationCountFlow().collect { count ->
                _uiState.update { it.copy(savedLocationsCount = count) }
            }
        }
    }

    fun updateCurrentLocation(locState: LocationState, unit: DistanceUnit) {
        currentLocationState = locState
        currentDistanceUnit = unit
        updateCalculations()
    }

    private fun updateCalculations() {
        val userLat = currentLocationState.latitude
        val userLon = currentLocationState.longitude
        val hasFix = currentLocationState.hasFix

        val withDistance = rawLocations.map { loc ->
            val dist = if (hasFix) {
                GeoUtils.calculateDistanceMeters(userLat, userLon, loc.latitude, loc.longitude)
            } else 0.0
            val formatted = if (hasFix) GeoUtils.formatDistance(dist, currentDistanceUnit) else "GPS required"
            SavedLocationWithDistance(loc, dist, formatted)
        }

        val nearest = withDistance.sortedBy { it.distanceMeters }
        val recent = withDistance.sortedByDescending { it.location.timestamp }.take(4)

        _uiState.update {
            it.copy(
                allLocationsWithDistance = nearest,
                recentLocations = recent,
                activeDestination = it.activeDestination ?: rawLocations.firstOrNull()
            )
        }
    }

    fun saveLocation(location: SavedLocation) {
        viewModelScope.launch {
            locationRepository.insertLocation(location)
        }
    }
}
