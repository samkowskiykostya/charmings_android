package com.charmings.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.charmings.app.MainActivity
import com.charmings.app.R
import com.charmings.app.data.api.WeatherApi
import com.charmings.app.data.repository.PetRepository
import com.charmings.app.data.repository.StepRepository
import com.charmings.app.domain.PetCatcher
import com.charmings.app.receiver.StopServiceReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StepCounterService : Service(), SensorEventListener {
    
    companion object {
        private const val TAG = "StepCounterService"
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "step_counter_channel"
        const val PET_CAUGHT_CHANNEL_ID = "pet_caught_channel"
        const val PET_CAUGHT_NOTIFICATION_ID = 2
        
        var isRunning = false
            private set
    }
    
    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null
    
    private lateinit var petRepository: PetRepository
    private lateinit var stepRepository: StepRepository
    private lateinit var weatherApi: WeatherApi
    private lateinit var petCatcher: PetCatcher
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private var initialStepCount: Int = -1
    private var lastStepCount: Int = 0
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        
        // Initialize repositories
        petRepository = PetRepository(applicationContext)
        stepRepository = StepRepository(applicationContext)
        weatherApi = WeatherApi()
        petCatcher = PetCatcher(petRepository, stepRepository, weatherApi)
        
        // Initialize sensor manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        
        // Create notification channels
        createNotificationChannels()
        
        // Initialize pet repository
        serviceScope.launch {
            petRepository.initializeIfNeeded()
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand")
        
        // Start as foreground service
        startForeground(NOTIFICATION_ID, createNotification())
        
        // Register sensor listeners
        registerSensorListeners()
        
        isRunning = true
        
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy")
        
        // Unregister sensor listeners
        sensorManager.unregisterListener(this)
        
        // Cancel coroutines
        serviceScope.cancel()
        
        isRunning = false
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannels() {
        // Step counter channel
        val stepChannel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_description)
            setShowBadge(false)
        }
        
        // Pet caught channel
        val petChannel = NotificationChannel(
            PET_CAUGHT_CHANNEL_ID,
            getString(R.string.pet_caught_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.pet_caught_channel_description)
            enableVibration(true)
        }
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(stepChannel)
        notificationManager.createNotificationChannel(petChannel)
    }
    
    private fun createNotification(): Notification {
        // Intent to open the app
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Intent to stop the service
        val stopIntent = Intent(this, StopServiceReceiver::class.java)
        val stopPendingIntent = PendingIntent.getBroadcast(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.tracking_active))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openPendingIntent)
            .addAction(R.drawable.ic_stop, getString(R.string.stop_tracking), stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    private fun registerSensorListeners() {
        // Prefer step counter sensor (more accurate for total count)
        stepCounterSensor?.let { sensor ->
            sensorManager.registerListener(
                this, sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            Log.d(TAG, "Registered step counter sensor")
        }
        
        // Also register step detector as backup
        if (stepCounterSensor == null) {
            stepDetectorSensor?.let { sensor ->
                sensorManager.registerListener(
                    this, sensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
                Log.d(TAG, "Registered step detector sensor")
            }
        }
        
        if (stepCounterSensor == null && stepDetectorSensor == null) {
            Log.e(TAG, "No step sensors available!")
        }
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val totalSteps = event.values[0].toInt()
                
                if (initialStepCount < 0) {
                    // First reading - initialize
                    initialStepCount = totalSteps
                    lastStepCount = totalSteps
                    Log.d(TAG, "Initial step count: $initialStepCount")
                    return
                }
                
                val stepsDifference = totalSteps - lastStepCount
                if (stepsDifference > 0) {
                    lastStepCount = totalSteps
                    Log.d(TAG, "Steps difference: $stepsDifference, Total: $totalSteps")
                    handleSteps(stepsDifference)
                }
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                // Each event is one step
                Log.d(TAG, "Step detected")
                handleSteps(1)
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "Sensor accuracy changed: $accuracy")
    }
    
    private fun handleSteps(steps: Int) {
        serviceScope.launch {
            try {
                val caughtPet = petCatcher.handleStepUpdate(steps)
                
                if (caughtPet != null) {
                    Log.d(TAG, "Pet caught: ${caughtPet.name}")
                    showPetCaughtNotification(caughtPet.id, caughtPet.name)
                }
                
                // Update notification with current step count
                val totalSteps = stepRepository.totalStepsFlow.first()
                updateNotification(totalSteps)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error handling steps", e)
            }
        }
    }
    
    private fun updateNotification(steps: Int) {
        // Intent to open the app
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Intent to stop the service
        val stopIntent = Intent(this, StopServiceReceiver::class.java)
        val stopPendingIntent = PendingIntent.getBroadcast(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("$steps кроків")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openPendingIntent)
            .addAction(R.drawable.ic_stop, getString(R.string.stop_tracking), stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun showPetCaughtNotification(petId: Int, petName: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("petId", petId)
            putExtra("celebrate", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, petId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, PET_CAUGHT_CHANNEL_ID)
            .setContentTitle("♥ Ви впіймали нове чарівнятко! ♥")
            .setContentText("Натисніть щоб познайомитись ближче")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(PET_CAUGHT_NOTIFICATION_ID + petId, notification)
    }
}
