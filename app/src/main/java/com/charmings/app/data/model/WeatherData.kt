package com.charmings.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    val current: WeatherCurrent
)

@Serializable
data class WeatherCurrent(
    @SerialName("temp_c") val tempC: Double,
    @SerialName("wind_kph") val windKph: Double,
    @SerialName("wind_dir") val windDir: String,
    val condition: WeatherCondition
)

@Serializable
data class WeatherCondition(
    val text: String
)
