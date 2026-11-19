package com.breadcrumbradar.app.data.repository

import com.breadcrumbradar.app.data.local.PreferencesManager
import com.breadcrumbradar.app.data.model.AppSettings
import kotlinx.coroutines.flow.StateFlow

class SettingsRepository(private val preferencesManager: PreferencesManager) {
    val settingsFlow: StateFlow<AppSettings> = preferencesManager.settingsFlow

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        preferencesManager.updateSettings(transform)
    }
}
