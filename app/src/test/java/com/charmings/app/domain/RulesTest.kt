package com.charmings.app.domain

import com.charmings.app.data.PetsData
import com.charmings.app.data.model.WeatherCondition
import com.charmings.app.data.model.WeatherCurrent
import com.charmings.app.data.model.WeatherKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class RulesTest {

    private fun pet(id: Int) = PetsData.getPetById(id)!!
    private fun weather(code: Int, temp: Double = 5.0, wind: Double = 5.0) =
        WeatherCurrent(tempC = temp, windKph = wind, condition = WeatherCondition("", code))


    @Test
    fun `orthodox easter dates`() {
        assertEquals(LocalDate.of(2025, 4, 20), HolidayCalculator.orthodoxEaster(2025))
        assertEquals(LocalDate.of(2026, 4, 12), HolidayCalculator.orthodoxEaster(2026))
        assertEquals(LocalDate.of(2027, 5, 2), HolidayCalculator.orthodoxEaster(2027))
        assertEquals(LocalDate.of(2028, 4, 16), HolidayCalculator.orthodoxEaster(2028))
    }

    @Test
    fun `light snow and showers now match`() {
        assertTrue(WeatherKind.SNOW in weather(1213).kinds)   // Light snow
        assertTrue(WeatherKind.RAIN in weather(1063).kinds)   // Patchy rain nearby
        assertTrue(WeatherKind.RAIN in weather(1240).kinds)   // Light rain shower
        assertTrue(WeatherKind.FOG in weather(1030).kinds)    // Mist
        assertTrue(RuleChecker.weatherSatisfied(pet(36).requirements, weather(1213)))
    }

    @Test
    fun `dremka needs a real kilometre now`() {
        val r = pet(60).requirements
        val now = LocalDateTime.of(2026, 3, 3, 12, 0)
        assertFalse(RuleChecker.basicSatisfied(r, 400, now))
        assertTrue(RuleChecker.basicSatisfied(r, 1300, now))
    }

    @Test
    fun `borovychok needs autumn rain, not just an evening`() {
        val r = pet(61).requirements
        assertTrue(r.needsWeather)
        assertFalse(RuleChecker.weatherSatisfied(r, weather(1000)))
        assertTrue(RuleChecker.weatherSatisfied(r, weather(1183)))
    }

    @Test
    fun `night hours wrap past midnight`() {
        val r = pet(22).requirements // 23..5
        assertTrue(RuleChecker.basicSatisfied(r, 1000, LocalDateTime.of(2026, 1, 1, 23, 30)))
        assertTrue(RuleChecker.basicSatisfied(r, 1000, LocalDateTime.of(2026, 1, 2, 4, 59)))
        assertFalse(RuleChecker.basicSatisfied(r, 1000, LocalDateTime.of(2026, 1, 2, 5, 0)))
    }

    @Test
    fun `friday the thirteenth`() {
        val r = pet(45).requirements
        assertTrue(RuleChecker.basicSatisfied(r, 1000, LocalDateTime.of(2026, 11, 13, 10, 0))) // Fri 13 Nov 2026
        assertFalse(RuleChecker.basicSatisfied(r, 1000, LocalDateTime.of(2026, 11, 12, 10, 0)))
    }

    @Test
    fun `easter pets follow the orthodox date`() {
        val r = pet(46).requirements
        assertTrue(RuleChecker.basicSatisfied(r, 1000, LocalDateTime.of(2026, 4, 12, 10, 0)))
        assertFalse(RuleChecker.basicSatisfied(r, 1000, LocalDateTime.of(2026, 4, 5, 10, 0)))
    }

    @Test
    fun `frost range has no gap below zero`() {
        assertTrue(RuleChecker.weatherSatisfied(pet(40).requirements, weather(1000, temp = -0.5)))
    }

    @Test
    fun `every pet has phrases and a unique id`() {
        assertEquals(67, PetsData.pets.map { it.id }.toSet().size)
        assertTrue(PetsData.pets.all { it.phrases.size == 3 })
    }

    @Test
    fun `ukrainian plurals`() {
        assertEquals("крок", stepsWord(1))
        assertEquals("кроки", stepsWord(3))
        assertEquals("кроків", stepsWord(5))
        assertEquals("кроків", stepsWord(12))
        assertEquals("крок", stepsWord(21))
        assertEquals("12 345", formatSteps(12345))
    }
}
