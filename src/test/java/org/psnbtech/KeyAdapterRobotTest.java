package org.psnbtech;

import org.junit.jupiter.api.*;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ================================================================
 *  Robot Integration Tests  —  KeyAdapter
 * ================================================================
 *
 *  Sends real OS keystrokes through java.awt.Robot.
 *  Auto-skipped on headless environments (CI servers, no display).
 *
 *  Guards via: Assumptions.assumeFalse(GraphicsEnvironment.isHeadless())
 *  Tag: @Tag("robot") — run separately with: mvn test -Dgroups=robot
 */
@Tag("robot")
@DisplayName("Robot Integration Tests — KeyAdapter")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class KeyAdapterRobotTest extends SnakeReflectionHelper {

    private static SnakeRobotHarness harness;

    @BeforeAll
    static void launchGame() throws Exception {
        Assumptions.assumeFalse(
            GraphicsEnvironment.isHeadless(),
            "Skipping Robot tests — no display available (headless environment)"
        );
        harness = new SnakeRobotHarness();
        harness.launch();
    }

    @AfterAll
    static void closeGame() {
        if (harness != null) harness.dispose();
    }

    @BeforeEach
    void resetState() throws Exception {
        SnakeGame game = harness.getGame();

        // Reset to a clean state before each test
        setBool(game, "isNewGame",  true);
        setBool(game, "isGameOver", false);
        setBool(game, "isPaused",   false);
        setInt (game, "score",       0);
        setInt (game, "fruitsEaten", 0);

        LinkedList<Direction> dirs = harness.getDirections();
        dirs.clear();
        dirs.add(Direction.North);

        harness.focusWindow();
    }

    // ====================================================================
    //  Robot-A — Pause toggle (VK_P)
    // ====================================================================
    @Test @Order(1)
    @DisplayName("Robot-A: VK_P toggles isPaused when game not over")
    void robotA_pauseToggle() throws Exception {
        setBool(harness.getGame(), "isGameOver", false);
        setBool(harness.getGame(), "isPaused",   false);

        harness.pressKey(KeyEvent.VK_P);
        assertTrue(harness.isPaused(), "First VK_P must set isPaused=true");

        harness.pressKey(KeyEvent.VK_P);
        assertFalse(harness.isPaused(), "Second VK_P must set isPaused=false");
    }

    // ====================================================================
    //  Robot-B — North via VK_W
    // ====================================================================
    @Test @Order(2)
    @DisplayName("Robot-B: VK_W enqueues North when last=East")
    void robotB_northViaW() throws Exception {
        setBool(harness.getGame(), "isPaused",   false);
        setBool(harness.getGame(), "isGameOver", false);
        LinkedList<Direction> dirs = harness.getDirections();
        dirs.clear();
        dirs.add(Direction.East);

        harness.pressKey(KeyEvent.VK_W);

        assertTrue(dirs.contains(Direction.North),
            "VK_W with last=East must enqueue Direction.North");
    }

    // ====================================================================
    //  Robot-C — Anti-reversal: North blocked when last=South
    // ====================================================================
    @Test @Order(3)
    @DisplayName("Robot-C: VK_W blocked when last=South (anti-reversal)")
    void robotC_northBlocked_antiReversal() throws Exception {
        setBool(harness.getGame(), "isPaused",   false);
        setBool(harness.getGame(), "isGameOver", false);
        LinkedList<Direction> dirs = harness.getDirections();
        dirs.clear();
        dirs.add(Direction.South);

        int before = dirs.size();
        harness.pressKey(KeyEvent.VK_W);

        assertEquals(before, dirs.size(),
            "VK_W when last=South must be blocked by anti-reversal guard");
    }

    // ====================================================================
    //  Robot-D — Enter resets game (isNewGame=true)
    // ====================================================================
    @Test @Order(4)
    @DisplayName("Robot-D: VK_ENTER fires resetGame() when isNewGame=true")
    void robotD_enterResetsGame() throws Exception {
        setBool(harness.getGame(), "isNewGame",  true);
        setBool(harness.getGame(), "isGameOver", false);
        setInt (harness.getGame(), "score",       999);

        harness.pressKey(KeyEvent.VK_ENTER);
        Thread.sleep(200);   // allow resetGame() + spawnFruit() to complete

        assertEquals(0, harness.getScore(),
            "VK_ENTER when isNewGame=true must reset score to 0");
        assertFalse(harness.isNewGame(),
            "resetGame() must clear the isNewGame flag");
    }

    // ====================================================================
    //  Robot-E — South via VK_DOWN
    // ====================================================================
    @Test @Order(5)
    @DisplayName("Robot-E: VK_DOWN enqueues South when last=East")
    void robotE_southViaDown() throws Exception {
        setBool(harness.getGame(), "isPaused",   false);
        setBool(harness.getGame(), "isGameOver", false);
        LinkedList<Direction> dirs = harness.getDirections();
        dirs.clear();
        dirs.add(Direction.East);

        harness.pressKey(KeyEvent.VK_DOWN);

        assertTrue(dirs.contains(Direction.South),
            "VK_DOWN with last=East must enqueue Direction.South");
    }

    // ====================================================================
    //  Robot-F — East via VK_RIGHT
    // ====================================================================
    @Test @Order(6)
    @DisplayName("Robot-F: VK_RIGHT enqueues East when last=North")
    void robotF_eastViaRight() throws Exception {
        setBool(harness.getGame(), "isPaused",   false);
        setBool(harness.getGame(), "isGameOver", false);
        LinkedList<Direction> dirs = harness.getDirections();
        dirs.clear();
        dirs.add(Direction.North);

        harness.pressKey(KeyEvent.VK_RIGHT);

        assertTrue(dirs.contains(Direction.East),
            "VK_RIGHT with last=North must enqueue Direction.East");
    }

    // ====================================================================
    //  Robot-G — West via VK_LEFT
    // ====================================================================
    @Test @Order(7)
    @DisplayName("Robot-G: VK_LEFT enqueues West when last=North")
    void robotG_westViaLeft() throws Exception {
        setBool(harness.getGame(), "isPaused",   false);
        setBool(harness.getGame(), "isGameOver", false);
        LinkedList<Direction> dirs = harness.getDirections();
        dirs.clear();
        dirs.add(Direction.North);

        harness.pressKey(KeyEvent.VK_LEFT);

        assertTrue(dirs.contains(Direction.West),
            "VK_LEFT with last=North must enqueue Direction.West");
    }

    // ====================================================================
    //  Robot-H — Queue full guard (C3=false)
    // ====================================================================
    @Test @Order(8)
    @DisplayName("Robot-H: VK_W blocked when queue is full (size=3)")
    void robotH_queueFull() throws Exception {
        setBool(harness.getGame(), "isPaused",   false);
        setBool(harness.getGame(), "isGameOver", false);
        LinkedList<Direction> dirs = harness.getDirections();
        dirs.clear();
        dirs.add(Direction.East);
        dirs.add(Direction.South);
        dirs.add(Direction.West);   // size = 3 = MAX_DIRECTIONS

        harness.pressKey(KeyEvent.VK_W);

        assertEquals(3, dirs.size(),
            "VK_W when queue is full must not increase queue size");
    }
}
