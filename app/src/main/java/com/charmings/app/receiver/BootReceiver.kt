package com.charmings.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.charmings.app.graph
import com.charmings.app.service.StepCounterService

/** Restarts tracking after reboot — only if the user hasn't stopped it and permission is granted. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        if (!context.graph.settings.trackingEnabled) return
        StepCounterService.start(context) // no-op without ACTIVITY_RECOGNITION
    }
}
