package com.charmings.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.stepDataStore: DataStore<Preferences> by preferencesDataStore(name = "step_prefs")

@Serializable
data class StepEntry(
    val timestamp: Long,
    val steps: Int
)

class StepRepository(private val context: Context) {
    
    companion object {
        private val TOTAL_STEPS_KEY = intPreferencesKey("total_steps")
        private val LAST_PET_CHECK_KEY = longPreferencesKey("last_pet_check")
        private val STEP_HISTORY_KEY = stringPreferencesKey("step_history")
        private val ENCOURAGEMENT_KEY = stringPreferencesKey("encouragement")
        private val ENCOURAGEMENT_TIME_KEY = longPreferencesKey("encouragement_time")
    }
    
    private val json = Json { ignoreUnknownKeys = true }
    
    // Total steps flow
    val totalStepsFlow: Flow<Int> = context.stepDataStore.data.map { preferences ->
        preferences[TOTAL_STEPS_KEY] ?: 0
    }
    
    // Encouragement flow
    val encouragementFlow: Flow<String> = context.stepDataStore.data.map { preferences ->
        preferences[ENCOURAGEMENT_KEY] ?: getRandomStartLabel()
    }
    
    suspend fun getTotalSteps(): Int {
        return totalStepsFlow.first()
    }
    
    suspend fun setTotalSteps(steps: Int) {
        context.stepDataStore.edit { preferences ->
            preferences[TOTAL_STEPS_KEY] = steps
        }
    }
    
    suspend fun addSteps(steps: Int) {
        val current = getTotalSteps()
        setTotalSteps(current + steps)
    }
    
    suspend fun resetSteps() {
        setTotalSteps(0)
    }
    
    suspend fun getLastPetCheck(): Long? {
        return context.stepDataStore.data.first()[LAST_PET_CHECK_KEY]
    }
    
    suspend fun setLastPetCheck(time: Long) {
        context.stepDataStore.edit { preferences ->
            preferences[LAST_PET_CHECK_KEY] = time
        }
    }
    
    suspend fun clearLastPetCheck() {
        context.stepDataStore.edit { preferences ->
            preferences.remove(LAST_PET_CHECK_KEY)
        }
    }
    
    suspend fun getStepHistory(): List<StepEntry> {
        val jsonString = context.stepDataStore.data.first()[STEP_HISTORY_KEY] ?: "[]"
        return try {
            json.decodeFromString<List<StepEntry>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun addStepEntry(entry: StepEntry) {
        val history = getStepHistory().toMutableList()
        history.add(entry)
        context.stepDataStore.edit { preferences ->
            preferences[STEP_HISTORY_KEY] = json.encodeToString(history)
        }
    }
    
    suspend fun setStepHistory(history: List<StepEntry>) {
        context.stepDataStore.edit { preferences ->
            preferences[STEP_HISTORY_KEY] = json.encodeToString(history)
        }
    }
    
    suspend fun getEncouragement(): String {
        return context.stepDataStore.data.first()[ENCOURAGEMENT_KEY] ?: getRandomStartLabel()
    }
    
    suspend fun getEncouragementTime(): Long {
        return context.stepDataStore.data.first()[ENCOURAGEMENT_TIME_KEY] ?: 0L
    }
    
    suspend fun setEncouragement(message: String) {
        val lastTime = getEncouragementTime()
        val now = System.currentTimeMillis()
        // Only update if 60 seconds have passed
        if (now - lastTime >= 60000) {
            context.stepDataStore.edit { preferences ->
                preferences[ENCOURAGEMENT_KEY] = message
                preferences[ENCOURAGEMENT_TIME_KEY] = now
            }
        }
    }
    
    suspend fun forceSetEncouragement(message: String) {
        context.stepDataStore.edit { preferences ->
            preferences[ENCOURAGEMENT_KEY] = message
            preferences[ENCOURAGEMENT_TIME_KEY] = System.currentTimeMillis()
        }
    }
    
    private fun getRandomStartLabel(): String {
        return listOf(
            "Ходімо на пошуки!",
            "Ходімо шукати пухнастиків",
            "Час шукати друзів!"
        ).random()
    }
}
