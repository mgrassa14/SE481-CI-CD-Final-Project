package org.psnbtech;

import org.junit.jupiter.api.*;
import java.awt.Canvas;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.Method;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("CACC Tests — KeyAdapter (direct reflection)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class KeyAdapterCACCTest extends SnakeReflectionHelper {

    private SnakeGame             game;
    private KeyListener           keyListener;
    private LinkedList<Direction> dirs;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        game = createGame();

        // Inject dependencies the KeyAdapter touches
        setField(game, "logicTimer", new Clock(9.0f));
        setField(game, "random",     new java.util.Random(42L));

        dirs = getDirections(game);
        dirs.clear();
        dirs.add(Direction.North);   // default starting direction

        // Minimal board+snake so resetGame() in TR-14/15 won't NPE
        BoardPanel board = getBoard(game);
        board.clearBoard();
        LinkedList<Point> snake = getSnake(game);
        snake.clear();
        snake.add(new Point(12, 12));
        board.setTile(new Point(12, 12), TileType.SnakeHead);

        setInt(game, "score",       0);
        setInt(game, "fruitsEaten", 0);

        // Extract the KeyAdapter registered in the constructor
        KeyListener[] listeners = game.getKeyListeners();
        assertTrue(listeners.length > 0, "SnakeGame must register at least one KeyListener");
        keyListener = listeners[0];
    }

    /**
     * Fires a keyPressed event directly on the KeyAdapter.
     * This is equivalent to pressing the key on a real keyboard,
     * without needing a display or java.awt.Robot.
     */
    private void press(int keyCode) {
        KeyEvent evt = new KeyEvent(
            new Canvas(),                   // source component (dummy)
            KeyEvent.KEY_PRESSED,
            System.currentTimeMillis(),
            0,                              // modifiers
            keyCode,
            KeyEvent.CHAR_UNDEFINED
        );
        keyListener.keyPressed(evt);
    }

    // ====================================================================
    //  TR-1  C1 = FALSE  (isPaused = true)
    //  Predicate: !isPaused && !isGameOver && ...
    //  C1=false → entire predicate false → North must NOT be enqueued
    // ====================================================================
    @Test @Order(1)
    @DisplayName("TR-1 C1=F isPaused=true → North blocked")
    void tr1_paused_blocksNorth() throws Exception {
        setBool(game, "isPaused",   true);
        setBool(game, "isGameOver", false);
        int before = dirs.size();

        press(KeyEvent.VK_W);

        assertEquals(before, dirs.size(),
            "TR-1: isPaused=true → C1 is false → North must not be enqueued");
    }

    // ====================================================================
    //  TR-2  C2 = FALSE  (isGameOver = true)
    //  C1=T but C2=F → predicate false → North must NOT be enqueued
    // ====================================================================
    @Test @Order(2)
    @DisplayName("TR-2 C2=F isGameOver=true → North blocked")
    void tr2_gameOver_blocksNorth() throws Exception {
        setBool(game, "isPaused",   false);
        setBool(game, "isGameOver", true);
        int before = dirs.size();

        press(KeyEvent.VK_W);

        assertEquals(before, dirs.size(),
            "TR-2: isGameOver=true → C2 is false → North must not be enqueued");
    }

    // ====================================================================
    //  TR-3  C3 = FALSE  (queue is full — size = MAX_DIRECTIONS = 3)
    //  C1=T, C2=T, but C3=F → predicate false → North must NOT be enqueued
    // ====================================================================
    @Test @Order(3)
    @DisplayName("TR-3 C3=F queue full (size=3) → North blocked")
    void tr3_queueFull_blocksNorth() throws Exception {
        setBool(game, "isPaused",   false);
        setBool(game, "isGameOver", false);
        dirs.clear();
        dirs.add(Direction.East);
        dirs.add(Direction.South);
        dirs.add(Direction.West);   // size = 3 = MAX_DIRECTIONS

        press(KeyEvent.VK_W);

        assertEquals(3, dirs.size(),
            "TR-3: queue full → C3 is false → North must not be enqueued");
    }

    // ====================================================================
    //  TR-4  C4 = FALSE for North  (last == South = reverse)
    //  Source: if(last != Direction.South && last != Direction.North)
    //  last=South → (last!=South) is FALSE → C4 false → North NOT enqueued
    // ====================================================================
    @Test @Order(4)
    @DisplayName("TR-4 C4=F last=South (reverse) → North blocked (anti-reversal)")
    void tr4_antiReversal_southBlocksNorth() throws Exception {
        setBool(game, "isPaused",   false);
        setBool(game, "isGameOver", false);
        dirs.clear();
        dirs.add(Direction.South);   // last = South → reverse of North

        press(KeyEvent.VK_W);

        assertFalse(dirs.contains(Direction.North),
            "TR-4: last=South → anti-reversal guard prevents North from being queued");
    }

    // ====================================================================
    //  TR-5  C4 = TRUE for North  (last == East = adjacent, valid)
    //  All C1-C4 true → North IS enqueued
    // ====================================================================
    @Test @Order(5)
    @DisplayName("TR-5 C4=T last=East (adjacent) → North enqueued")
    void tr5_validNorth() throws Exception {
        setBool(game, "isPaused",   false);
        setBool(game, "isGameOver", false);
        dirs.clear();
        dirs.add(Direction.East);    // last = East → adjacent to North

        press(KeyEvent.VK_W);

        assertTrue(dirs.contains(Direction.North),
            "TR-5: All conditions met → North must be enqueued");
    }

    // ====================================================================
    //  TR-6  C4 = FALSE for South  (last == North = reverse)
    //  Source: if(last != Direction.North && last != Direction.South)
    // ====================================================================
    @Test @Order(6)
    @DisplayName("TR-6 C4=F last=North (reverse) → South blocked")
    void tr6_antiReversal_northBlocksSouth() throws Exception {
        setBool(game, "isPaused",   false);
        setBool(game, "isGameOver", false);
        dirs.clear();
        dirs.add(Direction.North);   // last = North → reverse of South

        press(KeyEvent.VK_S);

        assertFalse(dirs.contains(Direction.South),
            "TR-6: last=North → South is blocked by anti-reversal guard");
    }

    // ====================================================================
    //  TR-7  C4 = TRUE for South  (last == East = adjacent)
    // ====================================================================
    @Test @Order(7)
    @DisplayName("TR-7 C4=T last=East (adjacent) → South enqueued")
    void tr7_validSouth() throws Exception {
        setBool(game, "isPaused",   false);
        setBool(game, "isGameOver", false);
        dirs.clear();
        dirs.add(Direction.East);

        press(KeyEvent.VK_S);

        assertTrue(dirs.contains(Direction.South),
            "TR-7: last=East → South must be enqueued");
    }

    // ====================================================================
    //  TR-8  C4 = FALSE for West  (last == East = reverse)
    //  Source: if(last != Direction.East && last != Direction.West)
    // ====================================================================
    @Test @Order(8)
    @DisplayName("TR-8 C4=F last=East (reverse) → West blocked")
    void tr8_antiReversal_eastBlocksWest() throws Exception {
        setBool(game, "isPaused",   false);
        setBool(game, "isGameOver", false);
        dirs.clear();
        dirs.add(Direction.East);    // last = East → reverse of West

        press(KeyEvent.VK_A);

        assertFalse(dirs.contains(Direction.West),
            "TR-8: last=East → West is blocked by anti-reversal guard");
    }

    // ====================================================================
    //  TR-9  C4 = TRUE for West  (last == North = adjacent)
    // ====================================================================
    @Test @Order(9)
    @DisplayName("TR-9 C4=T last=North (adjacent) → West enqueued")
    void tr9_validWest() throws Exception {
        setBool(game, "isPaused",   false);
        setBool(game, "isGameOver", false);
        dirs.clear();
        dirs.add(Direction.North);

        press(KeyEvent.VK_A);

        assertTrue(dirs.contains(Direction.West),
            "TR-9: last=North → West must be enqueued");
    }

    // ====================================================================
    //  TR-10  C4 = FALSE for East  (last == West = reverse)
    //  Source: if(last != Direction.West && last != Direction.East)
    // ====================================================================
    @Test @Order(10)
    @DisplayName("TR-10 C4=F last=West (reverse) → East blocked")
    void tr10_antiReversal_westBlocksEast() throws Exception {
        setBool(game, "isPaused",   false);
        setBool(game, "isGameOver", false);
        dirs.clear();
        dirs.add(Direction.West);    // last = West → reverse of East

        press(KeyEvent.VK_D);

        assertFalse(dirs.contains(Direction.East),
            "TR-10: last=West → East is blocked by anti-reversal guard");
    }

    // ====================================================================
    //  TR-11  C4 = TRUE for East  (last == North = adjacent)
    // ====================================================================
    @Test @Order(11)
    @DisplayName("TR-11 C4=T last=North (adjacent) → East enqueued")
    void tr11_validEast() throws Exception {
        setBool(game, "isPaused",   false);
        setBool(game, "isGameOver", false);
        dirs.clear();
        dirs.add(Direction.North);

        press(KeyEvent.VK_D);

        assertTrue(dirs.contains(Direction.East),
            "TR-11: last=North → East must be enqueued");
    }

    // ====================================================================
    //  TR-12  C5 = TRUE  (pause key, game NOT over → isPaused toggles)
    //  Predicate: !isGameOver
    //  C5=T → condition passes → isPaused toggles
    // ====================================================================
    @Test @Order(12)
    @DisplayName("TR-12 C5=T isGameOver=false → VK_P toggles isPaused")
    void tr12_pause_toggles() throws Exception {
        setBool(game, "isGameOver", false);
        setBool(game, "isPaused",   false);

        press(KeyEvent.VK_P);
        assertTrue(getBool(game, "isPaused"),
            "TR-12a: first VK_P must set isPaused to true");

        press(KeyEvent.VK_P);
        assertFalse(getBool(game, "isPaused"),
            "TR-12b: second VK_P must set isPaused back to false");
    }

    // ====================================================================
    //  TR-13  C5 = FALSE  (pause key, game IS over → isPaused unchanged)
    // ====================================================================
    @Test @Order(13)
    @DisplayName("TR-13 C5=F isGameOver=true → VK_P does NOT toggle isPaused")
    void tr13_pause_blockedByGameOver() throws Exception {
        setBool(game, "isGameOver", true);
        setBool(game, "isPaused",   false);

        press(KeyEvent.VK_P);

        assertFalse(getBool(game, "isPaused"),
            "TR-13: isGameOver=true → C5 false → isPaused must NOT change");
    }

    // ====================================================================
    //  TR-14  C6 = TRUE via isNewGame=true  (Enter key)
    //  Predicate: isNewGame || isGameOver
    //  isNewGame=T → predicate T → resetGame() fires
    //  Verifies: score reset to 0, fruitsEaten reset to 0, dirs=[North]
    // ====================================================================
    @Test @Order(14)
    @DisplayName("TR-14 C6=T via isNewGame → VK_ENTER triggers resetGame()")
    void tr14_enter_resetsGame_viaNewGame() throws Exception {
        setBool(game, "isNewGame",  true);
        setBool(game, "isGameOver", false);
        setInt (game, "score",       999);
        setInt (game, "fruitsEaten",  10);

        press(KeyEvent.VK_ENTER);

        assertEquals(0, getInt(game, "score"),
            "TR-14: resetGame() must set score to 0");
        assertEquals(0, getInt(game, "fruitsEaten"),
            "TR-14: resetGame() must set fruitsEaten to 0");
        assertEquals(Direction.North, dirs.peek(),
            "TR-14: resetGame() must add North as the first direction");
    }

    // ====================================================================
    //  TR-15  C6 = TRUE via isGameOver=true  (Enter key)
    //  isGameOver=T → predicate T → resetGame() fires
    // ====================================================================
    @Test @Order(15)
    @DisplayName("TR-15 C6=T via isGameOver → VK_ENTER triggers resetGame()")
    void tr15_enter_resetsGame_viaGameOver() throws Exception {
        setBool(game, "isNewGame",  false);
        setBool(game, "isGameOver", true);
        setInt (game, "score",       500);
        setInt (game, "fruitsEaten",   5);

        press(KeyEvent.VK_ENTER);

        assertEquals(0, getInt(game, "score"),
            "TR-15: resetGame() must set score to 0");
        assertFalse(getBool(game, "isGameOver"),
            "TR-15: resetGame() must clear isGameOver");
    }

    // ====================================================================
    //  TR-16  C6 = FALSE  (neither isNewGame nor isGameOver)
    //  Predicate: isNewGame || isGameOver → F || F = F
    //  → resetGame() must NOT be called → score unchanged
    // ====================================================================
    @Test @Order(16)
    @DisplayName("TR-16 C6=F neither flag → VK_ENTER does NOT reset")
    void tr16_enter_doesNotReset_duringGame() throws Exception {
        setBool(game, "isNewGame",  false);
        setBool(game, "isGameOver", false);
        setInt (game, "score",       250);

        press(KeyEvent.VK_ENTER);

        assertEquals(250, getInt(game, "score"),
            "TR-16: C6=false → resetGame() must NOT fire, score stays 250");
    }

    // ====================================================================
    //  TR-17  Arrow key parity  (VK_UP must behave exactly like VK_W)
    // ====================================================================
    @Test @Order(17)
    @DisplayName("TR-17 VK_UP behaves identically to VK_W → North enqueued")
    void tr17_upArrow_equalsW() throws Exception {
        setBool(game, "isPaused",   false);
        setBool(game, "isGameOver", false);
        dirs.clear();
        dirs.add(Direction.East);   // adjacent to North

        press(KeyEvent.VK_UP);

        assertTrue(dirs.contains(Direction.North),
            "TR-17: VK_UP must enqueue North just like VK_W");
    }
}
