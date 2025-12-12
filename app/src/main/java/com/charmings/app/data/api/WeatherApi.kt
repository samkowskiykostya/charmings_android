package com.charmings.app.data.api

import com.charmings.app.data.model.WeatherCurrent
import com.charmings.app.data.model.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class WeatherApi {
    
    companion object {
        private const val API_KEY = "eac7af031d9044f8beb43052243001"
        private const val BASE_URL = "https://api.weatherapi.com/v1/current.json"
        private const val CACHE_DURATION_MS = 60 * 60 * 1000L // 60 minutes
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }
    
    private var cachedWeather: WeatherCurrent? = null
    private var lastFetchTime: Long = 0
    
    suspend fun getCurrentWeather(latitude: Double = 50.4501, longitude: Double = 30.5234): WeatherCurrent? {
        return withContext(Dispatchers.IO) {
            // Check cache
            val now = System.currentTimeMillis()
            if (cachedWeather != null && (now - lastFetchTime) < CACHE_DURATION_MS) {
                return@withContext cachedWeather
            }
            
            try {
                val url = "$BASE_URL?key=$API_KEY&q=$latitude,$longitude"
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()
                
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        val weatherResponse = json.decodeFromString<WeatherResponse>(body)
                        cachedWeather = weatherResponse.current
                        lastFetchTime = now
                        return@withContext weatherResponse.current
                    }
                }
                null
            } catch (e: Exception) {
                e.printStackTrace()
                cachedWeather // Return cached data on error
            }
        }
    }
}
