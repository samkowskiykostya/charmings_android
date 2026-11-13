package com.charmings.app.domain

import kotlinx.serialization.Serializable

/**
 * Everything the step pipeline needs to survive process death. Persisted as one JSON blob,
 * at most about once per minute (see WalkTracker), instead of several DataStore writes per step.
 */
@Serializable
data class WalkState(
    // Current, still-open minute bucket (epoch minute index) and the steps inside it.
    val bucketMinute: Long = -1,
    val bucketSteps: Int = 0,

    // Active walk session; sessionStart == null means "not walking".
    val sessionStart: Long? = null,          // epoch ms of the first walking minute
    val sessionSteps: Int = 0,
    val sessionMinutes: Int = 0,             // minutes that met the walking cadence
    val lastActiveMinute: Long = -1,         // epoch minute of the latest walking minute

    // Catch bookkeeping, scoped to the session.
    val stepsSinceCatch: Int = 0,
    val rolledPetIds: List<Int> = emptyList(), // probability is rolled once per pet per walk
    val lastPetCheck: Long = 0,

    // Daily total (all steps, walking or not).
    val dayKey: String = "",
    val daySteps: Int = 0,

    // Hardware step counter baseline, so a service restart doesn't lose or double count steps.
    val lastRawCounter: Long = -1,
    val bootCount: Int = -1
) {
    val isWalking: Boolean get() = sessionStart != null

    /** Steps shown in the UI: the session plus the minute currently being counted. */
    val liveSessionSteps: Int get() = if (isWalking) sessionSteps + bucketSteps else bucketSteps
}

data class FinishedWalk(val steps: Int, val startTime: Long, val durationMinutes: Int)

data class EngineResult(
    val state: WalkState,
    val finishedWalk: FinishedWalk? = null,
    /** True when a minute bucket was closed: a good moment to persist and to check for pets. */
    val minuteClosed: Boolean = false
)

/**
 * Cadence-based walk detection.
 *
 * - Steps are grouped by wall-clock minute. A minute with >= [walkingStepsPerMinute] steps is a
 *   *walking minute* (normal walking is 90–120 steps/min; pottering at home is far below 60).
 * - A walk starts at the first walking minute and continues while walking minutes are at most
 *   [sessionGapMinutes] apart (traffic lights, a quick shop stop). Slow minutes inside a walk still
 *   add their steps, but not their duration.
 * - The walk ends when no walking minute happened for longer than [sessionGapMinutes].
 *
 * Pure and deterministic: all time comes in as parameters, which keeps it unit-testable.
 */
class WalkEngine(
    private val walkingStepsPerMinute: Int = 60,
    private val sessionGapMinutes: Int = 5
) {

    fun addSteps(state: WalkState, timestampMs: Long, steps: Int, dayKey: String): EngineResult {
        if (steps <= 0) return EngineResult(state)
        val minute = timestampMs / MINUTE_MS
        var s = state
        var finished: FinishedWalk? = null
        var closed = false

        if (s.bucketMinute >= 0 && minute > s.bucketMinute) {
            val r = closeBucket(s)
            s = r.state; finished = r.finishedWalk; closed = true
        }
        // A new walking minute far after the last one ends the old session first.
        if (s.isWalking && minute - s.lastActiveMinute > sessionGapMinutes + 1) {
            finished = finished ?: finishOf(s)
            s = s.clearedSession()
        }
        if (s.bucketMinute < 0 || minute > s.bucketMinute) {
            s = s.copy(bucketMinute = minute, bucketSteps = 0)
        }
        // Late (batched) events for an older minute simply join the open bucket.
        s = s.copy(bucketSteps = s.bucketSteps + steps)
        s = addDaySteps(s, steps, dayKey)
        return EngineResult(s, finished, closed)
    }

    /** Steps we know happened but not when (e.g. while the service was dead): daily total only. */
    fun addUntimedSteps(state: WalkState, steps: Int, dayKey: String): WalkState =
        if (steps <= 0) state else addDaySteps(state, steps, dayKey)

    /**
     * Called periodically. Closes the open bucket once its minute is safely in the past (one extra
     * minute of grace for batched sensor delivery) and ends a walk that has gone quiet.
     */
    fun tick(state: WalkState, nowMs: Long, dayKey: String): EngineResult {
        val nowMinute = nowMs / MINUTE_MS
        var s = state
        var finished: FinishedWalk? = null
        var closed = false

        if (s.bucketMinute >= 0 && nowMinute > s.bucketMinute + 1) {
            val r = closeBucket(s)
            s = r.state.copy(bucketMinute = -1, bucketSteps = 0)
            finished = r.finishedWalk; closed = true
        }
        if (s.isWalking && nowMinute - s.lastActiveMinute > sessionGapMinutes + 1) {
            finished = finished ?: finishOf(s)
            s = s.clearedSession()
        }
        if (s.dayKey != dayKey) s = s.copy(dayKey = dayKey, daySteps = 0)
        return EngineResult(s, finished, closed)
    }

    fun onCatch(state: WalkState, nowMs: Long): WalkState =
        state.copy(stepsSinceCatch = 0, lastPetCheck = nowMs)

    private fun closeBucket(state: WalkState): EngineResult {
        val m = state.bucketMinute
        val n = state.bucketSteps
        var s = state.copy(bucketSteps = 0)
        var finished: FinishedWalk? = null

        if (n >= walkingStepsPerMinute) {
            if (s.isWalking && m - s.lastActiveMinute > sessionGapMinutes) {
                finished = finishOf(s)
                s = s.clearedSession()
            }
            if (!s.isWalking) {
                s = s.copy(
                    sessionStart = m * MINUTE_MS, sessionSteps = 0, sessionMinutes = 0,
                    stepsSinceCatch = 0, rolledPetIds = emptyList(), lastPetCheck = 0
                )
            }
            s = s.copy(
                sessionSteps = s.sessionSteps + n,
                sessionMinutes = s.sessionMinutes + 1,
                stepsSinceCatch = s.stepsSinceCatch + n,
                lastActiveMinute = maxOf(s.lastActiveMinute, m)
            )
        } else if (s.isWalking && m - s.lastActiveMinute <= sessionGapMinutes) {
            s = s.copy(sessionSteps = s.sessionSteps + n, stepsSinceCatch = s.stepsSinceCatch + n)
        }
        return EngineResult(s, finished, true)
    }

    private fun finishOf(s: WalkState): FinishedWalk? {
        val start = s.sessionStart ?: return null
        val end = (s.lastActiveMinute + 1) * MINUTE_MS
        val minutes = ((end - start) / MINUTE_MS).toInt().coerceAtLeast(s.sessionMinutes)
        return FinishedWalk(s.sessionSteps, start, minutes)
    }

    private fun WalkState.clearedSession() = copy(
        sessionStart = null, sessionSteps = 0, sessionMinutes = 0, lastActiveMinute = -1,
        stepsSinceCatch = 0, rolledPetIds = emptyList(), lastPetCheck = 0
    )

    private fun addDaySteps(s: WalkState, steps: Int, dayKey: String) =
        if (s.dayKey == dayKey) s.copy(daySteps = s.daySteps + steps)
        else s.copy(dayKey = dayKey, daySteps = steps)

    companion object {
        const val MINUTE_MS = 60_000L
    }
}
