package com.breadcrumbradar.app

import android.app.Application
import android.content.Context
import org.osmdroid.config.Configuration

class BreadcrumbRadarApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize OpenStreetMap offline cache & user agent
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName
    }
}
