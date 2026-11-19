package com.breadcrumbradar.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.breadcrumbradar.app.sensor.GeoUtils
import com.breadcrumbradar.app.sensor.GpsSignalState
import com.breadcrumbradar.app.sensor.LocationState
import com.breadcrumbradar.app.ui.theme.CyberCyan
import com.breadcrumbradar.app.ui.theme.ElectricEmerald
import com.breadcrumbradar.app.ui.theme.SignalRed
import com.breadcrumbradar.app.ui.theme.WarningAmber

@Composable
fun GpsStatusBadge(
    locationState: LocationState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val (badgeColor, label) = when (locationState.signalState) {
        GpsSignalState.EXCELLENT -> Pair(ElectricEmerald, "GPS LOCKED (±%.1fm)".format(locationState.accuracy))
        GpsSignalState.GOOD -> Pair(CyberCyan, "GPS ACTIVE (±%.1fm)".format(locationState.accuracy))
        GpsSignalState.WEAK -> Pair(WarningAmber, "WEAK SIGNAL (±%.1fm)".format(locationState.accuracy))
        GpsSignalState.SEARCHING -> Pair(WarningAmber, "SEARCHING SATELLITES...")
        GpsSignalState.DISABLED -> Pair(SignalRed, "GPS DISABLED")
        GpsSignalState.NO_PERMISSION -> Pair(SignalRed, "PERMISSION REQUIRED")
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        glowColor = badgeColor,
        contentPadding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pulsing dot
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .scale(if (locationState.signalState == GpsSignalState.EXCELLENT || locationState.signalState == GpsSignalState.GOOD) pulseScale else 1f)
                            .background(badgeColor.copy(alpha = 0.35f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(badgeColor, CircleShape)
                    )
                }

                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                    if (locationState.hasFix) {
                        Text(
                            text = GeoUtils.formatCoordinates(locationState.latitude, locationState.longitude),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Stand under open sky for satellite fix",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (locationState.hasFix && locationState.altitude != 0.0) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "Alt: %.0fm".format(locationState.altitude),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
