package com.charmings.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.charmings.app.data.PetsData
import com.charmings.app.data.model.Pet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "charmings_prefs")

class PetRepository(private val context: Context) {
    
    companion object {
        private val CAUGHT_PETS_KEY = stringPreferencesKey("caught_pets")
        private val NEW_CATCHES_KEY = stringPreferencesKey("new_catches")
    }
    
    private val json = Json { ignoreUnknownKeys = true }
    
    // Get all pets
    fun getAllPets(): List<Pet> = PetsData.pets
    
    // Get pet by ID
    fun getPetById(id: Int): Pet? = PetsData.getPetById(id)
    
    // Get caught pet IDs as Flow
    val caughtPetIdsFlow: Flow<List<Int>> = context.dataStore.data.map { preferences ->
        val jsonString = preferences[CAUGHT_PETS_KEY] ?: "[]"
        try {
            json.decodeFromString<List<Int>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Get new catches as Flow
    val newCatchesFlow: Flow<List<Int>> = context.dataStore.data.map { preferences ->
        val jsonString = preferences[NEW_CATCHES_KEY] ?: "[]"
        try {
            json.decodeFromString<List<Int>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Get caught pet IDs (suspend)
    suspend fun getCaughtPetIds(): List<Int> {
        return caughtPetIdsFlow.first()
    }
    
    // Get new catches (suspend)
    suspend fun getNewCatches(): List<Int> {
        return newCatchesFlow.first()
    }
    
    // Save caught pet IDs
    suspend fun saveCaughtPetIds(ids: List<Int>) {
        context.dataStore.edit { preferences ->
            preferences[CAUGHT_PETS_KEY] = json.encodeToString(ids)
        }
    }
    
    // Add a caught pet
    suspend fun addCaughtPet(petId: Int) {
        val currentIds = getCaughtPetIds().toMutableList()
        if (!currentIds.contains(petId)) {
            currentIds.add(petId)
            saveCaughtPetIds(currentIds)
        }
    }
    
    // Save new catches
    suspend fun saveNewCatches(ids: List<Int>) {
        context.dataStore.edit { preferences ->
            preferences[NEW_CATCHES_KEY] = json.encodeToString(ids)
        }
    }
    
    // Add a new catch
    suspend fun addNewCatch(petId: Int) {
        val currentIds = getNewCatches().toMutableList()
        if (!currentIds.contains(petId)) {
            currentIds.add(petId)
            saveNewCatches(currentIds)
        }
    }
    
    // Remove from new catches (when user views the pet)
    suspend fun removeFromNewCatches(petId: Int) {
        val currentIds = getNewCatches().toMutableList()
        currentIds.remove(petId)
        saveNewCatches(currentIds)
    }
    
    // Get caught pets
    suspend fun getCaughtPets(): List<Pet> {
        val caughtIds = getCaughtPetIds()
        return getAllPets().filter { caughtIds.contains(it.id) }
    }
    
    // Get uncaught pets
    suspend fun getUncaughtPets(): List<Pet> {
        val caughtIds = getCaughtPetIds()
        return getAllPets().filter { !caughtIds.contains(it.id) }
    }
    
    // Initialize with first pet if needed
    suspend fun initializeIfNeeded() {
        val caughtIds = getCaughtPetIds()
        if (!caughtIds.contains(0)) {
            addCaughtPet(0)
            addNewCatch(0)
        }
    }
}
