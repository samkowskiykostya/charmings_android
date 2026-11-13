package com.charmings.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import com.charmings.app.service.StepCounterService
import com.charmings.app.ui.navigation.MainNavigation
import com.charmings.app.ui.theme.CharmingsTheme
import com.charmings.app.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    /** Pet to open from a "caught" notification; consumed by the navigation once shown. */
    private val pendingPetId = mutableStateOf<Int?>(null)
    private var askedForActivityPermission = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Only activity recognition is required. Notifications and location are nice-to-have:
        // denying them must not block step tracking.
        refreshPermission()
        if (hasActivityPermission() && application.graph.settings.trackingEnabled) {
            StepCounterService.start(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) handleIntent(intent)

        setContent {
            CharmingsTheme {
                MainNavigation(
                    viewModel = viewModel,
                    pendingPetId = pendingPetId.value,
                    onPendingPetShown = { pendingPetId.value = null },
                    onStartTracking = ::startTracking,
                    onStopTracking = ::stopTracking
                )
            }
        }

        if (savedInstanceState == null) requestPermissionsAndStart()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        refreshPermission()
    }

    override fun onStart() {
        super.onStart()
        StepCounterService.setUiVisible(this, true)
    }

    override fun onStop() {
        StepCounterService.setUiVisible(this, false)
        super.onStop()
    }

    private fun handleIntent(intent: Intent?) {
        val id = intent?.getIntExtra(StepCounterService.EXTRA_PET_ID, -1) ?: -1
        if (id >= 0) pendingPetId.value = id
    }

    private fun hasActivityPermission() = StepCounterService.hasActivityPermission(this)

    private fun refreshPermission() = viewModel.refresh(hasActivityPermission())

    private fun requestPermissionsAndStart() {
        val wanted = buildList {
            add(Manifest.permission.ACTIVITY_RECOGNITION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }.filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }

        if (wanted.isNotEmpty()) {
            askedForActivityPermission = Manifest.permission.ACTIVITY_RECOGNITION in wanted
            permissionLauncher.launch(wanted.toTypedArray())
        } else if (application.graph.settings.trackingEnabled) {
            StepCounterService.start(this)
        }
    }

    private fun startTracking() {
        viewModel.setTrackingEnabled(true)
        when {
            hasActivityPermission() -> StepCounterService.start(this)
            // After a permanent denial the system dialog no longer appears: send the user to settings.
            askedForActivityPermission &&
                !shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION) ->
                startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
                )
            else -> requestPermissionsAndStart()
        }
    }

    private fun stopTracking() {
        viewModel.setTrackingEnabled(false)
        StepCounterService.stop(this)
    }
}
