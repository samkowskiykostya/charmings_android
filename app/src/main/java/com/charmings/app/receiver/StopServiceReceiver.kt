package com.charmings.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.charmings.app.service.StepCounterService

class StopServiceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, StepCounterService::class.java)
        context.stopService(serviceIntent)
    }
}
