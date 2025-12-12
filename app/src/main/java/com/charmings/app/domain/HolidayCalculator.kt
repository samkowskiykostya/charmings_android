package com.charmings.app.domain

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.floor

object HolidayCalculator {
    
    private var cachedHolidays: Map<String, String>? = null
    private var cachedYear: Int? = null
    
    /**
     * Calculate Easter date using the Computus algorithm
     */
    private fun calculateEaster(year: Int): LocalDate {
        val g = year % 19
        val c = floor(year / 100.0).toInt()
        val h = (c - floor(c / 4.0).toInt() - floor((8 * c + 13) / 25.0).toInt() + 19 * g + 15) % 30
        val i = h - floor(h / 28.0).toInt() * (1 - floor(29.0 / (h + 1)).toInt() * floor((21 - g) / 11.0).toInt())
        val j = (year + floor(year / 4.0).toInt() + i + 2 - c + floor(c / 4.0).toInt()) % 7
        val l = i - j
        val month = 3 + floor((l + 40) / 44.0).toInt()
        val day = l + 28 - 31 * floor(month / 4.0).toInt()
        
        return LocalDate.of(year, month, day)
    }
    
    /**
     * Get holidays for the current year
     * Returns map of holiday name (lowercase) to date string (YYYY-MM-DD)
     */
    fun getHolidays(): Map<String, String> {
        val currentYear = LocalDate.now().year
        
        if (cachedHolidays != null && cachedYear == currentYear) {
            return cachedHolidays!!
        }
        
        val easterDate = calculateEaster(currentYear)
        val pentecostDate = easterDate.plusDays(49)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        
        cachedHolidays = mapOf(
            "новий рік" to "$currentYear-01-01",
            "день жінки" to "$currentYear-03-08",
            "великдень" to easterDate.format(formatter),
            "трійця" to pentecostDate.format(formatter),
            "день працівника" to "$currentYear-05-01",
            "різдво" to "$currentYear-12-25",
            "день тетяни" to "$currentYear-02-12",
            "день святого валентина" to "$currentYear-02-14",
            "хелловін" to "$currentYear-10-31",
            "івана купала" to "$currentYear-06-20",
            "день знань" to "$currentYear-09-01"
        )
        cachedYear = currentYear
        
        return cachedHolidays!!
    }
    
    /**
     * Check if today is a specific holiday
     */
    fun isHolidayToday(holidayName: String): Boolean {
        val holidays = getHolidays()
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val holidayDate = holidays[holidayName.lowercase()]
        return holidayDate == today
    }
    
    /**
     * Get today's holiday name if any
     */
    fun getTodayHoliday(): String? {
        val holidays = getHolidays()
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        return holidays.entries.find { it.value == today }?.key?.replaceFirstChar { it.uppercase() }
    }
}
