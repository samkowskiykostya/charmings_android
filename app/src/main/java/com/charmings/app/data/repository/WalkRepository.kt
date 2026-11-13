package com.charmings.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.charmings.app.domain.FinishedWalk
import com.charmings.app.domain.Texts
import com.charmings.app.domain.WalkState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.walkDataStore: DataStore<Preferences> by preferencesDataStore(name = "walk_prefs")

@Serializable
data class WalkingHistoryEntry(
    val steps: Int,
    val startTime: Long,
    val durationMinutes: Int = 0
)

class WalkRepository(private val context: Context) {

    companion object {
        private val WALK_STATE_KEY = stringPreferencesKey("walk_state")
        private val HISTORY_KEY = stringPreferencesKey("walking_history")
        private val ENCOURAGEMENT_KEY = stringPreferencesKey("encouragement")
        private val ENCOURAGEMENT_TIME_KEY = longPreferencesKey("encouragement_time")
        private const val MAX_HISTORY = 3
        private const val MIN_STEPS_FOR_HISTORY = 200
        private const val ENCOURAGEMENT_MIN_INTERVAL_MS = 60_000L
    }

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = false }

    val historyFlow: Flow<List<WalkingHistoryEntry>> = context.walkDataStore.data
        .map { decodeHistory(it[HISTORY_KEY]) }
        .distinctUntilChanged()

    val encouragementFlow: Flow<String?> = context.walkDataStore.data
        .map { it[ENCOURAGEMENT_KEY] }
        .distinctUntilChanged()

    suspend fun loadState(): WalkState {
        val raw = context.walkDataStore.data.first()[WALK_STATE_KEY] ?: return WalkState()
        return runCatching { json.decodeFromString<WalkState>(raw) }.getOrDefault(WalkState())
    }

    /** One write for the whole step pipeline state (plus an optional finished walk). */
    suspend fun save(state: WalkState, finishedWalk: FinishedWalk? = null) {
        context.walkDataStore.edit { prefs ->
            prefs[WALK_STATE_KEY] = json.encodeToString(state)
            if (finishedWalk != null && finishedWalk.steps >= MIN_STEPS_FOR_HISTORY) {
                val history = listOf(
                    WalkingHistoryEntry(finishedWalk.steps, finishedWalk.startTime, finishedWalk.durationMinutes)
                ) + decodeHistory(prefs[HISTORY_KEY])
                prefs[HISTORY_KEY] = json.encodeToString(history.take(MAX_HISTORY))
                prefs[ENCOURAGEMENT_KEY] = Texts.WALK_ENDED.random()
                prefs[ENCOURAGEMENT_TIME_KEY] = System.currentTimeMillis()
            }
        }
    }

    /** Rate-limited so the bubble doesn't flicker; [force] for important news (a catch). */
    suspend fun setEncouragement(message: String, force: Boolean = false) {
        context.walkDataStore.edit { prefs ->
            val now = System.currentTimeMillis()
            val last = prefs[ENCOURAGEMENT_TIME_KEY] ?: 0L
            if (force || now - last >= ENCOURAGEMENT_MIN_INTERVAL_MS) {
                prefs[ENCOURAGEMENT_KEY] = message
                prefs[ENCOURAGEMENT_TIME_KEY] = now
            }
        }
    }

    private fun decodeHistory(raw: String?): List<WalkingHistoryEntry> =
        runCatching { json.decodeFromString<List<WalkingHistoryEntry>>(raw ?: "[]") }.getOrDefault(emptyList())
}
