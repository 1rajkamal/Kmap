package com.breadcrumbradar.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breadcrumbradar.app.data.model.*
import com.breadcrumbradar.app.data.repository.LocationRepository
import com.breadcrumbradar.app.data.repository.SettingsRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    val settingsFlow: StateFlow<AppSettings> = settingsRepository.settingsFlow

    fun setThemeMode(mode: AppThemeMode) {
        settingsRepository.updateSettings { it.copy(themeMode = mode) }
    }

    fun setAccentColor(hex: String) {
        settingsRepository.updateSettings { it.copy(accentColorHex = hex) }
    }

    fun setRadarTheme(theme: String) {
        settingsRepository.updateSettings { it.copy(radarTheme = theme) }
    }

    fun setRadarSpeed(speed: RadarSpeed) {
        settingsRepository.updateSettings { it.copy(radarSpeed = speed) }
    }

    fun setDistanceUnit(unit: DistanceUnit) {
        settingsRepository.updateSettings { it.copy(distanceUnit = unit) }
    }

    fun setArrivalRadius(radius: ArrivalRadius) {
        settingsRepository.updateSettings { it.copy(arrivalRadius = radius) }
    }

    fun setGpsUpdateRate(rate: GpsUpdateRate) {
        settingsRepository.updateSettings { it.copy(gpsUpdateRate = rate) }
    }

    fun setBatterySavingMode(enabled: Boolean) {
        settingsRepository.updateSettings { it.copy(batterySavingMode = enabled) }
    }

    fun setVibrateOnArrival(enabled: Boolean) {
        settingsRepository.updateSettings { it.copy(vibrateOnArrival = enabled) }
    }

    fun setSoundOnArrival(enabled: Boolean) {
        settingsRepository.updateSettings { it.copy(soundOnArrival = enabled) }
    }

    fun setGpsLostWarning(enabled: Boolean) {
        settingsRepository.updateSettings { it.copy(gpsLostWarning = enabled) }
    }

    fun setRadarGlow(enabled: Boolean) {
        settingsRepository.updateSettings { it.copy(radarGlow = enabled) }
    }

    fun setShowCircularGrid(enabled: Boolean) {
        settingsRepository.updateSettings { it.copy(showCircularGrid = enabled) }
    }

    fun setEcoRadarMode(enabled: Boolean) {
        settingsRepository.updateSettings { it.copy(ecoRadarMode = enabled) }
    }

    fun deleteAllSavedLocations(onDone: () -> Unit) {
        viewModelScope.launch {
            locationRepository.deleteAll()
            onDone()
        }
    }
}
