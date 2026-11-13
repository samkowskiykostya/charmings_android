package com.charmings.app.data.repository

import android.content.Context

/**
 * Tiny synchronous settings (read from BootReceiver, which must not block on DataStore).
 */
class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    /** The user's choice. "Зупинити" must survive app restarts and reboots. */
    var trackingEnabled: Boolean
        get() = prefs.getBoolean(KEY_TRACKING, true)
        set(value) = prefs.edit().putBoolean(KEY_TRACKING, value).apply()

    private companion object {
        const val KEY_TRACKING = "tracking_enabled"
    }
}
