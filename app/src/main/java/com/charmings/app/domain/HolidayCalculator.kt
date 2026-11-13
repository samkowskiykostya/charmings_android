package com.charmings.app.domain

import com.charmings.app.data.model.Holiday
import java.time.LocalDate
import java.time.MonthDay

/**
 * Ukrainian holidays. Since 2023 fixed-date church feasts follow the New Julian calendar
 * (Christmas 25 Dec, Kupala 24 Jun), while Easter is still computed with the Julian paschalion.
 */
object HolidayCalculator {

    /** Orthodox (Julian) Easter as a Gregorian date. Valid for 1900–2099 (+13 days offset). */
    fun orthodoxEaster(year: Int): LocalDate {
        val a = year % 4
        val b = year % 7
        val c = year % 19
        val d = (19 * c + 15) % 30
        val e = (2 * a + 4 * b - d + 34) % 7
        val month = (d + e + 114) / 31
        val day = (d + e + 114) % 31 + 1
        return LocalDate.of(year, month, day).plusDays(13)
    }

    fun dateOf(holiday: Holiday, year: Int): LocalDate = when (holiday) {
        Holiday.NEW_YEAR -> LocalDate.of(year, 1, 1)
        Holiday.VALENTINE -> LocalDate.of(year, 2, 14)
        Holiday.EASTER -> orthodoxEaster(year)
        Holiday.KUPALA -> LocalDate.of(year, 6, 24)
        Holiday.KNOWLEDGE_DAY -> LocalDate.of(year, 9, 1)
        Holiday.HALLOWEEN -> LocalDate.of(year, 10, 31)
        Holiday.CHRISTMAS -> LocalDate.of(year, 12, 25)
    }

    fun isToday(holiday: Holiday, today: LocalDate = LocalDate.now()): Boolean =
        dateOf(holiday, today.year) == today

    /** Display name for the dashboard, including days without a pet attached. */
    fun todayHolidayName(today: LocalDate = LocalDate.now()): String? {
        val md = MonthDay.from(today)
        val easter = orthodoxEaster(today.year)
        return when {
            today == easter -> "Великдень"
            today == easter.plusDays(49) -> "Трійця"
            md == MonthDay.of(1, 1) -> "Новий рік"
            md == MonthDay.of(2, 14) -> "День святого Валентина"
            md == MonthDay.of(3, 8) -> "Міжнародний жіночий день"
            md == MonthDay.of(6, 24) -> "Івана Купала"
            md == MonthDay.of(9, 1) -> "День знань"
            md == MonthDay.of(10, 31) -> "Хелловін"
            md == MonthDay.of(12, 25) -> "Різдво"
            else -> null
        }
    }
}
