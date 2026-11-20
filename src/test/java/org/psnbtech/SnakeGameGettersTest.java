package org.psnbtech;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.LinkedList;

/**
 * JUnit tests for the public getter methods on SnakeGame.
 *
 * SnakeGame extends JFrame, and the test environment is configured to be
 * headless (-Djava.awt.headless=true in pom.xml). Calling the real
 * SnakeGame constructor in this environment throws HeadlessException.
 *
 * To work around this, Mockito.mock with CALLS_REAL_METHODS, which
 * creates an instance WITHOUT running the JFrame constructor but still
 * delegates each method call to the real implementation, then sets the
 * private fields via reflection to control what each getter returns.
 */
@RunWith(MockitoJUnitRunner.class)
public class SnakeGameGettersTest
{
    private SnakeGame game;

    @Before
    public void setUp() throws Exception
    {
        // Creates a SnakeGame instance without running its JFrame constructor.
        // CALLS_REAL_METHODS ensures the actual getter code is executed.
        game = Mockito.mock(SnakeGame.class, Mockito.CALLS_REAL_METHODS);

        // Initializes the directions queue so getDirection() doesn't give a null pointer exception
        // The real game does this in startGame(), which is not called.
        setVariable("directions", new LinkedList<Direction>());
    }

    // Sets a private instance variable on the game via reflection
    private void setVariable(String name, Object value) throws Exception
    {
        Field variable = SnakeGame.class.getDeclaredField(name);
        variable.setAccessible(true);
        variable.set(game, value);
    }

    @Test
    public void isGameOverReturnsTrueWhenFlagSet() throws Exception
    {
        setVariable("isGameOver", true);
        assertTrue(game.isGameOver());
    }

    @Test
    public void isGameOverReturnsFalseWhenFlagNotSet() throws Exception
    {
        setVariable("isGameOver", false);
        assertFalse(game.isGameOver());
    }

    @Test
    public void isPausedReturnsTrueWhenFlagSet() throws Exception
    {
        setVariable("isPaused", true);
        assertTrue(game.isPaused());
    }

    @Test
    public void isPausedReturnsFalseWhenFlagNotSet() throws Exception
    {
        setVariable("isPaused", false);
        assertFalse(game.isPaused());
    }


    @Test
    public void isNewGameReturnsTrueWhenFlagSet() throws Exception
    {
        setVariable("isNewGame", true);
        assertTrue(game.isNewGame());
    }

    @Test
    public void isNewGameReturnsFalseWhenFlagNotSet() throws Exception
    {
        setVariable("isNewGame", false);
        assertFalse(game.isNewGame());
    }

    // Chose random, distinct, realistic, non-zero values for testing
    @Test
    public void getScoreReturnsCurrentScore() throws Exception
    {
        setVariable("score", 4250);
        assertEquals(4250, game.getScore());
    }

    @Test
    public void getFruitsEatenReturnsCount() throws Exception
    {
        setVariable("fruitsEaten", 17);
        assertEquals(17, game.getFruitsEaten());
    }

    @Test
    public void getNextFruitScoreReturnsCurrentValue() throws Exception
    {
        setVariable("nextFruitScore", 75);
        assertEquals(75, game.getNextFruitScore());
    }

    @Test
    // Returns directions.peek() to front of the queue
    public void getDirectionReturnsFrontOfQueue() throws Exception
    {
        LinkedList<Direction> queue = new LinkedList<>();
        queue.add(Direction.East);
        setVariable("directions", queue);

        assertEquals(Direction.East, game.getDirection());
    }

    @Test
    // peek() returns null on an empty queue, does not throw
    public void getDirectionReturnsNullWhenQueueIsEmpty() throws Exception
    {
        setVariable("directions", new LinkedList<Direction>());
        assertNull(game.getDirection());
    }
}