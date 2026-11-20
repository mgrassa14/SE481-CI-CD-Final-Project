package org.psnbtech;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.Mockito.*;
import java.awt.Point;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * Tests for BoardPanel.paintComponent(Graphics g)
 *
 * Coverage Criterion: EC (Edge Coverage)
 * Framework: BufferedImage (real Graphics from an in-memory image)

 * EC TEST REQUIREMENTS COVERED:
 *   TC1 (N14→N22):         active game, no overlay → no white pixels
 *   TC2 (N14→N15→N16→N17→N22): isNewGame=true → white overlay appears
 *   TC3 (N14→N15→N16→N18→N19→N22): isGameOver=true → white overlay appears
 *   TC4 (N14→N15→N16→N18→N20→N21→N22): isPaused=true → white overlay appears
 *   TC5 (N4→N5):           non-null tile → drawTile called (red pixel appears)
 *   TC6 (N4→N6):           null tile → drawTile NOT called (no red pixel)
 */
@RunWith(MockitoJUnitRunner.class)
public class BoardPanelPaintComponentTest {

    // Mock the game so we can control isNewGame/isGameOver/isPaused
    @Mock
    private SnakeGame mockGame;

    private BoardPanel boardPanel;

    // BufferedImage gives us a real Graphics context with no display needed
    private BufferedImage image;
    private Graphics graphics;

    @Before
    public void setUp() {
        // Default state: active game, no overlay
        when(mockGame.isNewGame()).thenReturn(false);
        when(mockGame.isGameOver()).thenReturn(false);
        when(mockGame.isPaused()).thenReturn(false);

        // Create board
        boardPanel = new BoardPanel(mockGame);
        boardPanel.setSize(
                BoardPanel.COL_COUNT * BoardPanel.TILE_SIZE,
                BoardPanel.ROW_COUNT * BoardPanel.TILE_SIZE
        );
        boardPanel.clearBoard();

        // Create in-memory image :paintComponent draws into this
        image = new BufferedImage(
                BoardPanel.COL_COUNT * BoardPanel.TILE_SIZE,
                BoardPanel.ROW_COUNT * BoardPanel.TILE_SIZE,
                BufferedImage.TYPE_INT_RGB
        );
        graphics = image.getGraphics();
    }

    private boolean imageContainsColor(Color color) {
        int targetRGB = color.getRGB();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (image.getRGB(x, y) == targetRGB) {
                    return true;
                }
            }
        }
        return false;
    }

    // TC1 — Active game, no overlay

    @Test
    public void tc1_activeGame_noOverlayDrawn() {
        // All flags false by default in setUp
        boardPanel.paintComponent(graphics);
        assertFalse(
                "Active game should NOT draw white overlay text",
                imageContainsColor(Color.WHITE)
        );
        assertTrue(
                "Grid lines should be drawn in DARK_GRAY",
                imageContainsColor(Color.DARK_GRAY)
        );
    }

    // TC2 — New game state

    @Test
    public void tc2_newGame_whiteOverlayAppears() {
        when(mockGame.isNewGame()).thenReturn(true);

        boardPanel.paintComponent(graphics);
        assertTrue(
                "New game overlay should draw white text on screen",
                imageContainsColor(Color.WHITE)
        );
    }


    // TC3 — Game over state

    @Test
    public void tc3_gameOver_whiteOverlayAppears() {
        when(mockGame.isGameOver()).thenReturn(true);
        boardPanel.paintComponent(graphics);
        assertTrue(
                "Game over overlay should draw white text on screen",
                imageContainsColor(Color.WHITE)
        );
    }


    // TC4 — Paused state

    @Test
    public void tc4_paused_whiteOverlayAppears() {
        when(mockGame.isPaused()).thenReturn(true);

        boardPanel.paintComponent(graphics);
        assertTrue(
                "Paused overlay should draw white text on screen",
                imageContainsColor(Color.WHITE)
        );
    }

    // TC5 — Non-null tile on board (drawTile called)

    @Test
    public void tc5_fruitTileOnBoard_redPixelAppears() {
        // Place a Fruit tile — drawTile draws it as a red oval
        boardPanel.setTile(0, 0, TileType.Fruit);

        boardPanel.paintComponent(graphics);
        assertTrue(
                "Fruit tile should be drawn as red circle",
                imageContainsColor(Color.RED)
        );
    }

    // TC6 — Null tile on board (drawTile NOT called)

    @Test
    public void tc6_nullTiles_noTileDrawn() {
        // Board is all null from setUp — drawTile never called
        boardPanel.paintComponent(graphics);
        assertFalse(
                "No Fruit should be drawn when all tiles are null",
                imageContainsColor(Color.RED)
        );
        assertFalse(
                "No snake should be drawn when all tiles are null",
                imageContainsColor(Color.GREEN)
        );
    }

   @Test
    public void testSetAndGetTile() {
        boardPanel.setTile(5, 5, TileType.Fruit);
        assertEquals(TileType.Fruit, boardPanel.getTile(5, 5));

        boardPanel.setTile(10, 10, TileType.SnakeBody);
        assertEquals(TileType.SnakeBody, boardPanel.getTile(10, 10));

        boardPanel.setTile(15, 15, TileType.SnakeHead);
        assertEquals(TileType.SnakeHead, boardPanel.getTile(15, 15));
    }

    @Test
    public void testSetTileWithPoint() {
        // Arrange
        Point point = new Point(8, 8);

        // Act
        boardPanel.setTile(point, TileType.Fruit);

        // Assert
        assertEquals(TileType.Fruit, boardPanel.getTile(8, 8));
    }

    @Test
    public void testClearBoard() {
        boardPanel.setTile(5, 5, TileType.Fruit);
        boardPanel.setTile(10, 10, TileType.SnakeBody);

        boardPanel.clearBoard();

        assertNull(boardPanel.getTile(5, 5));
        assertNull(boardPanel.getTile(10, 10));
        assertNull(boardPanel.getTile(0, 0));
    }

    @Test
    public void testTileBoundaries() {
        boardPanel.setTile(0, 0, TileType.Fruit);
        assertEquals(TileType.Fruit, boardPanel.getTile(0, 0));

        boardPanel.setTile(24, 24, TileType.SnakeBody);
        assertEquals(TileType.SnakeBody, boardPanel.getTile(24, 24));
    }
}