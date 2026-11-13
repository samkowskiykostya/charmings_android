package com.charmings.app.data.model

data class Pet(
    val id: Int,
    val name: String,
    val description: String,
    val story: String,
    val requirementsDescription: String,
    /** Chance to catch, rolled once per walk when all requirements are met. */
    val probability: Double,
    val requirements: Requirement,
    /** Short lines the pet says when tapped. */
    val phrases: List<String> = emptyList()
) {
    val needsWeather: Boolean get() = requirements.needsWeather
}

enum class Day { MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY }

/**
 * All conditions must hold at the same time. `null` means "any".
 * Steps are counted only inside a real walk session (see WalkEngine), since the last catch.
 */
data class Requirement(
    val steps: Int = 0,
    val days: Set<Day>? = null,
    val months: Set<Int>? = null,          // 1..12
    val hours: Pair<Int, Int>? = null,     // [start, end) in hours; wraps past midnight if start > end
    val dayOfMonth: Int? = null,
    val holiday: Holiday? = null,
    val weather: Set<WeatherKind>? = null,
    val minTempC: Double? = null,
    val maxTempC: Double? = null,
    val minWindKph: Double? = null
) {
    val needsWeather: Boolean
        get() = weather != null || minTempC != null || maxTempC != null || minWindKph != null
}

enum class Holiday { NEW_YEAR, VALENTINE, EASTER, KUPALA, KNOWLEDGE_DAY, HALLOWEEN, CHRISTMAS }

val WINTER = setOf(12, 1, 2)
val SPRING = setOf(3, 4, 5)
val SUMMER = setOf(6, 7, 8)
val AUTUMN = setOf(9, 10, 11)
