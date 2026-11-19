package com.breadcrumbradar.app.ui.screens.radar

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.breadcrumbradar.app.data.model.AppSettings
import com.breadcrumbradar.app.data.model.SavedLocation
import com.breadcrumbradar.app.sensor.CompassData
import com.breadcrumbradar.app.sensor.GeoUtils
import com.breadcrumbradar.app.sensor.LocationState
import com.breadcrumbradar.app.ui.components.GlassCard
import com.breadcrumbradar.app.ui.components.GpsStatusBadge
import com.breadcrumbradar.app.ui.components.SciFiRadar
import com.breadcrumbradar.app.ui.screens.places.getCategoryIcon
import com.breadcrumbradar.app.ui.theme.CyberCyan
import com.breadcrumbradar.app.ui.theme.ElectricEmerald
import com.breadcrumbradar.app.ui.theme.SignalRed
import com.breadcrumbradar.app.ui.theme.WarningAmber

@Composable
fun RadarScreen(
    viewModel: RadarViewModel,
    locationState: LocationState,
    compassData: CompassData,
    settings: AppSettings,
    onNavigateToPlaces: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Continuously update navigation geometry on sensor changes
    LaunchedEffect(locationState, compassData, settings, uiState.selectedDestination) {
        viewModel.updateNavigation(locationState, compassData, settings)
    }

    // Trigger arrival haptic vibration if enabled
    LaunchedEffect(uiState.isArrived) {
        if (uiState.isArrived && !uiState.hasTriggeredArrivalHaptic && settings.vibrateOnArrival) {
            triggerArrivalVibration(context)
            viewModel.setHapticTriggered()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "RADAR WAYFINDING",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = uiState.selectedDestination?.name ?: "Select a Target",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Compass Recalibrate Button
                IconButton(
                    onClick = { viewModel.setShowRecalibrateDialog(true) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.RotateRight,
                        contentDescription = "Calibrate Compass",
                        tint = if (compassData.isCalibrated) ElectricEmerald else WarningAmber
                    )
                }

                // Choose Destination / Places Button
                IconButton(
                    onClick = onNavigateToPlaces,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmarks,
                        contentDescription = "All Places",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Target Selector Quick Chips
        if (uiState.allLocations.size > 1) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.allLocations) { loc ->
                    val isSelected = uiState.selectedDestination?.id == loc.id
                    Surface(
                        modifier = Modifier.clickable { viewModel.selectDestination(loc) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(loc.iconName),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = loc.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // If no destination is saved yet
        if (uiState.selectedDestination == null) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 24.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddLocation,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "No Destination Selected",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Bookmark a location (such as your car, hotel, or campsite) to begin offline directional radar guidance.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onNavigateToPlaces,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Choose or Bookmark a Place")
                    }
                }
            }
        } else {
            // SCI-FI CIRCULAR RADAR CANVAS
            SciFiRadar(
                relativeBearing = uiState.relativeBearing,
                currentHeading = compassData.azimuthDegrees,
                targetDistanceMeters = uiState.distanceMeters,
                radarSpeed = settings.radarSpeed,
                hasTarget = true,
                isArrived = uiState.isArrived,
                radarTheme = settings.radarTheme,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 4.dp)
            )

            // Prominent Distance Readout
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = if (uiState.isArrived) ElectricEmerald else null,
                contentPadding = 16.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (uiState.isArrived) "DESTINATION REACHED!" else "DISTANCE TO TARGET",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isArrived) ElectricEmerald else MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = uiState.distanceFormatted,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 38.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = if (uiState.isArrived) ElectricEmerald else MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.distanceKm,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "•",
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = uiState.distanceFeet,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Compass & Telemetry Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Compass Heading Card
                GlassCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = 12.dp
                ) {
                    Text(
                        text = "HEADING",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan
                    )
                    Text(
                        text = "%.0f° %s".format(
                            compassData.azimuthDegrees,
                            GeoUtils.headingToCardinal(compassData.azimuthDegrees)
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (compassData.isCalibrated) "Compass Calibrated" else "Needs Calibration",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (compassData.isCalibrated) ElectricEmerald else WarningAmber
                    )
                }

                // Altitude Difference Card
                GlassCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = 12.dp
                ) {
                    Text(
                        text = "ELEVATION DELTA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val delta = uiState.altitudeDeltaMeters
                    val deltaText = when {
                        delta > 1.0 -> "+%.0fm Higher".format(delta)
                        delta < -1.0 -> "%.0fm Lower".format(delta)
                        else -> "Level Ground"
                    }
                    Text(
                        text = deltaText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Relative altitude",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // GPS Signal & Accuracy Status
            GpsStatusBadge(locationState = locationState)
        }

        Spacer(modifier = Modifier.height(70.dp))
    }

    // Compass Recalibrate Dialog
    if (uiState.showRecalibrateDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowRecalibrateDialog(false) },
            icon = {
                Icon(
                    imageVector = Icons.Default.ScreenRotation,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = { Text("Calibrate Compass Sensor") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "To ensure true directional accuracy:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "1. Wave your phone smoothly in a Figure-8 pattern for 5 seconds.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "2. Keep away from strong magnets, car speakers, or large metal structures.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Status: ${if (compassData.isCalibrated) "Calibrated (High Accuracy)" else "Uncalibrated / Low Accuracy"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (compassData.isCalibrated) ElectricEmerald else WarningAmber,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.setShowRecalibrateDialog(false) }) {
                    Text("Done")
                }
            }
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
            val timings = longArrayOf(0, 200, 150, 400)
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
