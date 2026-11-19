package com.breadcrumbradar.app.ui.screens.map

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.breadcrumbradar.app.data.model.SavedLocation
import com.breadcrumbradar.app.sensor.LocationState
import com.breadcrumbradar.app.ui.components.GlassCard
import com.breadcrumbradar.app.ui.theme.CyberCyan
import com.breadcrumbradar.app.ui.theme.ElectricEmerald
import com.breadcrumbradar.app.ui.theme.WarningAmber
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineMapScreen(
    savedLocations: List<SavedLocation>,
    selectedDestination: SavedLocation?,
    locationState: LocationState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var showTileInfo by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        onDispose {
            mapView?.onDetach()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("OpenStreetMap View", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "${savedLocations.size} markers plotted",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showTileInfo = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Offline Cache Info", tint = CyberCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(16.0)

                        // Center on user position or selected destination
                        val centerPoint = if (locationState.hasFix) {
                            GeoPoint(locationState.latitude, locationState.longitude)
                        } else if (selectedDestination != null) {
                            GeoPoint(selectedDestination.latitude, selectedDestination.longitude)
                        } else {
                            GeoPoint(37.7749, -122.4194)
                        }
                        controller.setCenter(centerPoint)

                        // Plot all saved location markers
                        savedLocations.forEach { loc ->
                            val marker = Marker(this)
                            marker.position = GeoPoint(loc.latitude, loc.longitude)
                            marker.title = loc.name
                            marker.snippet = loc.category
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            overlays.add(marker)
                        }

                        // Draw vector polyline to selected destination if user has fix
                        if (locationState.hasFix && selectedDestination != null) {
                            val line = Polyline().apply {
                                outlinePaint.color = android.graphics.Color.parseColor("#00E676")
                                outlinePaint.strokeWidth = 6f
                                setPoints(
                                    listOf(
                                        GeoPoint(locationState.latitude, locationState.longitude),
                                        GeoPoint(selectedDestination.latitude, selectedDestination.longitude)
                                    )
                                )
                            }
                            overlays.add(line)
                        }

                        mapView = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Honest Offline Tile Notice Banner at bottom
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = ElectricEmerald,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Offline Radar Guarantee",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Directional Radar & Distances work 100% offline. Visual map tiles render from local device cache.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showTileInfo) {
        AlertDialog(
            onDismissRequest = { showTileInfo = false },
            icon = { Icon(Icons.Default.Storage, contentDescription = null, tint = CyberCyan) },
            title = { Text("OpenStreetMap Offline Storage") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Honest Map Transparency:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• Map tiles viewed while connected are automatically cached locally in device storage.\n• When fully offline, previously viewed map sectors remain visible.\n• The core Radar, Compass, and Breadcrumb Wayfinding never rely on map tiles and work anywhere on Earth with zero internet.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showTileInfo = false }) {
                    Text("Understood")
                }
            }
        )
    }
}
