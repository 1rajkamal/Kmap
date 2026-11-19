package com.breadcrumbradar.app.sensor

import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class RouteResult(
    val points: List<GeoPoint>,
    val distanceMeters: Double,
    val isRoadRoute: Boolean
)

object RoutingClient {

    /**
     * Attempts to query free OpenStreetMap OSRM pedestrian route.
     * If offline or fails, falls back gracefully to geodesic straight line.
     */
    suspend fun fetchRoute(
        startLat: Double, startLon: Double,
        destLat: Double, destLon: Double
    ): RouteResult = withContext(Dispatchers.IO) {
        val straightLineDistance = GeoUtils.calculateDistanceMeters(startLat, startLon, destLat, destLon)
        val directFallback = RouteResult(
            points = listOf(GeoPoint(startLat, startLon), GeoPoint(destLat, destLon)),
            distanceMeters = straightLineDistance,
            isRoadRoute = false
        )

        try {
            val urlString = String.format(
                Locale.US,
                "https://router.project-osrm.org/route/v1/walking/%.6f,%.6f;%%.6f,%.6f?overview=full&geometries=geojson",
                startLon, startLat, destLon, destLat
            )

            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 2500
                readTimeout = 2500
                requestMethod = "GET"
                setRequestProperty("User-Agent", "Kmap/1.0 (Android)")
            }

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val jsonStr = reader.use { it.readText() }
                conn.disconnect()

                val json = JsonParser.parseString(jsonStr).asJsonObject
                if (json.get("code")?.asString == "Ok") {
                    val routes = json.getAsJsonArray("routes")
                    if (routes != null && routes.size() > 0) {
                        val routeObj = routes.get(0).asJsonObject
                        val distance = routeObj.get("distance")?.asDouble ?: straightLineDistance
                        val coordinates = routeObj.getAsJsonObject("geometry")?.getAsJsonArray("coordinates")

                        if (coordinates != null && coordinates.size() > 0) {
                            val geoPoints = mutableListOf<GeoPoint>()
                            for (i in 0 until coordinates.size()) {
                                val coord = coordinates.get(i).asJsonArray
                                val lon = coord.get(0).asDouble
                                val lat = coord.get(1).asDouble
                                geoPoints.add(GeoPoint(lat, lon))
                            }
                            return@withContext RouteResult(
                                points = geoPoints,
                                distanceMeters = distance,
                                isRoadRoute = true
                            )
                        }
                    }
                }
            }
            directFallback
        } catch (e: Exception) {
            // Offline or timeout, use direct line
            directFallback
        }
    }
}
