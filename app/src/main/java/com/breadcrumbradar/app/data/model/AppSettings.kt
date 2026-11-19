package com.breadcrumbradar.app.data.model

enum class AppThemeMode { SYSTEM, DARK, LIGHT }

enum class MapStyleMode(val displayName: String) {
    STANDARD("Standard (OpenStreetMap)"),
    LIGHT("Clean Light"),
    DARK("Modern Dark")
}

enum class DistanceUnit(val displayName: String, val shortUnit: String) {
    METRIC("Meters & Kilometers", "m"),
    IMPERIAL("Feet & Miles", "ft")
}

enum class ArrivalRadius(val meters: Float, val label: String) {
    TIGHT(5f, "5 meters (Exact)"),
    STANDARD(10f, "10 meters (Standard)"),
    GENEROUS(20f, "20 meters (Nearby)"),
    OUTDOOR(50f, "50 meters (Field/Open)")
}

enum class GpsUpdateRate(val intervalMs: Long, val label: String) {
    ULTRA(1000L, "1 sec (High Precision)"),
    BALANCED(3000L, "3 sec (Balanced)"),
    ECO(7000L, "7 sec (Battery Saver)")
}

data class AppSettings(
    // 1. General & Map Appearance
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val mapStyle: MapStyleMode = MapStyleMode.STANDARD,
    val defaultZoomLevel: Double = 16.5,
    val autoRecenterOnMove: Boolean = true,
    val showCompassWidget: Boolean = true,
    val showZoomControls: Boolean = true,
    val preferRoadRouting: Boolean = true,

    // 2. Units & Navigation
    val distanceUnit: DistanceUnit = DistanceUnit.METRIC,
    val arrivalRadius: ArrivalRadius = ArrivalRadius.STANDARD,

    // 3. Location Settings
    val gpsUpdateRate: GpsUpdateRate = GpsUpdateRate.BALANCED,
    val batterySavingMode: Boolean = false,
    val useGooglePlayServices: Boolean = true,

    // 4. Notifications & Feedback
    val vibrateOnArrival: Boolean = true,
    val soundOnArrival: Boolean = false,
    val gpsLostWarning: Boolean = true,
    val nearbyAlerts: Boolean = false,

    // 5. Battery & Performance
    val trailLoggingIntervalSeconds: Int = 5
)
