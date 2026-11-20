package org.psnbtech;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit tests for the Direction enum.
 *
 * Direction has no methods of its own and no branches, so there is no
 * coverage criterion to derive tests from. These tests verify that all
 * four expected directional values exist and can be
 * referenced.
 */
public class DirectionTest
{
    @Test
    public void directionHasFourValues()
    {
        // N, E, S, W
        assertEquals(4, Direction.values().length);
    }

    @Test
    public void directionContainsAllExpectedValues()
    {
        // Verifies each constant is defined & accessible
        assertNotNull(Direction.North);
        assertNotNull(Direction.East);
        assertNotNull(Direction.South);
        assertNotNull(Direction.West);
    }

    @Test
    public void directionValueOfReturnsCorrectConstants()
    {
        // Verifies valueOf() resolves each name back to its enum constant
        assertEquals(Direction.North, Direction.valueOf("North"));
        assertEquals(Direction.East,  Direction.valueOf("East"));
        assertEquals(Direction.South, Direction.valueOf("South"));
        assertEquals(Direction.West,  Direction.valueOf("West"));
    }
}
