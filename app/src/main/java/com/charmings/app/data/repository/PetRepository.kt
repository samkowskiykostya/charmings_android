package com.charmings.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.charmings.app.data.PetsData
import com.charmings.app.data.model.Pet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "charmings_prefs")

class PetRepository(private val context: Context) {

    companion object {
        private val CAUGHT_PETS_KEY = stringPreferencesKey("caught_pets")
        private val NEW_CATCHES_KEY = stringPreferencesKey("new_catches")
    }

    private val json = Json { ignoreUnknownKeys = true }

    val allPets: List<Pet> get() = PetsData.pets

    fun getPetById(id: Int): Pet? = PetsData.getPetById(id)

    val caughtPetIdsFlow: Flow<List<Int>> =
        context.dataStore.data.map { decode(it[CAUGHT_PETS_KEY]) }.distinctUntilChanged()

    val newCatchesFlow: Flow<List<Int>> =
        context.dataStore.data.map { decode(it[NEW_CATCHES_KEY]) }.distinctUntilChanged()

    suspend fun getCaughtPetIds(): List<Int> = caughtPetIdsFlow.first()

    /** Marks a pet as caught and "new" in one atomic write. */
    suspend fun addCaughtPet(petId: Int) {
        context.dataStore.edit { prefs ->
            prefs.addTo(CAUGHT_PETS_KEY, petId)
            prefs.addTo(NEW_CATCHES_KEY, petId)
        }
    }

    suspend fun removeFromNewCatches(petId: Int) {
        context.dataStore.edit { prefs ->
            val ids = decode(prefs[NEW_CATCHES_KEY])
            if (petId in ids) prefs[NEW_CATCHES_KEY] = json.encodeToString(ids - petId)
        }
    }

    suspend fun initializeIfNeeded() {
        if (0 !in getCaughtPetIds()) addCaughtPet(0)
    }

    private fun MutablePreferences.addTo(key: Preferences.Key<String>, id: Int) {
        val ids = decode(this[key])
        if (id !in ids) this[key] = json.encodeToString(ids + id)
    }

    private fun decode(raw: String?): List<Int> =
        runCatching { json.decodeFromString<List<Int>>(raw ?: "[]") }.getOrDefault(emptyList())
}
