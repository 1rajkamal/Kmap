package com.breadcrumbradar.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.breadcrumbradar.app.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("breadcrumb_radar_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<AppSettings> = _settingsFlow.asStateFlow()

    private fun loadSettings(): AppSettings {
        val themeStr = prefs.getString("theme_mode", AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name
        val theme = runCatching { AppThemeMode.valueOf(themeStr) }.getOrDefault(AppThemeMode.DARK)

        val unitStr = prefs.getString("distance_unit", DistanceUnit.METRIC.name) ?: DistanceUnit.METRIC.name
        val distanceUnit = runCatching { DistanceUnit.valueOf(unitStr) }.getOrDefault(DistanceUnit.METRIC)

        val speedStr = prefs.getString("radar_speed", RadarSpeed.NORMAL.name) ?: RadarSpeed.NORMAL.name
        val radarSpeed = runCatching { RadarSpeed.valueOf(speedStr) }.getOrDefault(RadarSpeed.NORMAL)

        val radiusStr = prefs.getString("arrival_radius", ArrivalRadius.STANDARD.name) ?: ArrivalRadius.STANDARD.name
        val arrivalRadius = runCatching { ArrivalRadius.valueOf(radiusStr) }.getOrDefault(ArrivalRadius.STANDARD)

        val rateStr = prefs.getString("gps_update_rate", GpsUpdateRate.BALANCED.name) ?: GpsUpdateRate.BALANCED.name
        val gpsRate = runCatching { GpsUpdateRate.valueOf(rateStr) }.getOrDefault(GpsUpdateRate.BALANCED)

        return AppSettings(
            themeMode = theme,
            accentColorHex = prefs.getString("accent_color", "#00E676") ?: "#00E676",
            radarTheme = prefs.getString("radar_theme", "EMERALD") ?: "EMERALD",
            animationIntensity = prefs.getFloat("animation_intensity", 1.0f),
            dynamicColor = prefs.getBoolean("dynamic_color", false),
            compactLayout = prefs.getBoolean("compact_layout", false),

            radarSpeed = radarSpeed,
            radarGlow = prefs.getBoolean("radar_glow", true),
            showCircularGrid = prefs.getBoolean("circular_grid", true),
            distanceUnit = distanceUnit,
            arrivalRadius = arrivalRadius,
            showHeadingDegree = prefs.getBoolean("heading_degree", true),

            gpsUpdateRate = gpsRate,
            batterySavingMode = prefs.getBoolean("battery_saver", false),
            useGooglePlayServices = prefs.getBoolean("use_gms", true),

            vibrateOnArrival = prefs.getBoolean("vibrate_arrival", true),
            soundOnArrival = prefs.getBoolean("sound_arrival", false),
            gpsLostWarning = prefs.getBoolean("gps_lost_warn", true),
            nearbyAlerts = prefs.getBoolean("nearby_alerts", false),

            ecoRadarMode = prefs.getBoolean("eco_radar", false),
            trailLoggingIntervalSeconds = prefs.getInt("trail_interval", 5)
        )
    }

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val current = _settingsFlow.value
        val updated = transform(current)
        saveSettings(updated)
        _settingsFlow.value = updated
    }

    private fun saveSettings(s: AppSettings) {
        prefs.edit()
            .putString("theme_mode", s.themeMode.name)
            .putString("accent_color", s.accentColorHex)
            .putString("radar_theme", s.radarTheme)
            .putFloat("animation_intensity", s.animationIntensity)
            .putBoolean("dynamic_color", s.dynamicColor)
            .putBoolean("compact_layout", s.compactLayout)

            .putString("radar_speed", s.radarSpeed.name)
            .putBoolean("radar_glow", s.radarGlow)
            .putBoolean("circular_grid", s.showCircularGrid)
            .putString("distance_unit", s.distanceUnit.name)
            .putString("arrival_radius", s.arrivalRadius.name)
            .putBoolean("heading_degree", s.showHeadingDegree)

            .putString("gps_update_rate", s.gpsUpdateRate.name)
            .putBoolean("battery_saver", s.batterySavingMode)
            .putBoolean("use_gms", s.useGooglePlayServices)

            .putBoolean("vibrate_arrival", s.vibrateOnArrival)
            .putBoolean("sound_arrival", s.soundOnArrival)
            .putBoolean("gps_lost_warn", s.gpsLostWarning)
            .putBoolean("nearby_alerts", s.nearbyAlerts)

            .putBoolean("eco_radar", s.ecoRadarMode)
            .putInt("trail_interval", s.trailLoggingIntervalSeconds)
            .apply()
    }
}
