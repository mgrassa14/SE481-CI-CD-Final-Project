package org.psnbtech;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit tests for the TileType enum.
 *
 * TileType has no methods of its own and no branches, so there is no
 * coverage criterion to derive tests from. These tests verify
 * that all three expected values exist and can be referenced.
 */
public class TileTypeTest
{
    @Test
    public void tileTypeHasThreeValues()
    {
        // The Snake game uses exactly three tile types
        assertEquals(3, TileType.values().length);
    }

    @Test
    public void tileTypeContainsAllExpectedValues()
    {
        // Verifies each expected constant is defined and accessible
        assertNotNull(TileType.Fruit);
        assertNotNull(TileType.SnakeHead);
        assertNotNull(TileType.SnakeBody);
    }

    @Test
    public void tileTypeValueOfReturnsCorrectConstants()
    {
        // Verifies valueOf() resolves each name back to its enum constant
        assertEquals(TileType.Fruit,     TileType.valueOf("Fruit"));
        assertEquals(TileType.SnakeHead, TileType.valueOf("SnakeHead"));
        assertEquals(TileType.SnakeBody, TileType.valueOf("SnakeBody"));
    }
}
