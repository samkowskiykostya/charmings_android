package com.charmings.app.domain

import com.charmings.app.data.model.Day
import com.charmings.app.data.model.Requirement
import com.charmings.app.data.model.WeatherCurrent
import java.time.LocalDateTime

/** Pure evaluation of pet requirements; no I/O. */
object RuleChecker {

    /** Everything except weather. Cheap — evaluate first so weather is fetched only when needed. */
    fun basicSatisfied(req: Requirement, steps: Int, now: LocalDateTime): Boolean {
        if (steps < req.steps) return false
        req.days?.let { days -> if (Day.values()[now.dayOfWeek.value - 1] !in days) return false }
        req.months?.let { if (now.monthValue !in it) return false }
        req.dayOfMonth?.let { if (now.dayOfMonth != it) return false }
        req.hours?.let { (start, end) ->
            val h = now.hour
            val inRange = if (start <= end) h in start until end else h >= start || h < end
            if (!inRange) return false
        }
        req.holiday?.let { if (!HolidayCalculator.isToday(it, now.toLocalDate())) return false }
        return true
    }

    fun weatherSatisfied(req: Requirement, weather: WeatherCurrent): Boolean {
        req.weather?.let { wanted -> if (weather.kinds.none { it in wanted }) return false }
        req.minTempC?.let { if (weather.tempC < it) return false }
        req.maxTempC?.let { if (weather.tempC > it) return false }
        req.minWindKph?.let { if (weather.windKph < it) return false }
        return true
    }
}
