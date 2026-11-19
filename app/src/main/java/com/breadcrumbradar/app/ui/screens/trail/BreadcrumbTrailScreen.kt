package com.breadcrumbradar.app.ui.screens.trail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.breadcrumbradar.app.data.model.AppSettings
import com.breadcrumbradar.app.data.model.BreadcrumbPoint
import com.breadcrumbradar.app.sensor.GeoUtils
import com.breadcrumbradar.app.sensor.LocationState
import com.breadcrumbradar.app.ui.components.GlassCard
import com.breadcrumbradar.app.ui.theme.CyberCyan
import com.breadcrumbradar.app.ui.theme.ElectricEmerald
import com.breadcrumbradar.app.ui.theme.SignalRed
import com.breadcrumbradar.app.ui.theme.WarningAmber

@Composable
fun BreadcrumbTrailScreen(
    viewModel: TrailViewModel,
    locationState: LocationState,
    settings: AppSettings,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(settings.distanceUnit) {
        viewModel.updateDistanceUnit(settings.distanceUnit)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "OFFLINE MOVEMENT RECORDER",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Breadcrumb Trail",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(
                onClick = { showClearDialog = true },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Clear History", tint = SignalRed)
            }
        }

        // VISUAL BREADCRUMB TRAIL CANVAS CARD
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            contentPadding = 16.dp
        ) {
            Text(
                text = "TRAIL MAP PATH",
                style = MaterialTheme.typography.labelSmall,
                color = CyberCyan,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.trailPoints.size < 2) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Route,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = if (uiState.activeSession == null) "Trail tracking is inactive" else "Recording initial footsteps...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Walk around to trace your movement path",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                TrailCanvas(
                    points = uiState.trailPoints,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Live Telemetry Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Distance Traveled
            GlassCard(
                modifier = Modifier.weight(1f),
                contentPadding = 12.dp
            ) {
                Text(
                    text = "TOTAL DISTANCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = uiState.formattedDistance,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Points Count
            GlassCard(
                modifier = Modifier.weight(1f),
                contentPadding = 12.dp
            ) {
                Text(
                    text = "BREADCRUMBS",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan
                )
                Text(
                    text = "${uiState.trailPoints.size} points",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Tracking Status
            GlassCard(
                modifier = Modifier.weight(1f),
                contentPadding = 12.dp
            ) {
                Text(
                    text = "STATUS",
                    style = MaterialTheme.typography.labelSmall,
                    color = WarningAmber
                )
                val statusText = when {
                    uiState.activeSession == null -> "IDLE"
                    uiState.activeSession?.isPaused == true -> "PAUSED"
                    else -> "RECORDING"
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = when (statusText) {
                        "RECORDING" -> ElectricEmerald
                        "PAUSED" -> WarningAmber
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }

        // Controls (Start / Pause / Resume / Stop)
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 16.dp
        ) {
            Text(
                text = "TRAIL CONTROLS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.activeSession == null) {
                Button(
                    onClick = { viewModel.startTracking(context) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricEmerald)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Recording Trail", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (uiState.activeSession?.isPaused == true) {
                        Button(
                            onClick = { viewModel.resumeTracking(context) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricEmerald)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resume")
                        }
                    } else {
                        Button(
                            onClick = { viewModel.pauseTracking(context) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WarningAmber)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pause")
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.stopTracking(context) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SignalRed)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Stop")
                    }
                }
            }
        }

        // REVERSE TRAIL NAVIGATION ("Retrace Footsteps")
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 16.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "REVERSE WAYFINDING",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Guide Me Back to Start",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Retrace footsteps back to your initial departure location.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = uiState.isReverseNavigationActive,
                    onCheckedChange = { viewModel.toggleReverseNavigation() },
                    enabled = uiState.trailPoints.isNotEmpty()
                )
            }

            if (uiState.isReverseNavigationActive && uiState.reverseTargetPoint != null) {
                val origin = uiState.reverseTargetPoint!!
                val distToOrigin = if (locationState.hasFix) {
                    GeoUtils.calculateDistanceMeters(
                        locationState.latitude, locationState.longitude,
                        origin.latitude, origin.longitude
                    )
                } else 0.0

                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Distance to Trail Origin:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = GeoUtils.formatDistance(distToOrigin, settings.distanceUnit),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElectricEmerald
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(70.dp))
    }

    // Clear Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Trail History?") },
            text = { Text("This will permanently delete all recorded movement paths and breadcrumb points from this device.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearHistory()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SignalRed)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TrailCanvas(
    points: List<BreadcrumbPoint>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas

        val lats = points.map { it.latitude }
        val lons = points.map { it.longitude }

        val minLat = lats.minOrNull() ?: 0.0
        val maxLat = lats.maxOrNull() ?: 0.0
        val minLon = lons.minOrNull() ?: 0.0
        val maxLon = lons.maxOrNull() ?: 0.0

        val latSpan = (maxLat - minLat).coerceAtLeast(0.0001)
        val lonSpan = (maxLon - minLon).coerceAtLeast(0.0001)

        val padding = 24.dp.toPx()
        val usableWidth = size.width - 2 * padding
        val usableHeight = size.height - 2 * padding

        fun pointToOffset(p: BreadcrumbPoint): Offset {
            val x = padding + ((p.longitude - minLon) / lonSpan).toFloat() * usableWidth
            // Invert Y because screen Y goes down but latitude goes up
            val y = padding + (1f - ((p.latitude - minLat) / latSpan).toFloat()) * usableHeight
            return Offset(x, y)
        }

        // Draw Polyline Path
        val path = Path()
        val firstOffset = pointToOffset(points.first())
        path.moveTo(firstOffset.x, firstOffset.y)

        for (i in 1 until points.size) {
            val pt = pointToOffset(points[i])
            path.lineTo(pt.x, pt.y)
        }

        // Glowing trail line
        drawPath(
            path = path,
            color = CyberCyan.copy(alpha = 0.25f),
            style = Stroke(width = 8.dp.toPx())
        )
        drawPath(
            path = path,
            color = CyberCyan,
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw Origin Marker (Start)
        drawCircle(
            color = ElectricEmerald,
            radius = 6.dp.toPx(),
            center = firstOffset
        )
        drawCircle(
            color = Color.White,
            radius = 2.5.dp.toPx(),
            center = firstOffset
        )

        // Draw Current / End Marker
        val lastOffset = pointToOffset(points.last())
        drawCircle(
            color = SignalRed,
            radius = 7.dp.toPx(),
            center = lastOffset
        )
        drawCircle(
            color = Color.White,
            radius = 3.dp.toPx(),
            center = lastOffset
        )
    }
}
