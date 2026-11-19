package com.breadcrumbradar.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.breadcrumbradar.app.data.model.AppSettings
import com.breadcrumbradar.app.data.model.SavedLocation
import com.breadcrumbradar.app.sensor.CompassData
import com.breadcrumbradar.app.sensor.LocationState
import com.breadcrumbradar.app.ui.components.BottomNavBar
import com.breadcrumbradar.app.ui.components.NavScreen
import com.breadcrumbradar.app.ui.screens.home.HomeScreen
import com.breadcrumbradar.app.ui.screens.home.HomeViewModel
import com.breadcrumbradar.app.ui.screens.map.OfflineMapScreen
import com.breadcrumbradar.app.ui.screens.places.MyPlacesScreen
import com.breadcrumbradar.app.ui.screens.places.PlacesViewModel
import com.breadcrumbradar.app.ui.screens.radar.RadarScreen
import com.breadcrumbradar.app.ui.screens.radar.RadarViewModel
import com.breadcrumbradar.app.ui.screens.settings.SettingsScreen
import com.breadcrumbradar.app.ui.screens.settings.SettingsViewModel
import com.breadcrumbradar.app.ui.screens.trail.BreadcrumbTrailScreen
import com.breadcrumbradar.app.ui.screens.trail.TrailViewModel

@Composable
fun AppNavigation(
    homeViewModel: HomeViewModel,
    radarViewModel: RadarViewModel,
    placesViewModel: PlacesViewModel,
    trailViewModel: TrailViewModel,
    settingsViewModel: SettingsViewModel,
    locationState: LocationState,
    compassData: CompassData,
    settings: AppSettings,
    allLocations: List<SavedLocation>
) {
    var currentScreen by remember { mutableStateOf(NavScreen.HOME) }
    var isMapOpen by remember { mutableStateOf(false) }

    val radarUiState by radarViewModel.uiState.collectAsState()

    if (isMapOpen) {
        OfflineMapScreen(
            savedLocations = allLocations,
            selectedDestination = radarUiState.selectedDestination,
            locationState = locationState,
            onBack = { isMapOpen = false }
        )
    } else {
        Scaffold(
            bottomBar = {
                BottomNavBar(
                    currentScreen = currentScreen,
                    onScreenSelected = { currentScreen = it }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (currentScreen) {
                    NavScreen.HOME -> HomeScreen(
                        viewModel = homeViewModel,
                        locationState = locationState,
                        compassData = compassData,
                        settings = settings,
                        onNavigateToRadarWithTarget = { target ->
                            radarViewModel.selectDestination(target)
                            currentScreen = NavScreen.RADAR
                        },
                        onNavigateToRadarDefault = {
                            currentScreen = NavScreen.RADAR
                        },
                        onNavigateToPlaces = {
                            currentScreen = NavScreen.PLACES
                        },
                        onNavigateToTrail = {
                            currentScreen = NavScreen.TRAIL
                        }
                    )
                    NavScreen.RADAR -> RadarScreen(
                        viewModel = radarViewModel,
                        locationState = locationState,
                        compassData = compassData,
                        settings = settings,
                        onNavigateToPlaces = {
                            currentScreen = NavScreen.PLACES
                        }
                    )
                    NavScreen.PLACES -> MyPlacesScreen(
                        viewModel = placesViewModel,
                        locationState = locationState,
                        onNavigateToRadar = { target ->
                            radarViewModel.selectDestination(target)
                            currentScreen = NavScreen.RADAR
                        },
                        onOpenMap = {
                            isMapOpen = true
                        }
                    )
                    NavScreen.TRAIL -> BreadcrumbTrailScreen(
                        viewModel = trailViewModel,
                        locationState = locationState,
                        settings = settings
                    )
                    NavScreen.SETTINGS -> SettingsScreen(
                        viewModel = settingsViewModel
                    )
                }
            }
        }
    }
}
