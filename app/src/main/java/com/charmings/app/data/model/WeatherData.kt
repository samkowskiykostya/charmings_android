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
    @SerialName("wind_dir") val windDir: String = "",
    @SerialName("is_day") val isDay: Int = 1,
    val condition: WeatherCondition
) {
    val kinds: Set<WeatherKind> get() = WeatherKind.fromCode(condition.code)
}

@Serializable
data class WeatherCondition(
    val text: String = "",
    val code: Int = 0
)

/**
 * Weather categories matched by WeatherAPI condition *codes* (stable), not by English text
 * (which changes, e.g. "Patchy rain possible" → "Patchy rain nearby").
 * https://www.weatherapi.com/docs/weather_conditions.json
 */
enum class WeatherKind {
    CLEAR, CLOUDY, FOG, RAIN, SNOW, BLIZZARD;

    companion object {
        private val CLOUDY_CODES = setOf(1003, 1006, 1009)
        private val FOG_CODES = setOf(1030, 1135, 1147)
        private val RAIN_CODES = setOf(
            1063, 1072, 1150, 1153, 1168, 1171, 1180, 1183, 1186, 1189, 1192, 1195,
            1198, 1201, 1240, 1243, 1246, 1273, 1276
        )
        private val SNOW_CODES = setOf(
            1066, 1069, 1114, 1117, 1204, 1207, 1210, 1213, 1216, 1219, 1222, 1225,
            1237, 1249, 1252, 1255, 1258, 1261, 1264, 1279, 1282
        )
        private val BLIZZARD_CODES = setOf(1114, 1117)

        fun fromCode(code: Int): Set<WeatherKind> = buildSet {
            if (code == 1000) add(CLEAR)  // "Sunny" by day, "Clear" by night
            if (code in CLOUDY_CODES) add(CLOUDY)
            if (code in FOG_CODES) add(FOG)
            if (code in RAIN_CODES) add(RAIN)
            if (code in SNOW_CODES) add(SNOW)
            if (code in BLIZZARD_CODES) add(BLIZZARD)
        }
    }
}
