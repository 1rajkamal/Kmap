package com.breadcrumbradar.app.data.model

enum class AppThemeMode { SYSTEM, DARK, LIGHT }
enum class DistanceUnit(val displayName: String, val shortUnit: String) {
    METRIC("Meters & Kilometers", "m"),
    IMPERIAL("Feet & Miles", "ft")
}
enum class RadarSpeed(val displayName: String, val durationMs: Int) {
    FAST("Fast (1.5s)", 1500),
    NORMAL("Normal (2.5s)", 2500),
    SLOW("Relaxed (4s)", 4000),
    STATIC("Static (No Sweep)", 0)
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
    // 1. Appearance
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val accentColorHex: String = "#00E676", // Electric Emerald
    val radarTheme: String = "EMERALD", // EMERALD, CYAN, AMBER, MONOCHROME
    val animationIntensity: Float = 1.0f,
    val dynamicColor: Boolean = false,
    val compactLayout: Boolean = false,

    // 2. Radar Settings
    val radarSpeed: RadarSpeed = RadarSpeed.NORMAL,
    val radarGlow: Boolean = true,
    val showCircularGrid: Boolean = true,
    val distanceUnit: DistanceUnit = DistanceUnit.METRIC,
    val arrivalRadius: ArrivalRadius = ArrivalRadius.STANDARD,
    val showHeadingDegree: Boolean = true,

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
    val ecoRadarMode: Boolean = false,
    val trailLoggingIntervalSeconds: Int = 5
)
