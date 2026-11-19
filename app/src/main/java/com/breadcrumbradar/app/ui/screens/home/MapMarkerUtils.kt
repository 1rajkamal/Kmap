package com.breadcrumbradar.app.ui.screens.home

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

object MapMarkerUtils {

    /**
     * Creates a Google Maps-style pin with category icon and soft drop shadow.
     */
    fun createObjectMarkerDrawable(
        context: Context,
        category: String,
        isSelected: Boolean = false
    ): Drawable {
        val width = if (isSelected) 100 else 84
        val height = if (isSelected) 124 else 104

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val (pinColor, badgeIconRes) = when (category.uppercase()) {
            "CAR", "PARKING" -> Pair(Color.parseColor("#1A73E8"), android.R.drawable.ic_menu_directions)
            "KEYS" -> Pair(Color.parseColor("#D97706"), android.R.drawable.ic_lock_lock)
            "SHOES" -> Pair(Color.parseColor("#059669"), android.R.drawable.ic_menu_myplaces)
            "BAG" -> Pair(Color.parseColor("#7C3AED"), android.R.drawable.ic_menu_agenda)
            "HOTEL" -> Pair(Color.parseColor("#4F46E5"), android.R.drawable.ic_menu_today)
            "BIKE" -> Pair(Color.parseColor("#0284C7"), android.R.drawable.ic_menu_compass)
            "MEETING" -> Pair(Color.parseColor("#E11D48"), android.R.drawable.ic_menu_share)
            "OUTDOOR" -> Pair(Color.parseColor("#15803D"), android.R.drawable.ic_menu_camera)
            else -> Pair(Color.parseColor("#E11D48"), android.R.drawable.ic_menu_myplaces)
        }

        val centerX = width / 2f
        val headRadius = width / 2.3f
        val tipY = height - 10f

        // Draw ground contact shadow
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(60, 0, 0, 0)
            maskFilter = BlurMaskFilter(6f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawOval(
            centerX - headRadius * 0.55f,
            tipY - 4f,
            centerX + headRadius * 0.55f,
            tipY + 8f,
            shadowPaint
        )

        // Draw Pin Teardrop Shape
        val pinPath = Path().apply {
            val headCenterY = headRadius + 4f
            arcTo(
                centerX - headRadius,
                headCenterY - headRadius,
                centerX + headRadius,
                headCenterY + headRadius,
                140f,
                260f,
                false
            )
            lineTo(centerX, tipY)
            close()
        }

        // Selection outer glow
        if (isSelected) {
            val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#00E5FF")
                strokeWidth = 8f
                style = Paint.Style.STROKE
            }
            canvas.drawPath(pinPath, glowPaint)
        }

        // Pin Body Fill
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = pinColor
            style = Paint.Style.FILL
        }
        canvas.drawPath(pinPath, fillPaint)

        // White border around pin
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }
        canvas.drawPath(pinPath, borderPaint)

        // Inner White Badge Circle
        val badgeCenterY = headRadius + 4f
        val badgeRadius = headRadius * 0.60f
        val innerCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(centerX, badgeCenterY, badgeRadius, innerCirclePaint)

        // Draw Center Icon inside Badge
        try {
            val iconDrawable = ContextCompat.getDrawable(context, badgeIconRes)
            if (iconDrawable != null) {
                val iconSize = (badgeRadius * 1.3f).toInt()
                val left = (centerX - iconSize / 2f).toInt()
                val top = (badgeCenterY - iconSize / 2f).toInt()
                iconDrawable.setBounds(left, top, left + iconSize, top + iconSize)
                iconDrawable.setTint(pinColor)
                iconDrawable.draw(canvas)
            }
        } catch (e: Exception) {
            // Ignored
        }

        return BitmapDrawable(context.resources, bitmap)
    }

    /**
     * Creates Google Maps-style Blue Location Dot with white border and soft blue accuracy halo.
     */
    fun createUserLocationDotDrawable(context: Context): Drawable {
        val size = 64
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val center = size / 2f

        // Outer soft translucent blue halo
        val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(60, 26, 115, 232)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(center, center, center - 2f, haloPaint)

        // White border
        val whiteBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(center, center, center * 0.62f, whiteBorderPaint)

        // Google Blue Center Dot
        val centerDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1A73E8") // Google Maps Royal Blue
            style = Paint.Style.FILL
        }
        canvas.drawCircle(center, center, center * 0.44f, centerDotPaint)

        return BitmapDrawable(context.resources, bitmap)
    }
}
