package org.psnbtech;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.*;
import java.util.LinkedList;

/**
 * ================================================================
 *  SnakeRobotHarness  —  java.awt.Robot integration harness
 * ================================================================
 *
 *  Launches SnakeGame on a daemon thread and wraps java.awt.Robot
 *  so tests can send real OS-level keystrokes through the full
 *  AWT event pipeline.
 *
 *  USAGE IN A TEST CLASS:
 *  ----------------------
 *    class MyRobotTest extends SnakeReflectionHelper {
 *
 *        static SnakeRobotHarness harness;
 *
 *        @BeforeAll
 *        static void start() throws Exception {
 *            // Always guard — Robot needs a real display
 *            Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
 *            harness = new SnakeRobotHarness();
 *            harness.launch();
 *        }
 *
 *        @AfterAll
 *        static void stop() { if (harness != null) harness.dispose(); }
 *
 *        @Test
 *        void testPause() throws Exception {
 *            harness.focusWindow();
 *            harness.pressKey(KeyEvent.VK_P);
 *            assertTrue(harness.isPaused());
 *        }
 *    }
 *
 *  TIMING NOTE:
 *  The default delays (KEY_HOLD_MS=50, POST_KEY_MS=80) work reliably
 *  on macOS, Windows, and Ubuntu. If tests are flaky, increase them.
 */
public class SnakeRobotHarness extends SnakeReflectionHelper {

    public static final int KEY_HOLD_MS   = 50;   // ms to hold key down
    public static final int POST_KEY_MS   = 80;   // ms after key released
    public static final long LAUNCH_TIMEOUT_MS = 5_000;

    private SnakeGame game;
    private Robot     robot;
    private Thread    gameThread;

    /**
     * 1) Instantiates SnakeGame via reflection (private ctor).
     * 2) Pauses the clock so game ticks don't race test setup.
     * 3) Runs startGame() on a daemon thread.
     * 4) Waits for the window to appear.
     * 5) Creates Robot and focuses the window.
     */
    public void launch() throws Exception {
        game = createGame();

        // Pause clock before starting loop
        Clock clock = getField(game, "logicTimer", Clock.class);
        if (clock != null) clock.setPaused(true);

        // Run infinite game loop on daemon thread
        Method start = SnakeGame.class.getDeclaredMethod("startGame");
        start.setAccessible(true);
        gameThread = new Thread(() -> {
            try { start.invoke(game); } catch (Exception ignored) {}
        });
        gameThread.setDaemon(true);
        gameThread.setName("SnakeRobot-GameThread");
        gameThread.start();

        // Wait for window to become visible
        long deadline = System.currentTimeMillis() + LAUNCH_TIMEOUT_MS;
        while (!game.isShowing() && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
        }
        if (!game.isShowing()) {
            throw new IllegalStateException(
                "Game window did not appear within " + LAUNCH_TIMEOUT_MS + "ms");
        }

        robot = new Robot();
        robot.setAutoDelay(0);
        focusWindow();
    }

    /**
     * Brings the game window to the front and waits for OS focus.
     * Call this before every group of keystrokes.
     */
    public void focusWindow() throws InterruptedException {
        EventQueue.invokeLater(() -> {
            game.toFront();
            game.requestFocusInWindow();
        });
        Thread.sleep(250);
    }

    /**
     * Sends one key-press+release via Robot with configured delays.
     */
    public void pressKey(int keyCode) throws InterruptedException {
        robot.keyPress(keyCode);
        Thread.sleep(KEY_HOLD_MS);
        robot.keyRelease(keyCode);
        Thread.sleep(POST_KEY_MS);
    }

    /** Sends a sequence of keystrokes in order. */
    public void pressKeys(int... keyCodes) throws InterruptedException {
        for (int kc : keyCodes) pressKey(kc);
    }

    // --- State inspection via reflection ---

    @SuppressWarnings("unchecked")
    public LinkedList<Direction> getDirections() throws Exception {
        return getFieldRaw(game, "directions");
    }

    public boolean isPaused()   throws Exception { return getBool(game, "isPaused"); }
    public boolean isGameOver() throws Exception { return getBool(game, "isGameOver"); }
    public boolean isNewGame()  throws Exception { return getBool(game, "isNewGame"); }
    public int     getScore()   throws Exception { return getInt(game, "score"); }

    /** Returns the raw SnakeGame for direct reflection access in tests. */
    public SnakeGame getGame() { return game; }

    /** Disposes the JFrame. The daemon thread terminates automatically. */
    public void dispose() {
        if (game != null) EventQueue.invokeLater(game::dispose);
    }
}
