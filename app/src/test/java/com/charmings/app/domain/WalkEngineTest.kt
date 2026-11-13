package com.charmings.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WalkEngineTest {

    private val engine = WalkEngine(walkingStepsPerMinute = 60, sessionGapMinutes = 5)
    private val day = "2026-11-13"
    private val t0 = 1_800_000_000_000L - (1_800_000_000_000L % 60_000) // minute-aligned

    private fun min(m: Int, sec: Int = 0) = t0 + m * 60_000L + sec * 1000L

    /** Feeds [perMinute] steps in each of the given minutes, spread in 10 batches. */
    private fun walk(state: WalkState, minutes: IntRange, perMinute: Int): Pair<WalkState, List<FinishedWalk>> {
        var s = state
        val finished = mutableListOf<FinishedWalk>()
        for (m in minutes) repeat(10) { i ->
            val r = engine.addSteps(s, min(m, i * 6), perMinute / 10, day)
            s = r.state
            r.finishedWalk?.let(finished::add)
        }
        return s to finished
    }

    @Test
    fun `brisk walking starts a session after the first full minute`() {
        val (s, _) = walk(WalkState(), 0..0, 100)
        assertFalse("minute still open", s.isWalking)
        val closed = engine.tick(s, min(2), day).state
        assertTrue(closed.isWalking)
        assertEquals(100, closed.sessionSteps)
        assertEquals(1, closed.sessionMinutes)
    }

    @Test
    fun `pottering at home never becomes a walk`() {
        // 30 steps a minute for two hours: the old logic counted all 3600 toward pets.
        val (s, finished) = walk(WalkState(), 0..119, 30)
        val end = engine.tick(s, min(125), day).state
        assertFalse(end.isWalking)
        assertEquals(0, end.stepsSinceCatch)
        assertTrue(finished.isEmpty())
        assertEquals(3600, end.daySteps) // still counted for the day
    }

    @Test
    fun `a short stop at a traffic light keeps the walk going`() {
        var (s, _) = walk(WalkState(), 0..9, 100)
        s = walk(s, 10..11, 10).first          // 2 slow minutes
        val (after, finished) = walk(s, 12..19, 100)
        val closed = engine.tick(after, min(21), day).state
        assertTrue(finished.isEmpty())
        assertEquals(18, closed.sessionMinutes)
        assertEquals(18 * 100 + 2 * 10, closed.sessionSteps)
    }

    @Test
    fun `walk ends after a long pause and is reported with its duration`() {
        val (s, _) = walk(WalkState(), 0..19, 100)
        val r = engine.tick(s, min(40), day)
        assertFalse(r.state.isWalking)
        val f = assertNotNullAndGet(r.finishedWalk)
        assertEquals(2000, f.steps)
        assertEquals(20, f.durationMinutes)
        assertEquals(min(0), f.startTime)
    }

    @Test
    fun `new walking after a long gap starts a fresh session`() {
        val (s, _) = walk(WalkState(), 0..4, 100)
        val (s2, finished) = walk(s, 30..34, 100)
        val closed = engine.tick(s2, min(36), day).state
        assertEquals(1, finished.size)
        assertEquals(500, finished[0].steps)
        assertEquals(5, closed.sessionMinutes)
        assertEquals(min(30), closed.sessionStart)
    }

    @Test
    fun `catch resets the catch counter but not the walk`() {
        val (s, _) = walk(WalkState(), 0..9, 100)
        val closed = engine.tick(s, min(11), day).state
        val afterCatch = engine.onCatch(closed, min(11))
        assertEquals(0, afterCatch.stepsSinceCatch)
        assertEquals(1000, afterCatch.sessionSteps)
        assertTrue(afterCatch.isWalking)
    }

    @Test
    fun `late batched events join the open minute instead of being lost`() {
        var s = engine.addSteps(WalkState(), min(5, 10), 50, day).state
        s = engine.addSteps(s, min(4, 50), 30, day).state // older timestamp arriving late
        assertEquals(80, s.bucketSteps)
        assertEquals(80, s.daySteps)
    }

    @Test
    fun `daily total resets on a new day`() {
        var s = engine.addSteps(WalkState(), min(0), 100, "2026-11-13").state
        s = engine.addSteps(s, min(1), 40, "2026-11-14").state
        assertEquals(40, s.daySteps)
    }

    @Test
    fun `untimed steps only count for the day`() {
        val s = engine.addUntimedSteps(WalkState(), 500, day)
        assertEquals(500, s.daySteps)
        assertFalse(s.isWalking)
        assertEquals(0, s.liveSessionSteps)
    }

    @Test
    fun `tick waits one extra minute for batched sensor delivery`() {
        val s = engine.addSteps(WalkState(), min(0, 30), 100, day).state
        assertNull(engine.tick(s, min(1, 30), day).state.sessionStart)
        assertNotNull(engine.tick(s, min(2), day).state.sessionStart)
    }

    private fun <T> assertNotNullAndGet(v: T?): T {
        assertNotNull(v); return v!!
    }
}
