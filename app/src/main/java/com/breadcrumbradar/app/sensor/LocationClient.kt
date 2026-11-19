package com.breadcrumbradar.app.sensor

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class GpsSignalState {
    NO_PERMISSION,
    DISABLED,
    SEARCHING,
    WEAK,
    GOOD,
    EXCELLENT
}

data class LocationState(
    val location: Location? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val accuracy: Float = 0f,
    val speed: Float = 0f,
    val bearing: Float = 0f,
    val timestamp: Long = 0L,
    val signalState: GpsSignalState = GpsSignalState.SEARCHING,
    val hasFix: Boolean = false
)

class LocationClient(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _locationFlow = MutableStateFlow(LocationState())
    val locationFlow: StateFlow<LocationState> = _locationFlow.asStateFlow()

    private var isTracking = false
    private var locationCallback: LocationCallback? = null

    private val fallbackListener = object : LocationListener {
        override fun onLocationChanged(loc: Location) {
            handleNewLocation(loc)
        }
        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {
            if (provider == LocationManager.GPS_PROVIDER) {
                _locationFlow.value = _locationFlow.value.copy(
                    signalState = GpsSignalState.DISABLED,
                    hasFix = false
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(intervalMs: Long = 2000L) {
        if (isTracking) return

        val isGpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
        if (!isGpsEnabled) {
            _locationFlow.value = _locationFlow.value.copy(signalState = GpsSignalState.DISABLED)
        } else {
            _locationFlow.value = _locationFlow.value.copy(signalState = GpsSignalState.SEARCHING)
        }

        try {
            // Setup Fused Location
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
                .setMinUpdateIntervalMillis(intervalMs / 2)
                .setMinUpdateDistanceMeters(0.5f)
                .build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { handleNewLocation(it) }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            ).addOnFailureListener {
                // Fallback to LocationManager if GMS fails
                startLocationManagerFallback(intervalMs)
            }

            // Also request last known location immediately
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null && _locationFlow.value.location == null) {
                    handleNewLocation(loc)
                }
            }

            isTracking = true
        } catch (e: SecurityException) {
            _locationFlow.value = _locationFlow.value.copy(signalState = GpsSignalState.NO_PERMISSION)
        } catch (e: Exception) {
            startLocationManagerFallback(intervalMs)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationManagerFallback(intervalMs: Long) {
        try {
            locationManager?.let { mgr ->
                if (mgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    mgr.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        intervalMs,
                        0.5f,
                        fallbackListener,
                        Looper.getMainLooper()
                    )
                }
                if (mgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    mgr.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        intervalMs,
                        1.0f,
                        fallbackListener,
                        Looper.getMainLooper()
                    )
                }
                isTracking = true
            }
        } catch (e: Exception) {
            // Handled
        }
    }

    fun stopLocationUpdates() {
        if (!isTracking) return
        try {
            locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
            locationManager?.removeUpdates(fallbackListener)
            isTracking = false
        } catch (e: Exception) {
            // Ignored
        }
    }

    private fun handleNewLocation(loc: Location) {
        val accuracy = loc.accuracy
        val signal = when {
            accuracy <= 6f -> GpsSignalState.EXCELLENT
            accuracy <= 15f -> GpsSignalState.GOOD
            accuracy <= 30f -> GpsSignalState.WEAK
            else -> GpsSignalState.SEARCHING
        }

        _locationFlow.value = LocationState(
            location = loc,
            latitude = loc.latitude,
            longitude = loc.longitude,
            altitude = loc.altitude,
            accuracy = accuracy,
            speed = loc.speed,
            bearing = loc.bearing,
            timestamp = loc.time,
            signalState = signal,
            hasFix = true
        )
    }
}
