package com.breadcrumbradar.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breadcrumbradar.app.data.model.AppSettings
import com.breadcrumbradar.app.data.model.DistanceUnit
import com.breadcrumbradar.app.data.model.SavedLocation
import com.breadcrumbradar.app.data.repository.LocationRepository
import com.breadcrumbradar.app.sensor.GeoUtils
import com.breadcrumbradar.app.sensor.LocationState
import com.breadcrumbradar.app.sensor.RouteResult
import com.breadcrumbradar.app.sensor.RoutingClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

data class HomeMapUiState(
    val allLocations: List<SavedLocation> = emptyList(),
    val selectedLocation: SavedLocation? = null,
    val isNavigating: Boolean = false,
    val liveDistanceMeters: Double = 0.0,
    val formattedDistance: String = "",
    val bearingDegrees: Float = 0f,
    val routeResult: RouteResult? = null,
    val isArrived: Boolean = false,
    val cameraTarget: GeoPoint? = null,
    val showMarkDialog: Boolean = false,
    val markLocationCoordinates: Pair<Double, Double>? = null
)

class HomeViewModel(
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeMapUiState())
    val uiState: StateFlow<HomeMapUiState> = _uiState.asStateFlow()

    private var currentLocationState = LocationState()
    private var currentSettings = AppSettings()
    private var routingJob: Job? = null

    init {
        viewModelScope.launch {
            locationRepository.getAllLocationsFlow().collect { list ->
                _uiState.update { current ->
                    // Keep selected location updated if it still exists
                    val updatedSelected = list.find { it.id == current.selectedLocation?.id }
                    current.copy(
                        allLocations = list,
                        selectedLocation = updatedSelected ?: current.selectedLocation
                    )
                }
                updateLiveDistance()
            }
        }
    }

    fun updateCurrentLocation(locState: LocationState, settings: AppSettings) {
        currentLocationState = locState
        currentSettings = settings
        updateLiveDistance()
    }

    fun selectLocation(location: SavedLocation?) {
        _uiState.update {
            it.copy(
                selectedLocation = location,
                cameraTarget = location?.let { l -> GeoPoint(l.latitude, l.longitude) }
            )
        }
        updateLiveDistance()
        fetchRouteIfEligible()
    }

    fun startNavigation() {
        val target = _uiState.value.selectedLocation ?: return
        _uiState.update {
            it.copy(
                isNavigating = true,
                isArrived = false
            )
        }
        viewModelScope.launch {
            locationRepository.markVisited(target.id)
        }
        fetchRouteIfEligible()
    }

    fun stopNavigation() {
        _uiState.update {
            it.copy(
                isNavigating = false,
                isArrived = false
            )
        }
    }

    fun recenterOnUser() {
        if (currentLocationState.hasFix) {
            _uiState.update {
                it.copy(cameraTarget = GeoPoint(currentLocationState.latitude, currentLocationState.longitude))
            }
        }
    }

    fun clearCameraTarget() {
        _uiState.update { it.copy(cameraTarget = null) }
    }

    fun openMarkDialog(coordinates: Pair<Double, Double>? = null) {
        _uiState.update {
            it.copy(
                showMarkDialog = true,
                markLocationCoordinates = coordinates
            )
        }
    }

    fun closeMarkDialog() {
        _uiState.update {
            it.copy(
                showMarkDialog = false,
                markLocationCoordinates = null
            )
        }
    }

    fun saveLocation(location: SavedLocation) {
        viewModelScope.launch {
            val id = locationRepository.insertLocation(location)
            closeMarkDialog()
            val saved = locationRepository.getLocationById(id)
            if (saved != null) {
                selectLocation(saved)
            }
        }
    }

    fun deleteLocation(location: SavedLocation) {
        viewModelScope.launch {
            locationRepository.deleteLocation(location)
            if (_uiState.value.selectedLocation?.id == location.id) {
                _uiState.update { it.copy(selectedLocation = null, isNavigating = false) }
            }
        }
    }

    private fun updateLiveDistance() {
        val target = _uiState.value.selectedLocation ?: return
        if (!currentLocationState.hasFix) return

        val userLat = currentLocationState.latitude
        val userLon = currentLocationState.longitude

        val dist = GeoUtils.calculateDistanceMeters(
            userLat, userLon,
            target.latitude, target.longitude
        )
        val formatted = GeoUtils.formatDistance(dist, currentSettings.distanceUnit)
        val bearing = GeoUtils.calculateBearing(
            userLat, userLon,
            target.latitude, target.longitude
        )

        val isArrived = dist <= currentSettings.arrivalRadius.meters

        _uiState.update {
            it.copy(
                liveDistanceMeters = dist,
                formattedDistance = formatted,
                bearingDegrees = bearing,
                isArrived = isArrived
            )
        }
    }

    private fun fetchRouteIfEligible() {
        val target = _uiState.value.selectedLocation ?: return
        if (!currentLocationState.hasFix) return

        routingJob?.cancel()
        routingJob = viewModelScope.launch {
            val result = RoutingClient.fetchRoute(
                currentLocationState.latitude, currentLocationState.longitude,
                target.latitude, target.longitude
            )
            _uiState.update { it.copy(routeResult = result) }
        }
    }
}
