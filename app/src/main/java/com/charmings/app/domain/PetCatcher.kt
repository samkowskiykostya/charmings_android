package com.charmings.app.domain

import com.charmings.app.data.api.WeatherApi
import com.charmings.app.data.model.Pet
import com.charmings.app.data.model.Requirement
import com.charmings.app.data.model.WeatherCurrent
import com.charmings.app.data.repository.PetRepository
import com.charmings.app.data.repository.StepRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale
import kotlin.random.Random

class PetCatcher(
    private val petRepository: PetRepository,
    private val stepRepository: StepRepository,
    private val weatherApi: WeatherApi
) {
    
    companion object {
        const val STEP_THRESHOLD_FOR_PET_CHECK = 400
        const val STEP_MIN_THRESHOLD_FOR_WALK = 250
        const val PET_CHECK_INTERVAL_MS = 3 * 60 * 1000L // 3 minutes
        const val STEPS_TIME_WINDOW = 10 * 60 * 1000L // 10 minutes
        const val MIN_STEPS_IN_WINDOW = 20
        
        val CAUGHT_LABELS = listOf(
            "Вітаємо з новим другом!",
            "Сьогодні Вам щастить!",
            "Ого! Яка чудова знахідка!",
            "Є! Спіймали пустунця!",
            "Ура! Новий друг з нами!",
            "Ось він, наш скарб!"
        )
        
        val MISSED_LABELS = listOf(
            "Тут щойно хтось був! Але втік..",
            "Тільки якась тінь майнула..",
            "Ой, лапки промайнули за рогом!",
            "Хвостик майнув і зник!"
        )
        
        val INACTIVE_LABELS = listOf(
            "Схоже ви призупинили пошуки, але ще не всі звірята знайдені!",
            "Наші пухнасті друзі все ще десь ховаються!",
            "Ой, перерва? А хвостаті ще чекають на зустріч!",
            "Десь тут бігають самотні звірятка, час їх знайти!",
            "Ще стільки хвостиків можна знайти!"
        )
    }
    
    /**
     * Check for caught pets and return the caught pet if any
     */
    suspend fun checkForCaughtPets(): Pet? {
        val caughtIds = petRepository.getCaughtPetIds()
        val uncaughtPets = petRepository.getAllPets().filter { !caughtIds.contains(it.id) }
        
        // Sort pets - those requiring weather last (to minimize API calls)
        val sortedPets = uncaughtPets.sortedBy { pet ->
            pet.requirements.any { req ->
                req.weather != null || req.temperature != null || req.windDirection != null || req.windSpeed != null
            }
        }
        
        for (pet in sortedPets) {
            if (areRequirementsSatisfied(pet.requirements)) {
                val randomValue = Random.nextDouble()
                if (randomValue <= pet.probability) {
                    // Pet caught!
                    petRepository.addCaughtPet(pet.id)
                    petRepository.addNewCatch(pet.id)
                    stepRepository.setEncouragement(CAUGHT_LABELS.random())
                    return pet
                } else {
                    // Almost caught
                    stepRepository.setEncouragement(MISSED_LABELS.random())
                }
            }
        }
        return null
    }
    
    /**
     * Check if all requirements are satisfied
     */
    private suspend fun areRequirementsSatisfied(requirements: List<Requirement>): Boolean {
        val totalSteps = stepRepository.getTotalSteps()
        
        // Check steps requirement first
        val stepsSatisfied = requirements.all { req ->
            req.steps == null || totalSteps >= req.steps
        }
        if (!stepsSatisfied) return false
        
        val now = LocalDateTime.now()
        val currentDay = now.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH).lowercase()
        val currentMonth = now.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH).lowercase()
        val currentHour = now.hour
        val currentDateDay = now.dayOfMonth
        val holidays = HolidayCalculator.getHolidays()
        val today = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        
        // Check non-weather requirements
        val basicSatisfied = requirements.all { req ->
            // Day check (can be single day or comma-separated list)
            if (req.day != null) {
                val days = req.day.split(",").map { it.trim().lowercase() }
                if (!days.contains(currentDay)) return@all false
            }
            
            // Month check
            if (req.month != null) {
                if (!req.month.map { it.lowercase() }.contains(currentMonth)) return@all false
            }
            
            // Time check (handles wraparound like 23-5)
            if (req.time != null && req.time.size >= 2) {
                val startHour = req.time[0]
                val endHour = req.time[1]
                val inRange = if (startHour <= endHour) {
                    currentHour in startHour until endHour
                } else {
                    // Wraparound (e.g., 23-5 means 23,0,1,2,3,4)
                    currentHour >= startHour || currentHour < endHour
                }
                if (!inRange) return@all false
            }
            
            // Date day check
            if (req.dateday != null) {
                if (currentDateDay != req.dateday) return@all false
            }
            
            // Holiday check
            if (req.holiday != null) {
                val holidayDate = holidays[req.holiday.lowercase()]
                if (holidayDate != today) return@all false
            }
            
            true
        }
        
        if (!basicSatisfied) return false
        
        // Check weather requirements if needed
        val needsWeather = requirements.any { req ->
            req.weather != null || req.temperature != null || req.windDirection != null || req.windSpeed != null
        }
        
        if (needsWeather) {
            val weather = weatherApi.getCurrentWeather() ?: return false
            return checkWeatherRequirements(requirements, weather)
        }
        
        return true
    }
    
    private fun checkWeatherRequirements(requirements: List<Requirement>, weather: WeatherCurrent): Boolean {
        return requirements.all { req ->
            // Weather condition check
            if (req.weather != null) {
                val conditions = req.weather.map { it.lowercase().trim() }
                if (!conditions.contains(weather.condition.text.lowercase().trim())) return@all false
            }
            
            // Temperature check
            if (req.temperature != null && req.temperature.size >= 2) {
                val minTemp = req.temperature[0]
                val maxTemp = req.temperature[1]
                val currentTemp = weather.tempC
                
                if (minTemp != "_") {
                    val min = minTemp.toDoubleOrNull()
                    if (min != null && currentTemp < min) return@all false
                }
                if (maxTemp != "_") {
                    val max = maxTemp.toDoubleOrNull()
                    if (max != null && currentTemp > max) return@all false
                }
            }
            
            // Wind direction check
            if (req.windDirection != null) {
                val directions = req.windDirection.map { it.uppercase() }
                if (!directions.contains(weather.windDir.uppercase())) return@all false
            }
            
            // Wind speed check
            if (req.windSpeed != null && req.windSpeed.size >= 2) {
                val minSpeed = req.windSpeed[0]
                val maxSpeed = req.windSpeed[1]
                val currentSpeed = weather.windKph
                
                if (minSpeed != "_") {
                    val min = minSpeed.toDoubleOrNull()
                    if (min != null && currentSpeed < min) return@all false
                }
                if (maxSpeed != "_") {
                    val max = maxSpeed.toDoubleOrNull()
                    if (max != null && currentSpeed > max) return@all false
                }
            }
            
            true
        }
    }
    
    /**
     * Handle step update and check for pets if threshold reached
     */
    suspend fun handleStepUpdate(stepsDifference: Int): Pet? {
        val currentTime = System.currentTimeMillis()
        
        // Add steps to total
        stepRepository.addSteps(stepsDifference)
        
        // Add to step history
        val entry = com.charmings.app.data.repository.StepEntry(currentTime, stepsDifference)
        stepRepository.addStepEntry(entry)
        
        // Clean old entries and check for inactivity
        val history = stepRepository.getStepHistory().toMutableList()
        val cutoffTime = currentTime - STEPS_TIME_WINDOW
        
        if (history.isNotEmpty() && history.first().timestamp <= cutoffTime) {
            val filteredHistory = history.filter { it.timestamp >= cutoffTime }
            stepRepository.setStepHistory(filteredHistory)
            
            val stepsInWindow = filteredHistory.sumOf { it.steps }
            if (stepsInWindow < MIN_STEPS_IN_WINDOW) {
                // User is inactive
                val totalSteps = stepRepository.getTotalSteps()
                if (totalSteps >= STEP_MIN_THRESHOLD_FOR_WALK) {
                    stepRepository.setEncouragement(INACTIVE_LABELS.random())
                }
                stepRepository.resetSteps()
                stepRepository.clearLastPetCheck()
            }
        }
        
        // Check for pets if threshold reached
        val totalSteps = stepRepository.getTotalSteps()
        if (totalSteps >= STEP_THRESHOLD_FOR_PET_CHECK) {
            val lastCheck = stepRepository.getLastPetCheck()
            val canCheck = lastCheck == null || (currentTime - lastCheck) > PET_CHECK_INTERVAL_MS
            
            if (canCheck) {
                stepRepository.setLastPetCheck(currentTime)
                
                val caughtPet = checkForCaughtPets()
                if (caughtPet != null) {
                    // Reset on catch
                    stepRepository.resetSteps()
                    stepRepository.clearLastPetCheck()
                    return caughtPet
                }
            }
        }
        
        return null
    }
}
