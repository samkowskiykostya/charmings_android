package com.charmings.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.charmings.app.data.model.Pet
import com.charmings.app.data.repository.PetRepository
import com.charmings.app.data.repository.StepRepository
import com.charmings.app.domain.HolidayCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

data class DashboardState(
    val totalSteps: Int = 0,
    val encouragement: String = "Ходімо на пошуки!",
    val newCatches: List<Int> = emptyList(),
    val currentDay: String = "",
    val currentDate: String = "",
    val todayHoliday: String? = null,
    val isServiceRunning: Boolean = false
)

data class CaughtPetsState(
    val pets: List<Pet> = emptyList(),
    val isLoading: Boolean = true
)

data class UncaughtPetsState(
    val pets: List<Pet> = emptyList(),
    val isLoading: Boolean = true
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val petRepository = PetRepository(application)
    private val stepRepository = StepRepository(application)
    
    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()
    
    private val _caughtPetsState = MutableStateFlow(CaughtPetsState())
    val caughtPetsState: StateFlow<CaughtPetsState> = _caughtPetsState.asStateFlow()
    
    private val _uncaughtPetsState = MutableStateFlow(UncaughtPetsState())
    val uncaughtPetsState: StateFlow<UncaughtPetsState> = _uncaughtPetsState.asStateFlow()
    
    init {
        initializeData()
        observeSteps()
        observeNewCatches()
        updateDateTime()
    }
    
    private fun initializeData() {
        viewModelScope.launch {
            petRepository.initializeIfNeeded()
            loadCaughtPets()
            loadUncaughtPets()
        }
    }
    
    private fun observeSteps() {
        viewModelScope.launch {
            stepRepository.totalStepsFlow.collectLatest { steps ->
                _dashboardState.value = _dashboardState.value.copy(totalSteps = steps)
            }
        }
        viewModelScope.launch {
            stepRepository.encouragementFlow.collectLatest { message ->
                _dashboardState.value = _dashboardState.value.copy(encouragement = message)
            }
        }
    }
    
    private fun observeNewCatches() {
        viewModelScope.launch {
            petRepository.newCatchesFlow.collectLatest { catches ->
                _dashboardState.value = _dashboardState.value.copy(newCatches = catches)
            }
        }
    }
    
    private fun updateDateTime() {
        val now = LocalDate.now()
        val dayName = translateDayToUkrainian(now.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH))
        val dateStr = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val holiday = HolidayCalculator.getTodayHoliday()
        
        _dashboardState.value = _dashboardState.value.copy(
            currentDay = dayName,
            currentDate = dateStr,
            todayHoliday = holiday
        )
    }
    
    fun loadCaughtPets() {
        viewModelScope.launch {
            _caughtPetsState.value = _caughtPetsState.value.copy(isLoading = true)
            val pets = petRepository.getCaughtPets()
            _caughtPetsState.value = CaughtPetsState(pets = pets, isLoading = false)
        }
    }
    
    fun loadUncaughtPets() {
        viewModelScope.launch {
            _uncaughtPetsState.value = _uncaughtPetsState.value.copy(isLoading = true)
            val pets = petRepository.getUncaughtPets()
            _uncaughtPetsState.value = UncaughtPetsState(pets = pets, isLoading = false)
        }
    }
    
    fun getPetById(id: Int): Pet? = petRepository.getPetById(id)
    
    fun removeFromNewCatches(petId: Int) {
        viewModelScope.launch {
            petRepository.removeFromNewCatches(petId)
        }
    }
    
    fun setServiceRunning(running: Boolean) {
        _dashboardState.value = _dashboardState.value.copy(isServiceRunning = running)
    }
    
    fun refreshData() {
        loadCaughtPets()
        loadUncaughtPets()
        updateDateTime()
    }
    
    private fun translateDayToUkrainian(day: String): String {
        return when (day.lowercase()) {
            "sunday" -> "Неділя"
            "monday" -> "Понеділок"
            "tuesday" -> "Вівторок"
            "wednesday" -> "Середа"
            "thursday" -> "Четвер"
            "friday" -> "П'ятниця"
            "saturday" -> "Субота"
            else -> day
        }
    }
}
