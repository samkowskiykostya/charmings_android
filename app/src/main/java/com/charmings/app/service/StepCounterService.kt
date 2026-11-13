package com.charmings.app.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.charmings.app.BuildConfig
import com.charmings.app.MainActivity
import com.charmings.app.R
import com.charmings.app.domain.TrackerUpdate
import com.charmings.app.domain.WalkState
import com.charmings.app.domain.formatSteps
import com.charmings.app.domain.stepsWord
import com.charmings.app.graph
import com.charmings.app.prefetchPetImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Battery notes:
 *  - Sensors are registered with a max report latency, so the sensor hub batches steps and
 *    wakes the CPU about once a minute instead of on every step. Low latency only while the
 *    app is on screen (ACTION_UI_VISIBLE).
 *  - Events go through one channel, processed sequentially; disk writes happen ~once a minute.
 *  - The notification is refreshed only when a minute closes.
 */
class StepCounterService : Service(), SensorEventListener {

    companion object {
        private const val TAG = "StepCounterService"
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "step_counter_channel"
        const val PET_CAUGHT_CHANNEL_ID = "pet_caught_channel"
        const val PET_CAUGHT_NOTIFICATION_ID = 1000

        const val ACTION_STOP = "com.charmings.app.action.STOP"
        const val ACTION_UI_VISIBLE = "com.charmings.app.action.UI_VISIBLE"
        const val EXTRA_VISIBLE = "visible"
        const val EXTRA_PET_ID = "petId"

        private const val BACKGROUND_LATENCY_US = 60_000_000   // 60 s batching
        private const val TICK_MS = 60_000L

        fun hasActivityPermission(context: Context) =
            context.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) ==
                PackageManager.PERMISSION_GRANTED

        fun start(context: Context) {
            if (!hasActivityPermission(context)) return
            context.startForegroundService(Intent(context, StepCounterService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, StepCounterService::class.java))
        }

        /** Tell a running service whether the UI is visible (live updates vs. battery batching). */
        fun setUiVisible(context: Context, visible: Boolean) {
            if (!context.graph.trackingActive.value) return
            context.startService(
                Intent(context, StepCounterService::class.java)
                    .setAction(ACTION_UI_VISIBLE)
                    .putExtra(EXTRA_VISIBLE, visible)
            )
        }
    }

    private sealed interface Sample {
        data class Counter(val raw: Long, val timestampMs: Long) : Sample
        data class Detector(val timestampMs: Long) : Sample
    }

    private lateinit var sensorManager: SensorManager
    private var counterSensor: Sensor? = null
    private var detectorSensor: Sensor? = null
    private var registeredLatencyUs = -1
    private var firstCounterEvent = true

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val samples = Channel<Sample>(Channel.UNLIMITED)
    private val tracker by lazy { graph.tracker }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SensorManager::class.java)
        counterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        detectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        createNotificationChannels()

        scope.launch { for (sample in samples) handle(sample) }
        scope.launch {
            while (isActive) {
                delay(TICK_MS) // no wakelock: if the CPU sleeps, the tick simply waits
                runCatching { onUpdate(tracker.tick()) }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                graph.settings.trackingEnabled = false
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_UI_VISIBLE -> {
                if (graph.trackingActive.value) {
                    registerSensors(if (intent.getBooleanExtra(EXTRA_VISIBLE, false)) 0 else BACKGROUND_LATENCY_US)
                    return START_STICKY
                }
            }
        }

        if (!hasActivityPermission(this)) {
            Log.w(TAG, "ACTIVITY_RECOGNITION not granted; not starting")
            stopSelf()
            return START_NOT_STICKY
        }

        val type = if (Build.VERSION.SDK_INT >= 34) ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH else 0
        ServiceCompat.startForeground(this, NOTIFICATION_ID, buildNotification(tracker.state.value), type)
        if (registeredLatencyUs < 0) registerSensors(BACKGROUND_LATENCY_US)
        graph.trackingActive.value = true
        return START_STICKY
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        samples.close()
        scope.cancel()
        graph.trackingActive.value = false
        graph.appScope.launch { tracker.flush() }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerSensors(latencyUs: Int) {
        if (latencyUs == registeredLatencyUs) return
        sensorManager.unregisterListener(this)
        val sensor = counterSensor ?: detectorSensor
        if (sensor == null) {
            Log.e(TAG, "No step sensors available")
            return
        }
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL, latencyUs)
        registeredLatencyUs = latencyUs
    }

    override fun onSensorChanged(event: SensorEvent) {
        val ts = eventWallClockMs(event)
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> samples.trySend(Sample.Counter(event.values[0].toLong(), ts))
            Sensor.TYPE_STEP_DETECTOR -> samples.trySend(Sample.Detector(ts))
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    /** Batched events arrive late; their own timestamp says when the step really happened. */
    private fun eventWallClockMs(event: SensorEvent): Long {
        val now = System.currentTimeMillis()
        val ageMs = (SystemClock.elapsedRealtimeNanos() - event.timestamp) / 1_000_000
        return if (ageMs in 0..15 * 60_000) now - ageMs else now
    }

    private suspend fun handle(sample: Sample) {
        try {
            val update = when (sample) {
                is Sample.Counter -> {
                    val first = firstCounterEvent
                    firstCounterEvent = false
                    tracker.onRawCounter(
                        raw = sample.raw,
                        bootCount = Settings.Global.getInt(contentResolver, Settings.Global.BOOT_COUNT, -1),
                        timestampMs = sample.timestampMs,
                        firstAfterStart = first,
                        multiplier = BuildConfig.STEP_MULTIPLIER
                    )
                }
                is Sample.Detector -> tracker.onSteps(sample.timestampMs, BuildConfig.STEP_MULTIPLIER)
            }
            onUpdate(update)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling steps", e)
        }
    }

    private fun onUpdate(update: TrackerUpdate) {
        update.caught?.let { pet ->
            prefetchPetImage(this, pet.id)
            showPetCaughtNotification(pet.id, pet.name)
        }
        if (update.minuteClosed || update.walkFinished || update.caught != null) {
            getSystemService(NotificationManager::class.java)
                .notify(NOTIFICATION_ID, buildNotification(tracker.state.value))
        }
    }

    private fun createNotificationChannels() {
        val stepChannel = NotificationChannel(
            CHANNEL_ID, getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_description)
            setShowBadge(false)
        }
        val petChannel = NotificationChannel(
            PET_CAUGHT_CHANNEL_ID, getString(R.string.pet_caught_channel_name), NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.pet_caught_channel_description)
            enableVibration(true)
        }
        getSystemService(NotificationManager::class.java).apply {
            createNotificationChannel(stepChannel)
            createNotificationChannel(petChannel)
        }
    }

    private fun buildNotification(state: WalkState): Notification {
        val open = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getService(
            this, 1,
            Intent(this, StepCounterService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val text = if (state.isWalking) {
            val steps = state.sessionSteps
            "Прогулянка: ${formatSteps(steps)} ${stepsWord(steps)}, ${state.sessionMinutes} хв"
        } else {
            "Сьогодні: ${formatSteps(state.daySteps)} ${stepsWord(state.daySteps)}"
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.tracking_active))
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(open)
            .addAction(R.drawable.ic_stop, getString(R.string.stop_tracking), stop)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun showPetCaughtNotification(petId: Int, petName: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_PET_ID, petId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, petId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, PET_CAUGHT_CHANNEL_ID)
            .setContentTitle("♥ $petName хоче познайомитись!")
            .setContentText("Ви знайшли нове чарівнятко. Натисніть, щоб привітатися.")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        getSystemService(NotificationManager::class.java)
            .notify(PET_CAUGHT_NOTIFICATION_ID + petId, notification)
    }
}
