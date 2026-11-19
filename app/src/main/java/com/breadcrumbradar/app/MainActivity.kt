package com.breadcrumbradar.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.breadcrumbradar.app.data.local.AppDatabase
import com.breadcrumbradar.app.data.local.PreferencesManager
import com.breadcrumbradar.app.data.repository.BreadcrumbRepository
import com.breadcrumbradar.app.data.repository.LocationRepository
import com.breadcrumbradar.app.data.repository.SettingsRepository
import com.breadcrumbradar.app.sensor.CompassSensorManager
import com.breadcrumbradar.app.sensor.LocationClient
import com.breadcrumbradar.app.ui.navigation.AppNavigation
import com.breadcrumbradar.app.ui.screens.home.HomeViewModel
import com.breadcrumbradar.app.ui.screens.places.PlacesViewModel
import com.breadcrumbradar.app.ui.screens.radar.RadarViewModel
import com.breadcrumbradar.app.ui.screens.settings.SettingsViewModel
import com.breadcrumbradar.app.ui.screens.trail.TrailViewModel
import com.breadcrumbradar.app.ui.theme.BreadcrumbRadarTheme
import com.breadcrumbradar.app.ui.theme.ElectricEmerald

class MainActivity : ComponentActivity() {

    private lateinit var locationClient: LocationClient
    private lateinit var compassSensorManager: CompassSensorManager

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineLocationGranted || coarseLocationGranted) {
            locationClient.startLocationUpdates()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Local Storage & Repositories
        val database = AppDatabase.getDatabase(applicationContext)
        val locationRepository = LocationRepository(database.locationDao())
        val breadcrumbRepository = BreadcrumbRepository(database.breadcrumbDao())
        val preferencesManager = PreferencesManager(applicationContext)
        val settingsRepository = SettingsRepository(preferencesManager)

        // Initialize Sensors
        locationClient = LocationClient(applicationContext)
        compassSensorManager = CompassSensorManager(applicationContext)

        // ViewModels
        val homeViewModel = HomeViewModel(locationRepository)
        val radarViewModel = RadarViewModel(locationRepository)
        val placesViewModel = PlacesViewModel(locationRepository)
        val trailViewModel = TrailViewModel(breadcrumbRepository)
        val settingsViewModel = SettingsViewModel(settingsRepository, locationRepository)

        // Check Permissions
        checkAndRequestPermissions()

        setContent {
            val settings by settingsViewModel.settingsFlow.collectAsState()
            val locationState by locationClient.locationFlow.collectAsState()
            val compassData by compassSensorManager.compassFlow.collectAsState()
            val allLocations by locationRepository.getAllLocationsFlow().collectAsState(initial = emptyList())

            val accentColor = remember(settings.radarTheme) {
                when (settings.radarTheme) {
                    "CYAN" -> Color(0xFF00E5FF)
                    "AMBER" -> Color(0xFFFFB300)
                    "MONOCHROME" -> Color(0xFFE2E8F0)
                    else -> ElectricEmerald
                }
            }

            BreadcrumbRadarTheme(
                themeMode = settings.themeMode,
                accentColor = accentColor
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    AppNavigation(
                        homeViewModel = homeViewModel,
                        radarViewModel = radarViewModel,
                        placesViewModel = placesViewModel,
                        trailViewModel = trailViewModel,
                        settingsViewModel = settingsViewModel,
                        locationState = locationState,
                        compassData = compassData,
                        settings = settings,
                        allLocations = allLocations
                    )
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        } else {
            locationClient.startLocationUpdates()
        }
    }

    override fun onResume() {
        super.onResume()
        compassSensorManager.startListening()
        val hasFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            locationClient.startLocationUpdates()
        }
    }

    override fun onPause() {
        compassSensorManager.stopListening()
        locationClient.stopLocationUpdates()
        super.onPause()
    }
}
