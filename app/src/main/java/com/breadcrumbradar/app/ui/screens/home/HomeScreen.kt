package com.breadcrumbradar.app.ui.screens.home

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.breadcrumbradar.app.data.model.AppSettings
import com.breadcrumbradar.app.data.model.LocationCategory
import com.breadcrumbradar.app.data.model.SavedLocation
import com.breadcrumbradar.app.sensor.CompassData
import com.breadcrumbradar.app.sensor.GeoUtils
import com.breadcrumbradar.app.sensor.LocationState
import com.breadcrumbradar.app.ui.components.GlassCard
import com.breadcrumbradar.app.ui.screens.places.MarkLocationDialog
import com.breadcrumbradar.app.ui.screens.places.getCategoryIcon
import com.breadcrumbradar.app.ui.theme.CyberCyan
import com.breadcrumbradar.app.ui.theme.ElectricEmerald
import com.breadcrumbradar.app.ui.theme.SignalRed
import com.breadcrumbradar.app.ui.theme.WarningAmber
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    locationState: LocationState,
    compassData: CompassData,
    settings: AppSettings,
    onNavigateToPlaces: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var userLocationMarker by remember { mutableStateOf<Marker?>(null) }
    var connectingPolyline by remember { mutableStateOf<Polyline?>(null) }
    val objectMarkers = remember { mutableStateMapOf<Long, Marker>() }

    // Continuously update ViewModel with live GPS updates
    LaunchedEffect(locationState, settings) {
        viewModel.updateCurrentLocation(locationState, settings)
    }

    // Trigger arrival vibration when destination is reached
    LaunchedEffect(uiState.isArrived) {
        if (uiState.isArrived && uiState.isNavigating && settings.vibrateOnArrival) {
            triggerArrivalVibration(context)
        }
    }

    // Camera target animation
    LaunchedEffect(uiState.cameraTarget) {
        val target = uiState.cameraTarget
        if (target != null && mapView != null) {
            mapView?.controller?.animateTo(target, 17.5, 1000L)
            viewModel.clearCameraTarget()
        }
    }

    // Update User Location Dot and Connecting Route Line dynamically on GPS updates
    LaunchedEffect(locationState, uiState.selectedLocation, uiState.routeResult) {
        val map = mapView ?: return@LaunchedEffect

        // 1. Update User Location Marker
        if (locationState.hasFix) {
            val userGeoPoint = GeoPoint(locationState.latitude, locationState.longitude)
            if (userLocationMarker == null) {
                userLocationMarker = Marker(map).apply {
                    icon = MapMarkerUtils.createUserLocationDotDrawable(context)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    title = "My Current Location"
                    map.overlays.add(this)
                }
            }
            userLocationMarker?.position = userGeoPoint
        }

        // 2. Update Dynamic Connecting Line
        connectingPolyline?.let { map.overlays.remove(it) }
        val target = uiState.selectedLocation
        if (locationState.hasFix && target != null) {
            val routePoints = uiState.routeResult?.points ?: listOf(
                GeoPoint(locationState.latitude, locationState.longitude),
                GeoPoint(target.latitude, target.longitude)
            )

            connectingPolyline = Polyline().apply {
                outlinePaint.color = if (uiState.routeResult?.isRoadRoute == true) {
                    android.graphics.Color.parseColor("#1A73E8") // Google Royal Blue
                } else {
                    android.graphics.Color.parseColor("#00E676") // Emerald connecting beam
                }
                outlinePaint.strokeWidth = if (uiState.isNavigating) 10f else 6f
                outlinePaint.isAntiAlias = true
                setPoints(routePoints)
            }
            map.overlays.add(connectingPolyline)
        }
        map.invalidate()
    }

    // Sync saved object markers on map
    LaunchedEffect(uiState.allLocations, uiState.selectedLocation) {
        val map = mapView ?: return@LaunchedEffect

        // Remove markers that were deleted
        val currentIds = uiState.allLocations.map { it.id }.toSet()
        val toRemove = objectMarkers.keys.filter { it !in currentIds }
        toRemove.forEach { id ->
            objectMarkers[id]?.let { map.overlays.remove(it) }
            objectMarkers.remove(id)
        }

        // Add or update markers
        uiState.allLocations.forEach { loc ->
            val isSelected = uiState.selectedLocation?.id == loc.id
            var marker = objectMarkers[loc.id]
            if (marker == null) {
                marker = Marker(map).apply {
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    setOnMarkerClickListener { _, _ ->
                        viewModel.selectLocation(loc)
                        true
                    }
                    map.overlays.add(this)
                }
                objectMarkers[loc.id] = marker
            }

            marker.position = GeoPoint(loc.latitude, loc.longitude)
            marker.title = loc.name
            marker.snippet = loc.category
            marker.icon = MapMarkerUtils.createObjectMarkerDrawable(context, loc.category, isSelected)
        }
        map.invalidate()
    }

    Box(modifier = modifier.fillMaxSize()) {
        // ==========================================
        // 1. FULLSCREEN INTERACTIVE GEOGRAPHICAL MAP
        // ==========================================
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

                    val startPoint = if (locationState.hasFix) {
                        GeoPoint(locationState.latitude, locationState.longitude)
                    } else {
                        GeoPoint(37.7749, -122.4194)
                    }
                    controller.setZoom(settings.defaultZoomLevel)
                    controller.setCenter(startPoint)

                    // Add Map Events Overlay to support Long-press to mark a point anywhere!
                    val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                            // Tap on blank map dismisses selection if not navigating
                            if (!uiState.isNavigating) {
                                viewModel.selectLocation(null)
                            }
                            return false
                        }

                        override fun longPressHelper(p: GeoPoint?): Boolean {
                            if (p != null) {
                                viewModel.openMarkDialog(Pair(p.latitude, p.longitude))
                            }
                            return true
                        }
                    })
                    overlays.add(0, eventsOverlay)

                    mapView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // ==========================================
        // 2. TOP FLOATING BAR & CATEGORY CHIPS
        // ==========================================
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 12.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top App Bar Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Explore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Kmap Tracker",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val gpsText = if (locationState.hasFix) {
                                "GPS Fix Active (±%.1fm)".format(locationState.accuracy)
                            } else {
                                "Searching satellites..."
                            }
                            Text(
                                text = gpsText,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (locationState.hasFix) ElectricEmerald else WarningAmber
                            )
                        }
                    }

                    // Places List Shortcut
                    Surface(
                        onClick = onNavigateToPlaces,
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmarks,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${uiState.allLocations.size} Objects",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Quick Category Filter / Selector Chips
            if (uiState.allLocations.isNotEmpty() && !uiState.isNavigating) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.allLocations) { loc ->
                        val isSelected = uiState.selectedLocation?.id == loc.id
                        Surface(
                            modifier = Modifier.clickable { viewModel.selectLocation(loc) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            ),
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(loc.iconName),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = loc.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. FLOATING MAP CONTROLS (Right Side)
        // ==========================================
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Recenter on User
            FloatingActionButton(
                onClick = { viewModel.recenterOnUser() },
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "My Location", modifier = Modifier.size(22.dp))
            }

            // Reset North
            FloatingActionButton(
                onClick = { mapView?.mapOrientation = 0f },
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SignalRed,
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Icon(Icons.Default.Navigation, contentDescription = "North", modifier = Modifier.size(20.dp))
            }

            // Zoom In
            FloatingActionButton(
                onClick = { mapView?.controller?.zoomIn() },
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(20.dp))
            }

            // Zoom Out
            FloatingActionButton(
                onClick = { mapView?.controller?.zoomOut() },
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(20.dp))
            }
        }

        // ==========================================
        // 4. FLOATING "+ MARK LOCATION" ACTION BUTTON
        // ==========================================
        if (!uiState.isNavigating && uiState.selectedLocation == null) {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openMarkDialog() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 85.dp, end = 16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.AddLocationAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mark Location", fontWeight = FontWeight.Bold)
            }
        }

        // ==========================================
        // 5. SELECTED LOCATION BOTTOM INFO PANEL
        // ==========================================
        AnimatedVisibility(
            visible = uiState.selectedLocation != null && !uiState.isNavigating,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 75.dp, start = 16.dp, end = 16.dp)
        ) {
            uiState.selectedLocation?.let { loc ->
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shadowElevation = 14.dp
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Title row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = getCategoryIcon(loc.iconName),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = loc.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = loc.category,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(onClick = { viewModel.selectLocation(null) }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Live Distance Display
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "LIVE DISTANCE FROM YOU",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (locationState.hasFix) uiState.formattedDistance else "Acquiring GPS...",
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    val routeType = if (uiState.routeResult?.isRoadRoute == true) {
                                        "Road walking route"
                                    } else {
                                        "Straight-line connecting distance"
                                    }
                                    Text(
                                        text = routeType,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "HEADING",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyberCyan
                                    )
                                    Text(
                                        text = "%.0f° %s".format(
                                            uiState.bearingDegrees,
                                            GeoUtils.headingToCardinal(uiState.bearingDegrees)
                                        ),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Action Buttons: [ START GOING ] & Delete
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.startNavigation() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("START GOING", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { viewModel.deleteLocation(loc) },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SignalRed)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 6. ACTIVE FULL-SCREEN NAVIGATION HUD MODE
        // ==========================================
        if (uiState.isNavigating && uiState.selectedLocation != null) {
            val target = uiState.selectedLocation!!

            // Top Navigation Banner
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "NAVIGATING TO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = target.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Text(
                            text = uiState.formattedDistance,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Arrival state banner
                    if (uiState.isArrived) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ElectricEmerald
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 14.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DESTINATION REACHED!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Navigation HUD Controls
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shadowElevation = 10.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.recenterOnUser() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Recenter")
                    }

                    Button(
                        onClick = { viewModel.stopNavigation() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SignalRed)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Stop Going")
                    }
                }
            }
        }
    }

    // Mark Location Dialog
    if (uiState.showMarkDialog) {
        val defaultCoord = uiState.markLocationCoordinates
        val initialLoc = defaultCoord?.let { (lat, lon) ->
            SavedLocation(
                name = "",
                latitude = lat,
                longitude = lon,
                accuracy = locationState.accuracy
            )
        }

        MarkLocationDialog(
            locationState = locationState,
            initialLocation = initialLoc,
            onDismiss = { viewModel.closeMarkDialog() },
            onSave = { loc -> viewModel.saveLocation(loc) }
        )
    }
}

private fun triggerArrivalVibration(context: Context) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 250, 150, 450)
            val amplitudes = intArrayOf(0, 255, 0, 255)
            vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(500)
        }
    } catch (e: Exception) {
        // Ignored
    }
}
