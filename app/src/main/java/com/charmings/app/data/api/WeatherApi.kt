package com.charmings.app.data.api

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import com.charmings.app.BuildConfig
import com.charmings.app.data.model.WeatherCurrent
import com.charmings.app.data.model.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class WeatherApi(private val context: Context) {

    companion object {
        private const val TAG = "WeatherApi"
        private const val BASE_URL = "https://api.weatherapi.com/v1/current.json"
        private const val CACHE_DURATION_MS = 30 * 60 * 1000L
        private const val KYIV_LAT = 50.4501
        private const val KYIV_LON = 30.5234
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val mutex = Mutex()

    private var cachedWeather: WeatherCurrent? = null
    private var lastFetchTime: Long = 0

    suspend fun getCurrentWeather(): WeatherCurrent? = mutex.withLock {
        val now = System.currentTimeMillis()
        cachedWeather?.takeIf { now - lastFetchTime < CACHE_DURATION_MS }?.let { return it }
        if (BuildConfig.WEATHER_API_KEY.isBlank()) return null

        withContext(Dispatchers.IO) {
            try {
                val loc = lastKnownLocation()
                val q = loc?.let { "%.2f,%.2f".format(java.util.Locale.US, it.latitude, it.longitude) }
                    ?: "$KYIV_LAT,$KYIV_LON"
                val request = Request.Builder()
                    .url("$BASE_URL?key=${BuildConfig.WEATHER_API_KEY}&q=$q")
                    .get()
                    .build()
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val current = json.decodeFromString<WeatherResponse>(body).current
                        cachedWeather = current
                        lastFetchTime = now
                        current
                    } else {
                        Log.w(TAG, "Weather request failed: ${response.code}")
                        cachedWeather
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Weather request error", e)
                cachedWeather
            }
        }
    }

    /**
     * Cheapest possible location: the last fix any app already obtained. No GPS is switched on.
     * City-level precision is plenty for weather. Falls back to Kyiv without permission.
     */
    private fun lastKnownLocation(): Location? {
        val granted = context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) return null
        val lm = context.getSystemService(LocationManager::class.java) ?: return null
        return try {
            listOf(LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER, LocationManager.FUSED_PROVIDER)
                .mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
                .maxByOrNull { it.time }
        } catch (e: SecurityException) {
            null
        }
    }
}
