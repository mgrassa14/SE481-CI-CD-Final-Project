package org.psnbtech;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * JUnit tests for the SidePanel class.
 *
 * SidePanel contains paintComponent() with no decision points.
 * The CFG is a single basic block, so EC is satisfied by any test that invokes the method.
 */
@RunWith(MockitoJUnitRunner.class)
// Tests that SidePanel can be constructed with a SnakeGame instance
public class SidePanelTest
{
    @Mock
    private SnakeGame mockGame; // only mock SnakeGame, NOT Graphics

    private SidePanel sidePanel;
    private BufferedImage image;
    private Graphics graphics; // real Graphics from BufferedImage

    @Before
    public void setUp()
    {
        // a game where no fruit has been eaten yet
        when(mockGame.getScore()).thenReturn(0);
        when(mockGame.getFruitsEaten()).thenReturn(0);
        // nextFruitScore starts at 100 (set in spawnFruit when a game begins)
        when(mockGame.getNextFruitScore()).thenReturn(100);

        sidePanel = new SidePanel(mockGame);
        sidePanel.setSize(300, BoardPanel.ROW_COUNT * BoardPanel.TILE_SIZE);

        // Real Graphics from BufferedImage
        // Matches SidePanel's preferred size: 300 wide, ROW_COUNT * TILE_SIZE tall (25 * 20 = 500)
        image = new BufferedImage(300, 500, BufferedImage.TYPE_INT_RGB);
        graphics = image.getGraphics();
    }

    @Test
    // Verifies SidePanel can be constructed with a SnakeGame
    public void constructorCreatesPanelWithGame()
    {
        assertNotNull(sidePanel);
    }

    @Test
    // Tests that paintComponent runs to completion without throwing on a valid Graphics object
    public void paintComponentRunsWithoutException()
    {
        try
        {
            sidePanel.paintComponent(graphics);
        }
        finally
        {
            graphics.dispose();
        }
    }
}