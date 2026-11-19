package com.breadcrumbradar.app.ui.screens.trail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breadcrumbradar.app.data.model.BreadcrumbPoint
import com.breadcrumbradar.app.data.model.DistanceUnit
import com.breadcrumbradar.app.data.model.TrailSession
import com.breadcrumbradar.app.data.repository.BreadcrumbRepository
import com.breadcrumbradar.app.sensor.GeoUtils
import com.breadcrumbradar.app.sensor.LocationState
import com.breadcrumbradar.app.service.BreadcrumbService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TrailUiState(
    val activeSession: TrailSession? = null,
    val trailPoints: List<BreadcrumbPoint> = emptyList(),
    val totalDistanceMeters: Double = 0.0,
    val formattedDistance: String = "0 m",
    val elapsedTimeSeconds: Long = 0L,
    val currentSpeedKmH: Float = 0f,
    val isReverseNavigationActive: Boolean = false,
    val reverseTargetPoint: BreadcrumbPoint? = null
)

class TrailViewModel(
    private val breadcrumbRepository: BreadcrumbRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrailUiState())
    val uiState: StateFlow<TrailUiState> = _uiState.asStateFlow()

    private var currentDistanceUnit = DistanceUnit.METRIC

    init {
        viewModelScope.launch {
            breadcrumbRepository.activeSessionFlow.collect { session ->
                _uiState.update { it.copy(activeSession = session) }
                if (session != null) {
                    loadPoints(session.id)
                } else {
                    _uiState.update { it.copy(trailPoints = emptyList(), totalDistanceMeters = 0.0) }
                }
            }
        }
    }

    fun updateDistanceUnit(unit: DistanceUnit) {
        currentDistanceUnit = unit
    }

    private fun loadPoints(sessionId: Long) {
        viewModelScope.launch {
            breadcrumbRepository.getPointsForSessionFlow(sessionId).collect { points ->
                var dist = 0.0
                for (i in 0 until points.size - 1) {
                    val p1 = points[i]
                    val p2 = points[i + 1]
                    dist += GeoUtils.calculateDistanceMeters(p1.latitude, p1.longitude, p2.latitude, p2.longitude)
                }
                val formatted = GeoUtils.formatDistance(dist, currentDistanceUnit)
                _uiState.update {
                    it.copy(
                        trailPoints = points,
                        totalDistanceMeters = dist,
                        formattedDistance = formatted
                    )
                }
            }
        }
    }

    fun startTracking(context: Context, title: String = "Trail Session") {
        viewModelScope.launch {
            val session = breadcrumbRepository.startNewSession(title)
            BreadcrumbService.start(context, session.id)
        }
    }

    fun pauseTracking(context: Context) {
        val s = _uiState.value.activeSession ?: return
        viewModelScope.launch {
            breadcrumbRepository.pauseSession(s.id)
            BreadcrumbService.pause(context)
        }
    }

    fun resumeTracking(context: Context) {
        val s = _uiState.value.activeSession ?: return
        viewModelScope.launch {
            breadcrumbRepository.resumeSession(s.id)
            BreadcrumbService.resume(context)
        }
    }

    fun stopTracking(context: Context) {
        val s = _uiState.value.activeSession ?: return
        viewModelScope.launch {
            breadcrumbRepository.stopSession(s.id)
            BreadcrumbService.stop(context)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            breadcrumbRepository.clearAllTrailData()
        }
    }

    fun toggleReverseNavigation() {
        _uiState.update { current ->
            val nextState = !current.isReverseNavigationActive
            val target = if (nextState) current.trailPoints.firstOrNull() else null
            current.copy(
                isReverseNavigationActive = nextState,
                reverseTargetPoint = target
            )
        }
    }
}
