package org.psnbtech;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.Mockito.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;

@RunWith(MockitoJUnitRunner.class)
public class BoardPanelDrawTileTest {

    @Mock
    private SnakeGame mockGame;  // only mock SnakeGame, NOT Graphics

    private BoardPanel boardPanel;
    private BufferedImage image;
    private Graphics graphics;  // real Graphics from BufferedImage

    @Before
    public void setUp() {
        boardPanel = new BoardPanel(mockGame);
        boardPanel.clearBoard();

        // Real Graphics
        image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        graphics = image.getGraphics();
    }

    private void invokeDrawTile(int x, int y, TileType type) throws Exception {
        Method method = BoardPanel.class.getDeclaredMethod(
                "drawTile", int.class, int.class, TileType.class, Graphics.class);
        method.setAccessible(true);
        method.invoke(boardPanel, x, y, type, graphics);
    }

    private boolean imageContainsColor(Color color) {
        int target = color.getRGB();
        for (int x = 0; x < image.getWidth(); x++)
            for (int y = 0; y < image.getHeight(); y++)
                if (image.getRGB(x, y) == target) return true;
        return false;
    }

    // TP1: [N1, N2, N9] — Fruit tile draws red oval
    @Test
    public void testDrawTile_Fruit() throws Exception {
        invokeDrawTile(0, 0, TileType.Fruit);
        assertTrue("Fruit should be drawn in RED",
                imageContainsColor(Color.RED));
        assertFalse("Fruit should NOT draw green",
                imageContainsColor(Color.GREEN));
    }

    // TP2: [N1, N3, N9] — SnakeBody draws green rectangle
    @Test
    public void testDrawTile_SnakeBody() throws Exception {
        invokeDrawTile(0, 0, TileType.SnakeBody);
        assertTrue("SnakeBody should be drawn in GREEN",
                imageContainsColor(Color.GREEN));
        assertFalse("SnakeBody should NOT draw red",
                imageContainsColor(Color.RED));
    }

    // TP3: [N1, N4, N5, N9] — SnakeHead North draws green + black eyes
    @Test
    public void testDrawTile_SnakeHead_North() throws Exception {
        when(mockGame.getDirection()).thenReturn(Direction.North);
        invokeDrawTile(0, 0, TileType.SnakeHead);
        assertTrue("SnakeHead should be drawn in GREEN",
                imageContainsColor(Color.GREEN));
        assertTrue("SnakeHead eyes should be drawn in BLACK",
                imageContainsColor(Color.BLACK));
    }

    // TP4: [N1, N4, N6, N9] — SnakeHead South
    @Test
    public void testDrawTile_SnakeHead_South() throws Exception {
        when(mockGame.getDirection()).thenReturn(Direction.South);
        invokeDrawTile(0, 0, TileType.SnakeHead);
        assertTrue("SnakeHead South should draw GREEN",
                imageContainsColor(Color.GREEN));
        assertTrue("SnakeHead South eyes should be BLACK",
                imageContainsColor(Color.BLACK));
    }

    // TP5: [N1, N4, N7, N9] — SnakeHead West
    @Test
    public void testDrawTile_SnakeHead_West() throws Exception {
        when(mockGame.getDirection()).thenReturn(Direction.West);
        invokeDrawTile(0, 0, TileType.SnakeHead);
        assertTrue("SnakeHead West should draw GREEN",
                imageContainsColor(Color.GREEN));
        assertTrue("SnakeHead West eyes should be BLACK",
                imageContainsColor(Color.BLACK));
    }

    // TP6: [N1, N4, N8, N9] — SnakeHead East
    @Test
    public void testDrawTile_SnakeHead_East() throws Exception {
        when(mockGame.getDirection()).thenReturn(Direction.East);
        invokeDrawTile(0, 0, TileType.SnakeHead);
        assertTrue("SnakeHead East should draw GREEN",
                imageContainsColor(Color.GREEN));
        assertTrue("SnakeHead East eyes should be BLACK",
                imageContainsColor(Color.BLACK));
    }
}