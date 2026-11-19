package com.breadcrumbradar.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.breadcrumbradar.app.data.model.AppSettings
import com.breadcrumbradar.app.sensor.CompassData
import com.breadcrumbradar.app.sensor.LocationState
import com.breadcrumbradar.app.ui.components.BottomNavBar
import com.breadcrumbradar.app.ui.components.NavScreen
import com.breadcrumbradar.app.ui.screens.home.HomeScreen
import com.breadcrumbradar.app.ui.screens.home.HomeViewModel
import com.breadcrumbradar.app.ui.screens.places.MyPlacesScreen
import com.breadcrumbradar.app.ui.screens.places.PlacesViewModel
import com.breadcrumbradar.app.ui.screens.settings.SettingsScreen
import com.breadcrumbradar.app.ui.screens.settings.SettingsViewModel
import com.breadcrumbradar.app.ui.screens.trail.BreadcrumbTrailScreen
import com.breadcrumbradar.app.ui.screens.trail.TrailViewModel

@Composable
fun AppNavigation(
    homeViewModel: HomeViewModel,
    placesViewModel: PlacesViewModel,
    trailViewModel: TrailViewModel,
    settingsViewModel: SettingsViewModel,
    locationState: LocationState,
    compassData: CompassData,
    settings: AppSettings
) {
    var currentScreen by remember { mutableStateOf(NavScreen.MAP) }
    val homeUiState by homeViewModel.uiState.collectAsState()

    val showBottomBar = !(currentScreen == NavScreen.MAP && homeUiState.isNavigating)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentScreen = currentScreen,
                    onScreenSelected = { currentScreen = it }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (currentScreen == NavScreen.MAP) {
                        Modifier.padding(top = innerPadding.calculateTopPadding())
                    } else {
                        Modifier.padding(innerPadding)
                    }
                )
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentScreen) {
                NavScreen.MAP -> HomeScreen(
                    viewModel = homeViewModel,
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
                    onLocateOnMap = { location ->
                        homeViewModel.selectLocation(location)
                        currentScreen = NavScreen.MAP
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
