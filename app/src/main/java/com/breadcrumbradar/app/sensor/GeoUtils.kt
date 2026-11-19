package com.breadcrumbradar.app.sensor

import com.breadcrumbradar.app.data.model.DistanceUnit
import kotlin.math.*

object GeoUtils {

    private const val EARTH_RADIUS_METERS = 6371000.0
    private const val METERS_TO_FEET = 3.28084
    private const val METERS_TO_MILES = 0.000621371

    /**
     * Calculates great-circle distance between two GPS coordinates using Haversine formula.
     * Returns distance in meters.
     */
    fun calculateDistanceMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    /**
     * Calculates the initial bearing from current position to destination in degrees (0..360).
     */
    fun calculateBearing(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)

        val theta = atan2(y, x)
        val bearing = (Math.toDegrees(theta) + 360.0) % 360.0
        return bearing.toFloat()
    }

    /**
     * Computes the relative bearing for the radar arrow:
     * How many degrees clockwise to point relative to the top of the phone screen.
     */
    fun calculateRelativeBearing(targetBearing: Float, currentHeading: Float): Float {
        val diff = (targetBearing - currentHeading + 360f) % 360f
        return diff
    }

    /**
     * Formats distance cleanly according to the selected unit.
     */
    fun formatDistance(distanceMeters: Double, unit: DistanceUnit): String {
        return when (unit) {
            DistanceUnit.METRIC -> {
                if (distanceMeters < 1000) {
                    "%.0f m".format(distanceMeters)
                } else {
                    "%.2f km".format(distanceMeters / 1000.0)
                }
            }
            DistanceUnit.IMPERIAL -> {
                val feet = distanceMeters * METERS_TO_FEET
                if (feet < 1000) {
                    "%.0f ft".format(feet)
                } else {
                    val miles = distanceMeters * METERS_TO_MILES
                    "%.2f mi".format(miles)
                }
            }
        }
    }

    fun formatDistanceDetailed(distanceMeters: Double): Triple<String, String, String> {
        val meters = "%.0f m".format(distanceMeters)
        val km = "%.2f km".format(distanceMeters / 1000.0)
        val feet = "%.0f ft".format(distanceMeters * METERS_TO_FEET)
        return Triple(meters, km, feet)
    }

    /**
     * Formats latitude and longitude as human-readable coordinates.
     */
    fun formatCoordinates(latitude: Double, longitude: Double): String {
        val latDir = if (latitude >= 0) "N" else "S"
        val lonDir = if (longitude >= 0) "E" else "W"
        return "%.5f° %s, %.5f° %s".format(abs(latitude), latDir, abs(longitude), lonDir)
    }

    /**
     * Converts a heading in degrees to a 16-point cardinal direction.
     */
    fun headingToCardinal(degrees: Float): String {
        val normalized = (degrees % 360 + 360) % 360
        val directions = arrayOf(
            "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
            "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"
        )
        val index = ((normalized + 11.25) / 22.5).toInt() % 16
        return directions[index]
    }
}
