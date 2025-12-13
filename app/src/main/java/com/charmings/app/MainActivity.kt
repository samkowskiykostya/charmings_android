package com.charmings.app

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.charmings.app.service.StepCounterService
import com.charmings.app.ui.navigation.MainNavigation
import com.charmings.app.ui.theme.CharmingsTheme
import com.charmings.app.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()
    
    private var initialPetId: Int? = null
    private var initialCelebrate: Boolean = false
    
    private val serviceStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val isRunning = intent?.getBooleanExtra(StepCounterService.EXTRA_IS_RUNNING, false) ?: false
            viewModel.setServiceRunning(isRunning)
        }
    }
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            startStepCounterService()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle intent extras (from notification)
        handleIntent(intent)
        
        setContent {
            CharmingsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation(
                        viewModel = viewModel,
                        initialPetId = initialPetId,
                        initialCelebrate = initialCelebrate,
                        onStartTracking = { startStepCounterService() }
                    )
                }
            }
        }
        
        // Request permissions and start service
        requestPermissionsAndStartService()
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(it) }
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.refreshData()
        viewModel.setServiceRunning(StepCounterService.isRunning)
        
        // Register receiver for service state changes
        val filter = IntentFilter(StepCounterService.ACTION_SERVICE_STATE_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(serviceStateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(serviceStateReceiver, filter)
        }
    }
    
    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(serviceStateReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver not registered
        }
    }
    
    private fun handleIntent(intent: Intent) {
        initialPetId = if (intent.hasExtra("petId")) {
            intent.getIntExtra("petId", -1).takeIf { it >= 0 }
        } else null
        initialCelebrate = intent.getBooleanExtra("celebrate", false)
    }
    
    private fun requestPermissionsAndStartService() {
        val permissionsToRequest = mutableListOf<String>()
        
        // Activity recognition permission (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
        
        // Notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            startStepCounterService()
        }
    }
    
    private fun startStepCounterService() {
        if (!StepCounterService.isRunning) {
            val serviceIntent = Intent(this, StepCounterService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        }
        viewModel.setServiceRunning(true)
    }
}
