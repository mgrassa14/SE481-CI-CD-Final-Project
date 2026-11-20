package org.psnbtech;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit tests for the Clock class.
 * Primary coverage criterion: Edge Coverage
 */
public class ClockTest
{
    private Clock clock;

    @Before
    public void setUp()
    {
        // 20 cycles/sec = 50ms per cycle.
        clock = new Clock(20.0f);
    }

    @Test
    // Verifies that the constructor that calls reset() leaves the clock in an unpaused state
    public void constructorCreatesUnpausedClock()
    {
        assertFalse(clock.isPaused());
    }

    @Test
    // Verifies that the cycle counter is initialized to zero
    public void constructorStartsWithNoElapsedCycles()
    {
        assertFalse(clock.peekElapsedCycle());
    }

    @Test
    public void setCyclesPerSecondChangesCycleRate() throws InterruptedException
    {
        // Slow clock (1 cycle/sec = 1000ms per cycle): no cycle in 60ms
        Clock slow = new Clock(1.0f);
        Thread.sleep(60);
        slow.update();
        assertFalse(slow.peekElapsedCycle());

        // Speed it up (100 cycles/sec = 10ms per cycle): plenty of cycles in 60ms
        slow.setCyclesPerSecond(100.0f);
        slow.reset();
        Thread.sleep(60);
        slow.update();
        assertTrue(slow.peekElapsedCycle());
    }

    @Test
    // Accumulates cycles, then resets, then confirms the counter is empty
    public void resetClearsElapsedCycles() throws InterruptedException
    {
        Thread.sleep(120);
        clock.update();
        assertTrue(clock.peekElapsedCycle());

        clock.reset();

        assertFalse(clock.peekElapsedCycle());
    }

    @Test
    // reset() always sets isPaused = false, even if the clock was paused at the time of the call
    public void resetUnpausesClock()
    {
        clock.setPaused(true);
        clock.reset();
        assertFalse(clock.isPaused());
    }

    //  setPaused / isPaused tests verify getters and setters
    @Test
    public void setPausedTrueMarksClockAsPaused()
    {
        clock.setPaused(true);
        assertTrue(clock.isPaused());
    }

    @Test
    public void setPausedFalseMarksClockAsRunning()
    {
        clock.setPaused(true);
        clock.setPaused(false);
        assertFalse(clock.isPaused());
    }


    /** update() tests
     * CFG has 3 nodes, 3 edges:
     * TR_EC = {[1,2], [1,3], [2,3]}
     * Edges: [1,2] (T), [1,3] (F), [2,3] (merge to exit)
     */

    @Test
    // Covers update() edge [1,2] (T, not paused enters if) and edge [2,3] (merge from if to exit).
    // After 200ms on a 50ms per cycle clock, there should be at least one cycle
    public void updateAccumulatesCyclesWhenNotPaused() throws InterruptedException
    {
        Thread.sleep(200);
        clock.update();
        assertTrue(clock.peekElapsedCycle());
    }

    @Test
    // Covers update() edge [1,3] (F, paused skips if and goes straight to the lastUpdate assignment).
    // Should be no cycles while paused.
    public void updateDoesNotAccumulateCyclesWhenPaused() throws InterruptedException
    {
        clock.setPaused(true);
        Thread.sleep(200);
        clock.update();
        assertFalse(clock.peekElapsedCycle());
    }

    @Test
    // Verifies pause,unpause state transition
    public void updateResumesAccumulatingAfterUnpause() throws InterruptedException
    {
        clock.setPaused(true);
        Thread.sleep(150);
        clock.update();
        assertFalse(clock.peekElapsedCycle());

        clock.setPaused(false);
        clock.reset();
        Thread.sleep(150);
        clock.update();
        assertTrue(clock.peekElapsedCycle());
    }

    /**
     * hasElapsedCycle() tests
     * CFG has 4 nodes, 4 edges:
     * TR_EC = {[1,2], [1,3], [2,4], [3,4]}
     * Edges: [1,2] (T), [1,3] (F), [2,4] (true exit), [3,4] (false exit)
     */


    @Test
    /*
     * Covers hasElapsedCycle() edge [1,3] (F, no cycles available) and edge [3,4] (exit from false branch)
     * On a fresh clock, elapsedCycles is 0, so the method returns false
     */
    public void hasElapsedCycleReturnsFalseWhenNoCyclesElapsed()
    {
        assertFalse(clock.hasElapsedCycle());
    }

    @Test
    /*
     * Covers hasElapsedCycle() edge [1,2] (T, cycles are available, decrement and return true)
     * and edge [2,4] (exit from true branch)
     */
    public void hasElapsedCycleReturnsTrueWhenCyclesAvailable() throws InterruptedException
    {
        Thread.sleep(120);
        clock.update();
        assertTrue(clock.hasElapsedCycle());
    }

    @Test
    /* Confirms the elapsedCycles statement by draining cycles until none
     * If hasElapsedCycle() doesn't decrement, this loop will not terminate & safety counter triggers fail()
     */
    public void hasElapsedCycleDecrementsCounter() throws InterruptedException
    {
        Thread.sleep(250);
        clock.update();

        int consumed = 0;
        while (clock.hasElapsedCycle())
        {
            consumed++;
            if (consumed > 100)
            {
                fail("hasElapsedCycle did not terminate");
            }
        }

        assertTrue(consumed >= 1);
        assertFalse(clock.peekElapsedCycle());
    }

    @Test
    // Verifies calling peek multiple times does not consume cycle
    public void peekElapsedCycleDoesNotDecrement() throws InterruptedException
    {
        Thread.sleep(120);
        clock.update();

        assertTrue(clock.peekElapsedCycle());
        assertTrue(clock.peekElapsedCycle());
        assertTrue(clock.hasElapsedCycle());
    }

    @Test
    // Verifies peek returns false when no cycles
    public void peekElapsedCycleReturnsFalseOnFreshClock()
    {
        assertFalse(clock.peekElapsedCycle());
    }
}