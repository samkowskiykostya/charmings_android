package com.charmings.app.domain

import com.charmings.app.data.model.Pet
import com.charmings.app.data.repository.WalkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.time.ZoneId

data class TrackerUpdate(
    val caught: Pet? = null,
    val minuteClosed: Boolean = false,
    val walkFinished: Boolean = false
)

/**
 * Process-wide owner of the walk state. All mutations are serialized by a mutex (the old code
 * launched a coroutine per sensor event and lost steps to read-modify-write races).
 *
 * The UI collects [state] directly, so it stays live while disk writes happen at most ~once a minute.
 */
class WalkTracker(
    private val walkRepository: WalkRepository,
    private val petCatcher: PetCatcher,
    private val engine: WalkEngine = WalkEngine(),
    private val clock: () -> Long = System::currentTimeMillis
) {
    private val mutex = Mutex()
    private val _state = MutableStateFlow(WalkState())
    val state: StateFlow<WalkState> = _state.asStateFlow()

    private var loaded = false
    private var lastSaveMs = 0L

    suspend fun ensureLoaded() = mutex.withLock { loadLocked() }

    /** Step-detector style input: [steps] happened at [timestampMs]. */
    suspend fun onSteps(timestampMs: Long, steps: Int): TrackerUpdate = mutex.withLock {
        loadLocked()
        val r = engine.addSteps(_state.value, timestampMs, steps, dayKey(timestampMs))
        afterUpdate(r)
    }

    /**
     * Step-counter input (cumulative since boot). The first reading after the service (re)starts
     * covers time we weren't listening, so it only goes to the daily total.
     */
    suspend fun onRawCounter(
        raw: Long, bootCount: Int, timestampMs: Long, firstAfterStart: Boolean, multiplier: Int
    ): TrackerUpdate = mutex.withLock {
        loadLocked()
        val s = _state.value
        val haveBaseline = s.lastRawCounter >= 0 && s.bootCount >= 0
        val rebooted = haveBaseline && (s.bootCount != bootCount || raw < s.lastRawCounter)
        val delta = when {
            !haveBaseline -> 0L               // first run ever: just take the baseline
            rebooted -> raw                   // counter restarted at boot
            else -> raw - s.lastRawCounter
        }.toInt() * multiplier
        val base = s.copy(lastRawCounter = raw, bootCount = bootCount)

        if (delta <= 0 || firstAfterStart || rebooted) {
            val st = engine.addUntimedSteps(base, delta, dayKey(timestampMs))
            _state.value = st
            save(st, null)
            return@withLock TrackerUpdate()
        }
        val r = engine.addSteps(base, timestampMs, delta, dayKey(timestampMs))
        afterUpdate(r)
    }

    suspend fun tick(): TrackerUpdate = mutex.withLock {
        loadLocked()
        val now = clock()
        afterUpdate(engine.tick(_state.value, now, dayKey(now)))
    }

    suspend fun flush() = mutex.withLock {
        if (loaded) save(_state.value, null)
    }

    private suspend fun afterUpdate(r: EngineResult): TrackerUpdate {
        val now = clock()
        var st = r.state
        var caught: Pet? = null

        if (r.minuteClosed && st.isWalking) {
            val canCheck = st.stepsSinceCatch >= PetCatcher.STEP_THRESHOLD_FOR_PET_CHECK &&
                now - st.lastPetCheck >= PetCatcher.PET_CHECK_INTERVAL_MS
            if (canCheck) {
                val outcome = petCatcher.check(st)
                st = st.copy(lastPetCheck = now, rolledPetIds = outcome.rolledPetIds)
                if (outcome.caught != null) {
                    st = engine.onCatch(st, now)
                    caught = outcome.caught
                }
            } else if (st.sessionMinutes == 1 || st.sessionMinutes % 5 == 0) {
                walkRepository.setEncouragement(Texts.WALKING.random())
            }
        }

        val changed = st != _state.value
        _state.value = st
        // Persist only real changes, and at most about once a minute while steps trickle in.
        if (r.finishedWalk != null || caught != null ||
            (changed && (r.minuteClosed || now - lastSaveMs > 60_000))
        ) {
            save(st, r.finishedWalk)
        }
        return TrackerUpdate(caught, r.minuteClosed && changed, r.finishedWalk != null)
    }

    private suspend fun loadLocked() {
        if (loaded) return
        _state.value = walkRepository.loadState()
        loaded = true
    }

    private suspend fun save(st: WalkState, finished: FinishedWalk?) {
        walkRepository.save(st, finished)
        lastSaveMs = clock()
    }

    private fun dayKey(ms: Long): String =
        Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate().toString()
}
