package com.charmings.app

import android.app.Application
import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.imageLoader
import coil.request.ImageRequest
import com.charmings.app.data.api.WeatherApi
import com.charmings.app.data.repository.PetRepository
import com.charmings.app.data.repository.SettingsRepository
import com.charmings.app.data.repository.WalkRepository
import com.charmings.app.domain.PetCatcher
import com.charmings.app.domain.WalkTracker
import com.charmings.app.ui.images.PetImages
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CharmingsApplication : Application(), ImageLoaderFactory {

    lateinit var graph: AppGraph
        private set

    override fun onCreate() {
        super.onCreate()
        graph = AppGraph(this)
        graph.appScope.launch {
            graph.petRepository.initializeIfNeeded()
            graph.tracker.ensureLoaded()
            // Make sure every pet already caught has its picture available offline.
            graph.petRepository.getCaughtPetIds().forEach { prefetchPetImage(this@CharmingsApplication, it) }
        }
    }

    /** Pet images are immutable, so ignore HTTP cache headers: once downloaded, always offline. */
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
}

/** Manual DI: one instance of each, shared by the service and the UI in the same process. */
class AppGraph(context: Context) {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val petRepository = PetRepository(context)
    val walkRepository = WalkRepository(context)
    val settings = SettingsRepository(context)
    val weatherApi = WeatherApi(context)
    val petCatcher = PetCatcher(petRepository, walkRepository, weatherApi)
    val tracker = WalkTracker(walkRepository, petCatcher)

    /** True while the step service is alive (replaces the static flag + broadcast). */
    val trackingActive = MutableStateFlow(false)
}

val Context.graph: AppGraph get() = (applicationContext as CharmingsApplication).graph

/** Downloads the full image into Coil's disk cache (no-op if already cached or bundled). */
fun prefetchPetImage(context: Context, petId: Int) {
    if (PetImages.bundled(petId) != null) return
    context.imageLoader.enqueue(
        ImageRequest.Builder(context).data(PetImages.url(petId)).build()
    )
}
