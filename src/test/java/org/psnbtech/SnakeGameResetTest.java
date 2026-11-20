package org.psnbtech;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import static org.junit.Assert.*;

import java.awt.Point;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;

/**
 * Prime Path Coverage tests for SnakeGame.resetGame().
 *
 *   Only 2 nodes. Prime path:  [1, 2]
 *
 *   Test Case:
 *   Call resetGame() and assert all post-conditions hold:
 *   score == 0
 *   fruitsEaten == 0
 *   snake.size() == 1
 *   snake head is at board center
 *   directions.size() == 1 && directions[0] == North
 *   board tile at head == SnakeHead
 *   isNewGame == false
 *   isGameOver == false
 *
 * Setup notes:
 *   - SnakeGame extends JFrame; the test environment is headless.
 *     Mockito.mock(CALLS_REAL_METHODS) creates the instance without
 *     invoking the JFrame constructor to avoid HeadlessException.
 *   - resetGame() is private & is invoked via reflection.
 *   - All fields that resetGame() reads or writes are injected via
 *     reflection before the call.
 *   - A real BoardPanel is not available in headless mode, so board is
 *     mocked; clearBoard() and setTile() calls are verified on the mock.
 */
@RunWith(MockitoJUnitRunner.class)
public class SnakeGameResetTest
{
    private SnakeGame game;
    private LinkedList<Point>     snake;
    private LinkedList<Direction> directions;

    // Mock board checks setTile()
    private BoardPanel boardMock;

    @Before
    public void setUp() throws Exception
    {
        // Creates SnakeGame without running its JFrame ctor
        game = Mockito.mock(SnakeGame.class, Mockito.CALLS_REAL_METHODS);

        // Builds a real Clock so logicTimer.reset() won't throw a NullPointerException
        Clock clock = new Clock(9.0f);

        // Empty lists that resetGame() will clear and fill w/ new starting values
        snake      = new LinkedList<>();
        directions = new LinkedList<>();

        // Mock board so clearBoard/setTile can be called without a real display
        boardMock = Mockito.mock(BoardPanel.class);

        // Sets all the private variables that resetGame() will read/modify
        setField("score",        100);
        setField("fruitsEaten",  5);
        setField("isNewGame",    true);
        setField("isGameOver",   true);
        setField("snake",        snake);
        setField("directions",   directions);
        setField("board",        boardMock);
        setField("logicTimer",   clock);

        // spawnFruit() needs a non-null Random
        setField("random", new java.util.Random());

        // Calls the private resetGame() method under test.
        invokeResetGame();
    }


    @Test
    // Resets score to 0.
    public void resetGame_scoreIsZero() throws Exception
    {
        assertEquals(0, getIntField("score"));
    }

    @Test
    // Resets fruitsEaten to 0.
    public void resetGame_fruitsEatenIsZero() throws Exception
    {
        assertEquals(0, getIntField("fruitsEaten"));
    }

    @Test
    // After resetGame(), isNewGame should be false
    public void resetGame_isNewGameIsFalse() throws Exception
    {
        assertFalse(getBooleanField("isNewGame"));
    }

    @Test
    // After resetGame(), GameOver should be turned off/ false
    public void resetGame_isGameOverIsFalse() throws Exception
    {
        assertFalse(getBooleanField("isGameOver"));
    }

    @Test
    public void resetGame_snakeLengthIsOne() throws Exception
    {
        assertEquals(1, snake.size());
    }

    @Test
    public void resetGame_snakeHeadAtBoardCenter() throws Exception
    {
        Point expectedCenter = new Point(
                BoardPanel.COL_COUNT / 2,
                BoardPanel.ROW_COUNT / 2
        );
        assertEquals(expectedCenter, snake.getFirst());
    }

    @Test
    public void resetGame_directionsQueueHasOneEntry() throws Exception
    {
        assertEquals(1, directions.size());
    }

    @Test
    public void resetGame_defaultDirectionIsNorth() throws Exception
    {
        assertEquals(Direction.North, directions.getFirst());
    }

    @Test
    public void resetGame_boardWasCleared() throws Exception
    {
        Mockito.verify(boardMock, Mockito.times(1)).clearBoard();
    }

    @Test
    public void resetGame_headTileSetToSnakeHead() throws Exception
    {
        Point expectedCenter = new Point(
                BoardPanel.COL_COUNT / 2,
                BoardPanel.ROW_COUNT / 2
        );
        Mockito.verify(boardMock, Mockito.times(1))
                .setTile(expectedCenter, TileType.SnakeHead);
    }

    // Calls -resetGame() method via reflection
    private void invokeResetGame() throws Exception
    {
        Method m = SnakeGame.class.getDeclaredMethod("resetGame");
        m.setAccessible(true);
        m.invoke(game);
    }

    // Calls private game instance via reflection
    private void setField(String name, Object value) throws Exception
    {
        Field f = SnakeGame.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(game, value);
    }

    // Reads private int field from the game instance via reflection
    private int getIntField(String name) throws Exception
    {
        Field f = SnakeGame.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.getInt(game);
    }

    // Reads private boolean field from the game instance via reflection
    private boolean getBooleanField(String name) throws Exception
    {
        Field f = SnakeGame.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.getBoolean(game);
    }
}
