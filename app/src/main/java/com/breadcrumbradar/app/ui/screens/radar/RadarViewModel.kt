package com.breadcrumbradar.app.ui.screens.radar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breadcrumbradar.app.data.model.AppSettings
import com.breadcrumbradar.app.data.model.SavedLocation
import com.breadcrumbradar.app.data.repository.LocationRepository
import com.breadcrumbradar.app.sensor.CompassData
import com.breadcrumbradar.app.sensor.GeoUtils
import com.breadcrumbradar.app.sensor.LocationState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RadarUiState(
    val selectedDestination: SavedLocation? = null,
    val allLocations: List<SavedLocation> = emptyList(),
    val distanceMeters: Double = 0.0,
    val distanceKm: String = "0.00 km",
    val distanceFeet: String = "0 ft",
    val distanceFormatted: String = "0 m",
    val targetBearing: Float = 0f,
    val relativeBearing: Float = 0f,
    val altitudeDeltaMeters: Double = 0.0,
    val isArrived: Boolean = false,
    val hasTriggeredArrivalHaptic: Boolean = false,
    val showRecalibrateDialog: Boolean = false
)

class RadarViewModel(
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RadarUiState())
    val uiState: StateFlow<RadarUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            locationRepository.getAllLocationsFlow().collect { locations ->
                _uiState.update { current ->
                    val selected = current.selectedDestination ?: locations.firstOrNull()
                    current.copy(
                        allLocations = locations,
                        selectedDestination = selected
                    )
                }
            }
        }
    }

    fun selectDestination(location: SavedLocation) {
        _uiState.update {
            it.copy(
                selectedDestination = location,
                isArrived = false,
                hasTriggeredArrivalHaptic = false
            )
        }
        viewModelScope.launch {
            locationRepository.markVisited(location.id)
        }
    }

    fun updateNavigation(
        locState: LocationState,
        compassData: CompassData,
        settings: AppSettings
    ) {
        val destination = _uiState.value.selectedDestination ?: return
        if (!locState.hasFix) return

        val userLat = locState.latitude
        val userLon = locState.longitude
        val userAlt = locState.altitude

        val distMeters = GeoUtils.calculateDistanceMeters(
            userLat, userLon,
            destination.latitude, destination.longitude
        )
        val (_, kmStr, ftStr) = GeoUtils.formatDistanceDetailed(distMeters)
        val formattedDist = GeoUtils.formatDistance(distMeters, settings.distanceUnit)

        val targetBearing = GeoUtils.calculateBearing(
            userLat, userLon,
            destination.latitude, destination.longitude
        )
        val relativeBearing = GeoUtils.calculateRelativeBearing(
            targetBearing, compassData.azimuthDegrees
        )

        val altDelta = if (destination.altitude != 0.0 && userAlt != 0.0) {
            destination.altitude - userAlt
        } else 0.0

        val arrivalThreshold = settings.arrivalRadius.meters
        val isArrived = distMeters <= arrivalThreshold

        _uiState.update {
            it.copy(
                distanceMeters = distMeters,
                distanceKm = kmStr,
                distanceFeet = ftStr,
                distanceFormatted = formattedDist,
                targetBearing = targetBearing,
                relativeBearing = relativeBearing,
                altitudeDeltaMeters = altDelta,
                isArrived = isArrived
            )
        }
    }

    fun setHapticTriggered() {
        _uiState.update { it.copy(hasTriggeredArrivalHaptic = true) }
    }

    fun setShowRecalibrateDialog(show: Boolean) {
        _uiState.update { it.copy(showRecalibrateDialog = show) }
    }
}
