package com.breadcrumbradar.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.breadcrumbradar.app.data.model.RadarSpeed
import com.breadcrumbradar.app.ui.theme.CyberCyan
import com.breadcrumbradar.app.ui.theme.ElectricEmerald
import com.breadcrumbradar.app.ui.theme.SignalRed
import kotlin.math.*

@OptIn(ExperimentalTextApi::class)
@Composable
fun SciFiRadar(
    relativeBearing: Float, // 0..360 (Direction to target relative to phone heading)
    currentHeading: Float,  // Phone azimuth (0..360)
    targetDistanceMeters: Double,
    maxRangeMeters: Double = 100.0,
    radarSpeed: RadarSpeed = RadarSpeed.NORMAL,
    hasTarget: Boolean = true,
    isArrived: Boolean = false,
    radarTheme: String = "EMERALD",
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // Primary accent color based on theme
    val accentColor = when (radarTheme) {
        "CYAN" -> CyberCyan
        "AMBER" -> Color(0xFFFFB300)
        "MONOCHROME" -> Color(0xFFE2E8F0)
        else -> ElectricEmerald
    }

    // Radar sweep rotation
    val infiniteTransition = rememberInfiniteTransition(label = "radarSweep")
    val sweepAngle by if (radarSpeed != RadarSpeed.STATIC) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(radarSpeed.durationMs, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "sweepAngle"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    // Target blip pulsing
    val blipPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blipPulse"
    )

    // Smoothly animated relative bearing to avoid abrupt needle flips
    val animatedBearing by animateFloatAsState(
        targetValue = relativeBearing,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "bearingAnim"
    )

    Box(modifier = modifier.aspectRatio(1f)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension / 2f) * 0.90f

            // 1. Draw outer tech border and glow
            drawCircle(
                color = accentColor.copy(alpha = 0.08f),
                radius = radius,
                center = center
            )
            drawCircle(
                color = accentColor.copy(alpha = 0.45f),
                radius = radius,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )
            drawCircle(
                color = accentColor.copy(alpha = 0.20f),
                radius = radius + 6.dp.toPx(),
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // 2. Draw Concentric Range Rings (25%, 50%, 75%)
            listOf(0.25f, 0.50f, 0.75f).forEach { fraction ->
                drawCircle(
                    color = accentColor.copy(alpha = 0.22f),
                    radius = radius * fraction,
                    center = center,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                )
            }

            // 3. Draw Crosshair Axes
            drawLine(
                color = accentColor.copy(alpha = 0.25f),
                start = Offset(center.x - radius, center.y),
                end = Offset(center.x + radius, center.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = accentColor.copy(alpha = 0.25f),
                start = Offset(center.x, center.y - radius),
                end = Offset(center.x, center.y + radius),
                strokeWidth = 1.dp.toPx()
            )

            // 4. Draw Degree Tick Marks around outer edge
            for (deg in 0 until 360 step 30) {
                val rad = Math.toRadians(deg.toDouble())
                val isMajor = deg % 90 == 0
                val tickLen = if (isMajor) 14.dp.toPx() else 7.dp.toPx()
                val outerX = center.x + radius * cos(rad).toFloat()
                val outerY = center.y + radius * sin(rad).toFloat()
                val innerX = center.x + (radius - tickLen) * cos(rad).toFloat()
                val innerY = center.y + (radius - tickLen) * sin(rad).toFloat()

                drawLine(
                    color = if (isMajor) accentColor.copy(alpha = 0.8f) else accentColor.copy(alpha = 0.35f),
                    start = Offset(innerX, innerY),
                    end = Offset(outerX, outerY),
                    strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                )
            }

            // 5. Draw Compass Cardinal indicators (N, E, S, W) relative to heading
            val cardinalOffsetHeading = -currentHeading
            val cardinals = listOf("N" to 0f, "E" to 90f, "S" to 180f, "W" to 270f)
            cardinals.forEach { (label, angleDeg) ->
                val totalAngle = Math.toRadians((angleDeg + cardinalOffsetHeading - 90).toDouble())
                val labelR = radius - 24.dp.toPx()
                val lx = center.x + labelR * cos(totalAngle).toFloat()
                val ly = center.y + labelR * sin(totalAngle).toFloat()

                val textStyle = TextStyle(
                    color = if (label == "N") SignalRed else accentColor.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                val layout = textMeasurer.measure(AnnotatedString(label), textStyle)
                drawText(
                    textMeasurer = textMeasurer,
                    text = label,
                    style = textStyle,
                    topLeft = Offset(lx - layout.size.width / 2f, ly - layout.size.height / 2f)
                )
            }

            // 6. Draw Animated Sweeping Radar Beam (if not static)
            if (radarSpeed != RadarSpeed.STATIC) {
                rotate(sweepAngle, center) {
                    val sweepBrush = Brush.sweepGradient(
                        0.0f to Color.Transparent,
                        0.88f to Color.Transparent,
                        1.0f to accentColor.copy(alpha = 0.35f),
                        center = center
                    )
                    drawCircle(
                        brush = sweepBrush,
                        radius = radius,
                        center = center
                    )
                    drawLine(
                        color = accentColor,
                        start = center,
                        end = Offset(center.x + radius, center.y),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            // 7. Draw Target Blip & Direction Pointer if hasTarget
            if (hasTarget) {
                // Direction Angle in Radians (-90 so 0° points UP)
                val targetRad = Math.toRadians((animatedBearing - 90.0))

                // Scaled Distance on radar screen (clamped between 20% and 88% of radius)
                val distanceRatio = (targetDistanceMeters / max(maxRangeMeters, 10.0)).toFloat()
                val clampedRatio = distanceRatio.coerceIn(0.20f, 0.88f)
                val blipDistance = radius * clampedRatio

                val blipX = center.x + blipDistance * cos(targetRad).toFloat()
                val blipY = center.y + blipDistance * sin(targetRad).toFloat()

                // Target aura
                drawCircle(
                    color = accentColor.copy(alpha = 0.25f),
                    radius = 16.dp.toPx() * blipPulse,
                    center = Offset(blipX, blipY)
                )
                // Target dot
                drawCircle(
                    color = if (isArrived) ElectricEmerald else CyberCyan,
                    radius = 7.dp.toPx(),
                    center = Offset(blipX, blipY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(blipX, blipY)
                )

                // 8. Draw Prominent Directional Vector Arrow from Center to Outer Edge
                rotate(animatedBearing, center) {
                    val arrowPath = Path().apply {
                        val arrowTipY = center.y - radius * 0.92f
                        val arrowBaseY = center.y - radius * 0.65f
                        moveTo(center.x, arrowTipY)
                        lineTo(center.x + 14.dp.toPx(), arrowBaseY)
                        lineTo(center.x, arrowBaseY + 6.dp.toPx())
                        lineTo(center.x - 14.dp.toPx(), arrowBaseY)
                        close()
                    }
                    drawPath(
                        path = arrowPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(CyberCyan, accentColor),
                            startY = center.y - radius,
                            endY = center.y
                        )
                    )
                    drawPath(
                        path = arrowPath,
                        color = Color.White,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }

            // 9. Center User Point (Device)
            drawCircle(
                color = accentColor.copy(alpha = 0.20f),
                radius = 14.dp.toPx(),
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = 5.dp.toPx(),
                center = center
            )
        }
    }
}
