package org.psnbtech;

import org.junit.jupiter.api.*;
import java.awt.Point;
import java.lang.reflect.Method;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("PPC Tests — updateGame()")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UpdateGameTest extends SnakeReflectionHelper {

    private SnakeGame             game;
    private BoardPanel            board;
    private LinkedList<Point>     snake;
    private LinkedList<Direction> dirs;
    private Method                updateGame;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        game  = createGame();
        board = getBoard(game);
        snake = getSnake(game);
        dirs  = getDirections(game);

        updateGame = getMethod(game, "updateGame");

        board.clearBoard();
        snake.clear();
        dirs.clear();

        // updateGame() calls updateSnake() which calls spawnFruit() on fruit collision.
        // spawnFruit() uses 'random' and needs a Clock for game-over path.
        setField(game, "random",     new java.util.Random(42L));
        setField(game, "logicTimer", new Clock(9.0f));
    }

    /** Place a single-segment snake at (x,y) facing dir. */
    private void placeSnake(int x, int y, Direction dir) {
        Point head = new Point(x, y);
        snake.add(head);
        board.setTile(head, TileType.SnakeHead);
        dirs.add(dir);
    }

    // ====================================================================
    //  TR-1  PP1 — Fruit Collision
    //  CFG path: 1→2→3[T]→4(fruitsEaten++)→5(score+=nFS)→6(spawnFruit)→exit
    //
    //  Setup:  head=(12,10) dir=North, Fruit at (12,9)
    //          score=0, fruitsEaten=0, nextFruitScore=75
    //
    //  Expected (from diagram nodes 4,5,6):
    //    - fruitsEaten increments: 0 → 1
    //    - score increases by nFS at call time: 0+75 = 75
    //    - spawnFruit() resets nextFruitScore to 100
    //    - isGameOver stays false
    // ====================================================================
    @Test @Order(1)
    @DisplayName("TR-1 [PP1] Fruit collision — score+=75, fruitsEaten++, nFS reset to 100")
    void tr1_fruitCollision() throws Exception {
        placeSnake(12, 10, Direction.North);
        board.setTile(12, 9, TileType.Fruit);   // fruit directly north

        setInt(game, "score",          0);
        setInt(game, "fruitsEaten",    0);
        setInt(game, "nextFruitScore", 75);

        updateGame.invoke(game);

        // Node 4: fruitsEaten++
        assertEquals(1, getInt(game, "fruitsEaten"),
            "Node 4 [fruitsEaten++]: must increment from 0 to 1");
        // Node 5: score += nextFruitScore
        assertEquals(75, getInt(game, "score"),
            "Node 5 [score += nextFruitScore]: 0 + 75 = 75");
        // Node 6: spawnFruit() resets nextFruitScore to 100
        assertEquals(100, getInt(game, "nextFruitScore"),
            "Node 6 [spawnFruit()]: nextFruitScore must reset to 100");
        // Game should NOT be over
        assertFalse(getBool(game, "isGameOver"),
            "Fruit collision must NOT set isGameOver");
    }

    // ====================================================================
    //  TR-2  PP2 — SnakeBody/Wall Collision → Game Over
    //  CFG path: 1→2→3[F]→7[T]→8(isGameOver=true)→9(timer.setPaused)→exit
    //
    //  Setup:  head=(0,12) moving West → hits left wall (x becomes -1)
    //          updateSnake() returns SnakeBody (wall treated as body in source)
    //
    //  Expected (from diagram nodes 8,9):
    //    - isGameOver = true      (Node 8 in diagram)
    //    - logicTimer.setPaused(true)  (Node 9 in diagram)
    //    - score unchanged
    // ====================================================================
    @Test @Order(2)
    @DisplayName("TR-2 [PP2] Wall collision → isGameOver=true, timer paused")
    void tr2_wallCollision_gameOver() throws Exception {
        placeSnake(0, 12, Direction.West);  // moving into left wall

        setInt (game, "score",       100);
        setInt (game, "fruitsEaten",   2);
        setBool(game, "isGameOver",  false);

        updateGame.invoke(game);

        // Node 8: isGameOver = true
        assertTrue(getBool(game, "isGameOver"),
            "Node 8 [isGameOver = true]: must be set after wall/body collision");
        // Node 9: logicTimer.setPaused(true)
        Clock clock = getField(game, "logicTimer", Clock.class);
        assertTrue(clock.isPaused(),
            "Node 9 [logicTimer.setPaused(true)]: clock must be paused after game over");
        // Score must not change
        assertEquals(100, getInt(game, "score"),
            "Score must not change on game over");
        assertEquals(2, getInt(game, "fruitsEaten"),
            "fruitsEaten must not change on game over");
    }

    // ====================================================================
    //  TR-3  PP3 — No Collision, nextFruitScore > 10 → Decrement
    //  CFG path: 1→2→3[F]→7[F]→10[T]→11(nextFruitScore--)→exit
    //
    //  Setup:  head=(12,10) dir=North, (12,9) is empty
    //          nextFruitScore=50  (> 10 → condition is TRUE)
    //
    //  Expected (from diagram node 11):
    //    - nextFruitScore decrements: 50 → 49
    //    - isGameOver stays false
    // ====================================================================
    @Test @Order(3)
    @DisplayName("TR-3 [PP3] No collision, nFS=50 > 10 → nextFruitScore-- to 49")
    void tr3_noCollision_decrement() throws Exception {
        placeSnake(12, 10, Direction.North);
        // (12,9) is empty — no fruit, no body

        setInt (game, "score",          0);
        setInt (game, "fruitsEaten",    0);
        setInt (game, "nextFruitScore", 50);
        setBool(game, "isGameOver",    false);

        updateGame.invoke(game);

        // Node 11: nextFruitScore--
        assertEquals(49, getInt(game, "nextFruitScore"),
            "Node 11 [nextFruitScore--]: must decrement from 50 to 49 when > 10");
        assertFalse(getBool(game, "isGameOver"),
            "isGameOver must remain false after a normal move");
    }

    // ====================================================================
    //  TR-4  PP4 — No Collision, nextFruitScore == 10 → NO Decrement (Clamp)
    //  CFG path: 1→2→3[F]→7[F]→10[F]→12(skip)→exit
    //  Node 10 [RED DIAMOND in diagram]: nextFruitScore > 10 → FALSE (10 is NOT > 10)
    //  → takes false branch → Node 12 (skip/no-op)
    //
    //  Setup:  head=(12,10) dir=North, (12,9) empty, nextFruitScore=10
    //
    //  Expected: nextFruitScore stays at 10 (boundary clamp)
    //  Bug note: the clamp value 10 is a magic number in the source code —
    //            surfaced by this boundary test.
    // ====================================================================
    @Test @Order(4)
    @DisplayName("TR-4 [PP4] No collision, nFS=10 NOT >10 → stays at 10 (clamped)")
    void tr4_noCollision_clampAt10() throws Exception {
        placeSnake(12, 10, Direction.North);

        setInt (game, "score",          0);
        setInt (game, "fruitsEaten",    0);
        setInt (game, "nextFruitScore", 10);  // exact boundary
        setBool(game, "isGameOver",    false);

        updateGame.invoke(game);

        // Node 10: 10 > 10 is FALSE → no decrement → score stays at 10
        assertEquals(10, getInt(game, "nextFruitScore"),
            "Node 10 [nextFruitScore > 10 = FALSE]: must NOT decrement below 10");
    }

    // ====================================================================
    //  TR-5  PP1 — Fruit Collision with existing score (accumulation test)
    //  CFG path: same as TR-1 (PP1)
    //
    //  Setup:  existing score=200, fruitsEaten=3, nextFruitScore=60
    //  Expected: score = 200+60 = 260, fruitsEaten = 4
    // ====================================================================
    @Test @Order(5)
    @DisplayName("TR-5 [PP1] Fruit collision with existing score — 200+60=260")
    void tr5_fruitCollision_scoreAccumulates() throws Exception {
        placeSnake(12, 10, Direction.North);
        board.setTile(12, 9, TileType.Fruit);

        setInt(game, "score",          200);
        setInt(game, "fruitsEaten",      3);
        setInt(game, "nextFruitScore",  60);

        updateGame.invoke(game);

        assertEquals(260, getInt(game, "score"),
            "Score accumulation: 200 + 60 = 260");
        assertEquals(4, getInt(game, "fruitsEaten"),
            "fruitsEaten: 3 + 1 = 4");
    }
}
