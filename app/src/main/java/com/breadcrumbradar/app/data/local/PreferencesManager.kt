package com.breadcrumbradar.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.breadcrumbradar.app.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("kmap_preferences", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<AppSettings> = _settingsFlow.asStateFlow()

    private fun loadSettings(): AppSettings {
        val themeStr = prefs.getString("theme_mode", AppThemeMode.LIGHT.name) ?: AppThemeMode.LIGHT.name
        val theme = runCatching { AppThemeMode.valueOf(themeStr) }.getOrDefault(AppThemeMode.LIGHT)

        val mapStyleStr = prefs.getString("map_style", MapStyleMode.STANDARD.name) ?: MapStyleMode.STANDARD.name
        val mapStyle = runCatching { MapStyleMode.valueOf(mapStyleStr) }.getOrDefault(MapStyleMode.STANDARD)

        val unitStr = prefs.getString("distance_unit", DistanceUnit.METRIC.name) ?: DistanceUnit.METRIC.name
        val distanceUnit = runCatching { DistanceUnit.valueOf(unitStr) }.getOrDefault(DistanceUnit.METRIC)

        val radiusStr = prefs.getString("arrival_radius", ArrivalRadius.STANDARD.name) ?: ArrivalRadius.STANDARD.name
        val arrivalRadius = runCatching { ArrivalRadius.valueOf(radiusStr) }.getOrDefault(ArrivalRadius.STANDARD)

        val rateStr = prefs.getString("gps_update_rate", GpsUpdateRate.BALANCED.name) ?: GpsUpdateRate.BALANCED.name
        val gpsRate = runCatching { GpsUpdateRate.valueOf(rateStr) }.getOrDefault(GpsUpdateRate.BALANCED)

        return AppSettings(
            themeMode = theme,
            mapStyle = mapStyle,
            defaultZoomLevel = prefs.getFloat("default_zoom", 16.5f).toDouble(),
            autoRecenterOnMove = prefs.getBoolean("auto_recenter", true),
            showCompassWidget = prefs.getBoolean("show_compass", true),
            showZoomControls = prefs.getBoolean("show_zoom_controls", true),
            preferRoadRouting = prefs.getBoolean("prefer_road_routing", true),

            distanceUnit = distanceUnit,
            arrivalRadius = arrivalRadius,

            gpsUpdateRate = gpsRate,
            batterySavingMode = prefs.getBoolean("battery_saver", false),
            useGooglePlayServices = prefs.getBoolean("use_gms", true),

            vibrateOnArrival = prefs.getBoolean("vibrate_arrival", true),
            soundOnArrival = prefs.getBoolean("sound_arrival", false),
            gpsLostWarning = prefs.getBoolean("gps_lost_warn", true),
            nearbyAlerts = prefs.getBoolean("nearby_alerts", false),

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
            .putString("map_style", s.mapStyle.name)
            .putFloat("default_zoom", s.defaultZoomLevel.toFloat())
            .putBoolean("auto_recenter", s.autoRecenterOnMove)
            .putBoolean("show_compass", s.showCompassWidget)
            .putBoolean("show_zoom_controls", s.showZoomControls)
            .putBoolean("prefer_road_routing", s.preferRoadRouting)

            .putString("distance_unit", s.distanceUnit.name)
            .putString("arrival_radius", s.arrivalRadius.name)

            .putString("gps_update_rate", s.gpsUpdateRate.name)
            .putBoolean("battery_saver", s.batterySavingMode)
            .putBoolean("use_gms", s.useGooglePlayServices)

            .putBoolean("vibrate_arrival", s.vibrateOnArrival)
            .putBoolean("sound_arrival", s.soundOnArrival)
            .putBoolean("gps_lost_warn", s.gpsLostWarning)
            .putBoolean("nearby_alerts", s.nearbyAlerts)

            .putInt("trail_interval", s.trailLoggingIntervalSeconds)
            .apply()
    }
}
