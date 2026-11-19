package com.breadcrumbradar.app.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.breadcrumbradar.app.MainActivity
import com.breadcrumbradar.app.data.local.AppDatabase
import com.breadcrumbradar.app.data.model.BreadcrumbPoint
import com.breadcrumbradar.app.sensor.LocationClient
import kotlinx.coroutines.*

class BreadcrumbService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var locationClient: LocationClient
    private var activeSessionId: Long = 0L
    private var isPaused = false

    override fun onCreate() {
        super.onCreate()
        locationClient = LocationClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_START -> {
                activeSessionId = intent.getLongExtra(EXTRA_SESSION_ID, 0L)
                isPaused = false
                startForeground(NOTIFICATION_ID, buildNotification("Recording trail...", "Breadcrumb tracking is active."))
                startTracking()
            }
            ACTION_PAUSE -> {
                isPaused = true
                updateNotification("Trail paused", "Tracking is paused.")
            }
            ACTION_RESUME -> {
                isPaused = false
                updateNotification("Recording trail...", "Breadcrumb tracking is active.")
            }
            ACTION_STOP -> {
                stopTracking()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startTracking() {
        locationClient.startLocationUpdates(4000L)
        serviceScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            locationClient.locationFlow.collect { state ->
                if (!isPaused && state.hasFix && activeSessionId > 0) {
                    val point = BreadcrumbPoint(
                        sessionId = activeSessionId,
                        latitude = state.latitude,
                        longitude = state.longitude,
                        altitude = state.altitude,
                        accuracy = state.accuracy,
                        speed = state.speed,
                        timestamp = System.currentTimeMillis()
                    )
                    db.breadcrumbDao().insertPoint(point)
                }
            }
        }
    }

    private fun stopTracking() {
        locationClient.stopLocationUpdates()
        serviceScope.cancel()
    }

    private fun buildNotification(title: String, text: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(title: String, text: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(NOTIFICATION_ID, buildNotification(title, text))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Kmap Trail Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active trail recording status in background"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stopTracking()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "breadcrumb_radar_channel"
        const val NOTIFICATION_ID = 4040

        const val ACTION_START = "com.breadcrumbradar.action.START"
        const val ACTION_PAUSE = "com.breadcrumbradar.action.PAUSE"
        const val ACTION_RESUME = "com.breadcrumbradar.action.RESUME"
        const val ACTION_STOP = "com.breadcrumbradar.action.STOP"

        const val EXTRA_SESSION_ID = "extra_session_id"

        fun start(context: Context, sessionId: Long) {
            val intent = Intent(context, BreadcrumbService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_SESSION_ID, sessionId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pause(context: Context) {
            val intent = Intent(context, BreadcrumbService::class.java).apply { action = ACTION_PAUSE }
            context.startService(intent)
        }

        fun resume(context: Context) {
            val intent = Intent(context, BreadcrumbService::class.java).apply { action = ACTION_RESUME }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, BreadcrumbService::class.java).apply { action = ACTION_STOP }
            context.startService(intent)
        }
    }
}
