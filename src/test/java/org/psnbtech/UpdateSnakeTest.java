package org.psnbtech;

import org.junit.jupiter.api.*;
import java.awt.Point;
import java.lang.reflect.Method;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("PPC Tests — updateSnake()")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UpdateSnakeTest extends SnakeReflectionHelper {

    private SnakeGame             game;
    private BoardPanel            board;
    private LinkedList<Point>     snake;
    private LinkedList<Direction> dirs;
    private Method                updateSnake;

    // Constants from BoardPanel
    private static final int COL = BoardPanel.COL_COUNT;  // 25
    private static final int ROW = BoardPanel.ROW_COUNT;  // 25

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        game  = createGame();                              // bypass private constructor
        board = getBoard(game);
        snake = getSnake(game);
        dirs  = getDirections(game);

        // Expose the private method
        updateSnake = getMethod(game, "updateSnake");

        // Start every test with a clean board
        board.clearBoard();
        snake.clear();
        dirs.clear();
    }

    /** Invoke updateSnake() via reflection and unwrap the TileType return. */
    private TileType invoke() throws Exception {
        return (TileType) updateSnake.invoke(game);
    }

    // ====================================================================
    //  TR-1  PP1 — North wall collision
    //  CFG path: Node1→2→3(wall)→return SnakeBody
    //
    //  Input:  head at (12, 0), moving North  →  new y = -1  (out of bounds)
    //  Expected: returns TileType.SnakeBody
    // ====================================================================
    @Test @Order(1)
    @DisplayName("TR-1 [PP1] North wall — head=(12,0) dir=North → SnakeBody")
    void tr1_northWall() throws Exception {
        Point head = new Point(12, 0);
        snake.add(head);
        board.setTile(head, TileType.SnakeHead);
        dirs.add(Direction.North);

        assertEquals(TileType.SnakeBody, invoke(),
            "Moving North from y=0 hits the top wall → must return SnakeBody");
    }

    // ====================================================================
    //  TR-2  PP1 — South wall collision
    //  CFG path: Node1→2→3(wall)→return SnakeBody
    //
    //  Input:  head at (12, 24), moving South  →  new y = 25  (>= ROW_COUNT=25)
    //  Expected: returns TileType.SnakeBody
    // ====================================================================
    @Test @Order(2)
    @DisplayName("TR-2 [PP1] South wall — head=(12,24) dir=South → SnakeBody")
    void tr2_southWall() throws Exception {
        Point head = new Point(12, ROW - 1);
        snake.add(head);
        board.setTile(head, TileType.SnakeHead);
        dirs.add(Direction.South);

        assertEquals(TileType.SnakeBody, invoke(),
            "Moving South from y=24 hits the bottom wall → must return SnakeBody");
    }

    // ====================================================================
    //  TR-3  PP1 — East wall collision
    //  CFG path: Node1→2→3(wall)→return SnakeBody
    //
    //  Input:  head at (24, 12), moving East  →  new x = 25  (>= COL_COUNT=25)
    //  Expected: returns TileType.SnakeBody
    // ====================================================================
    @Test @Order(3)
    @DisplayName("TR-3 [PP1] East wall — head=(24,12) dir=East → SnakeBody")
    void tr3_eastWall() throws Exception {
        Point head = new Point(COL - 1, 12);
        snake.add(head);
        board.setTile(head, TileType.SnakeHead);
        dirs.add(Direction.East);

        assertEquals(TileType.SnakeBody, invoke(),
            "Moving East from x=24 hits the right wall → must return SnakeBody");
    }

    // ====================================================================
    //  TR-4  PP1 — West wall collision
    //  CFG path: Node1→2→3(wall)→return SnakeBody
    //
    //  Input:  head at (0, 12), moving West  →  new x = -1  (< 0)
    //  Expected: returns TileType.SnakeBody
    // ====================================================================
    @Test @Order(4)
    @DisplayName("TR-4 [PP1] West wall — head=(0,12) dir=West → SnakeBody")
    void tr4_westWall() throws Exception {
        Point head = new Point(0, 12);
        snake.add(head);
        board.setTile(head, TileType.SnakeHead);
        dirs.add(Direction.West);

        assertEquals(TileType.SnakeBody, invoke(),
            "Moving West from x=0 hits the left wall → must return SnakeBody");
    }

    // ====================================================================
    //  TR-5  PP3 — Normal move North, no collision, single-dir queue
    //  CFG path: 1→2→3→4→5[F]→9[T]→10→11→12→13[F]→15
    //  Node 5 [RED DIAMOND]: old!=Fruit && size>MIN  →  FALSE (size=1, not >5)
    //  Node 9 check: old (null tile) != SnakeBody    →  TRUE  → update board
    //  Node 13 [RED DIAMOND]: dirs.size()>1          →  FALSE (1 dir) → no poll
    //
    //  Input:  head=(12,10) dir=North, (12,9) is empty, snake.size=1
    //  Expected: returns null, snake.peekFirst()=(12,9)
    // ====================================================================
    @Test @Order(5)
    @DisplayName("TR-5 [PP3] Normal North move — returns null, head moves to (12,9)")
    void tr5_normalNorth() throws Exception {
        Point head = new Point(12, 10);
        snake.add(head);
        board.setTile(head, TileType.SnakeHead);
        dirs.add(Direction.North);  // single direction

        TileType result = invoke();

        assertNull(result,                      "Empty tile → must return null");
        assertEquals(new Point(12, 9), snake.peekFirst(), "Head must move one step North");
        assertEquals(1, dirs.size(),            "Single-dir queue must not be polled");
    }

    // ====================================================================
    //  TR-6  PP3 — Normal move South
    //  Same path as TR-5 but direction = South  →  head.y++
    //
    //  Input:  head=(12,10) dir=South, (12,11) is empty
    //  Expected: returns null, head=(12,11)
    // ====================================================================
    @Test @Order(6)
    @DisplayName("TR-6 [PP3] Normal South move — head moves to (12,11)")
    void tr6_normalSouth() throws Exception {
        Point head = new Point(12, 10);
        snake.add(head);
        board.setTile(head, TileType.SnakeHead);
        dirs.add(Direction.South);

        assertNull(invoke());
        assertEquals(new Point(12, 11), snake.peekFirst());
    }

    // ====================================================================
    //  TR-7  PP3 — Normal move East
    //  Input:  head=(12,10) dir=East, (13,10) is empty
    //  Expected: returns null, head=(13,10)
    // ====================================================================
    @Test @Order(7)
    @DisplayName("TR-7 [PP3] Normal East move — head moves to (13,10)")
    void tr7_normalEast() throws Exception {
        Point head = new Point(12, 10);
        snake.add(head);
        board.setTile(head, TileType.SnakeHead);
        dirs.add(Direction.East);

        assertNull(invoke());
        assertEquals(new Point(13, 10), snake.peekFirst());
    }

    // ====================================================================
    //  TR-8  PP3 — Normal move West
    //  Input:  head=(12,10) dir=West, (11,10) is empty
    //  Expected: returns null, head=(11,10)
    // ====================================================================
    @Test @Order(8)
    @DisplayName("TR-8 [PP3] Normal West move — head moves to (11,10)")
    void tr8_normalWest() throws Exception {
        Point head = new Point(12, 10);
        snake.add(head);
        board.setTile(head, TileType.SnakeHead);
        dirs.add(Direction.West);

        assertNull(invoke());
        assertEquals(new Point(11, 10), snake.peekFirst());
    }

    // ====================================================================
    //  TR-9  PP8 — Fruit collision (snake size <= MIN_SNAKE_LENGTH)
    //  CFG path: 1→2→3→4→5[F]→9[T]→10→11→12→13[F]→15
    //  Node 5 [RED DIAMOND]: old==Fruit → (old!=Fruit) is FALSE → no tail remove
    //  Node 9: old (Fruit) != SnakeBody → TRUE → update board, push new head
    //
    //  Input:  head=(12,10), dir=North, Fruit placed at (12,9), snake.size=1
    //  Expected: returns TileType.Fruit, snake.size grows to 2
    // ====================================================================
    @Test @Order(9)
    @DisplayName("TR-9 [PP8] Fruit collision — returns Fruit, snake grows")
    void tr9_fruitCollision() throws Exception {
        Point head = new Point(12, 10);
        snake.add(head);
        board.setTile(head, TileType.SnakeHead);
        board.setTile(12, 9, TileType.Fruit);   // fruit directly north
        dirs.add(Direction.North);

        TileType result = invoke();

        assertEquals(TileType.Fruit, result,
            "Head moved onto Fruit tile → must return TileType.Fruit");
        assertEquals(2, snake.size(),
            "Snake must grow: old head stays as body, new head at fruit position");
        assertEquals(new Point(12, 9), snake.peekFirst(),
            "New head must be at the fruit tile position");
    }

    // ====================================================================
    //  TR-10  PP6 — Body collision, no tail removal (size <= MIN)
    //  CFG path: 1→2→3→4→5[F]→9[F]→15
    //  Node 5 [RED DIAMOND]: size=2, not >5 → FALSE → skip tail removal
    //  Node 9: old == SnakeBody → FALSE → skip board update, return SnakeBody
    //
    //  Input:  head=(12,10), body@(12,9), dir=North, snake=[head,body]
    //  Expected: returns SnakeBody, head position unchanged
    // ====================================================================
    @Test @Order(10)
    @DisplayName("TR-10 [PP6] Body collision — returns SnakeBody, head unchanged")
    void tr10_bodyCollision() throws Exception {
        Point head = new Point(12, 10);
        Point body = new Point(12, 9);
        snake.add(head);
        snake.add(body);
        board.setTile(head, TileType.SnakeHead);
        board.setTile(body, TileType.SnakeBody);
        dirs.add(Direction.North);

        TileType result = invoke();

        assertEquals(TileType.SnakeBody, result,
            "Head moved into SnakeBody tile → must return TileType.SnakeBody");
        assertEquals(head, snake.peekFirst(),
            "Head position must be unchanged after body collision");
    }

    // ====================================================================
    //  TR-11  PP4 — Tail removed, no collision, multi-dir queue
    //  CFG path: 1→2→3→4→5[T]→6→7→8→9[T]→10→11→12→13[T]→14→15
    //  Node 5 [RED DIAMOND]: size=6 > 5  AND  old==null!=Fruit → TRUE → remove tail
    //  Nodes 6,7,8: remove tail, clear tile, re-fetch old
    //  Node 9: old still null → T → update board
    //  Node 13 [RED DIAMOND]: dirs.size()=2 > 1 → TRUE → poll direction
    //
    //  Input:  snake of 6 segments, dir=North, 2-dir queue, (12,9) empty
    //  Expected: returns null, snake.size stays 6, dirs queue down to 1
    // ====================================================================
    @Test @Order(11)
    @DisplayName("TR-11 [PP4] Tail removed, multi-dir queue — size stays 6, queue polled")
    void tr11_tailRemoved_multiDir() throws Exception {
        // Build a snake of length 6: head at (12,10), body stretching south
        Point head = new Point(12, 10);
        snake.add(head);
        board.setTile(head, TileType.SnakeHead);
        for (int i = 1; i <= 5; i++) {
            Point seg = new Point(12, 10 + i);
            snake.add(seg);
            board.setTile(seg, TileType.SnakeBody);
        }
        dirs.add(Direction.North);
        dirs.add(Direction.East);   // second direction → queue size = 2

        TileType result = invoke();

        assertNull(result,               "Empty tile north → must return null");
        assertEquals(6, snake.size(),    "Tail removed and new head added → size stays 6");
        assertEquals(1, dirs.size(),     "Multi-dir queue: first direction polled after move");
    }

    // ====================================================================
    //  TR-12  PP5 — Tail removed, no collision, single-dir queue
    //  CFG path: 1→2→3→4→5[T]→6→7→8→9[T]→10→11→12→13[F]→15
    //  Node 13 [RED DIAMOND]: dirs.size()=1, NOT >1 → FALSE → no poll
    //
    //  Input:  snake of 6 segments, dir=North, 1-dir queue, (12,9) empty
    //  Expected: returns null, snake.size stays 6, dirs queue stays 1
    // ====================================================================
    @Test @Order(12)
    @DisplayName("TR-12 [PP5] Tail removed, single-dir queue — size stays 6, no poll")
    void tr12_tailRemoved_singleDir() throws Exception {
        Point head = new Point(12, 10);
        snake.add(head);
        board.setTile(head, TileType.SnakeHead);
        for (int i = 1; i <= 5; i++) {
            Point seg = new Point(12, 10 + i);
            snake.add(seg);
            board.setTile(seg, TileType.SnakeBody);
        }
        dirs.add(Direction.North);  // only one direction

        TileType result = invoke();

        assertNull(result,            "Empty tile north → must return null");
        assertEquals(6, snake.size(), "Tail removed and new head added → size stays 6");
        assertEquals(1, dirs.size(),  "Single-dir queue: must NOT be polled");
    }

    // ====================================================================
    //  TR-13  PP7 — Tail removed, but re-fetched tile is still SnakeBody
    //  CFG path: 1→2→3→4→5[T]→6→7→8→9[F]→15
    //  Node 5: size=6>5 AND old!=Fruit → TRUE → remove tail
    //  Nodes 6,7: tail at (12,15) removed; tile (12,15) cleared
    //  Node 8: re-fetch old at (12,9) → still SnakeBody (not the removed tail)
    //  Node 9: old == SnakeBody → FALSE → skip update, return SnakeBody
    //
    //  Input:  snake=[head(12,10), body(12,9), ...4 more south], dir=North
    //          body at (12,9) is NOT the tail → after tail removal it stays SnakeBody
    //  Expected: returns SnakeBody
    // ====================================================================
    @Test @Order(13)
    @DisplayName("TR-13 [PP7] Tail removed but head still hits body — returns SnakeBody")
    void tr13_tailRemoved_bodyCollision() throws Exception {
        // snake: head(12,10), body(12,9)[collision target], then 4 more south
        Point head  = new Point(12, 10);
        Point body1 = new Point(12, 9);   // directly north — collision target
        snake.add(head);
        board.setTile(head,  TileType.SnakeHead);
        board.setTile(body1, TileType.SnakeBody);
        snake.add(body1);
        for (int i = 1; i <= 4; i++) {
            Point seg = new Point(12, 10 + i);
            snake.add(seg);
            board.setTile(seg, TileType.SnakeBody);
        }
        dirs.add(Direction.North);

        TileType result = invoke();

        // Tail is removed (the southernmost segment) but (12,9) is still SnakeBody
        assertEquals(TileType.SnakeBody, result,
            "After tail removal, head still hits internal body → must return SnakeBody");
    }

    // ====================================================================
    //  TR-14  PP2/PP3 — Board state is correct after a normal move
    //  Verifies that Node10, Node11, Node12 (board update sequence) work:
    //    - old head position set to SnakeBody
    //    - new head pushed onto snake list
    //    - new head position set to SnakeHead
    // ====================================================================
    @Test @Order(14)
    @DisplayName("TR-14 Board state correct after move — old pos=Body, new pos=Head")
    void tr14_boardStateAfterMove() throws Exception {
        Point head = new Point(12, 10);
        snake.add(head);
        board.setTile(head, TileType.SnakeHead);
        dirs.add(Direction.East);

        invoke();

        assertEquals(TileType.SnakeBody, board.getTile(12, 10),
            "Old head position (12,10) must become SnakeBody after the move");
        assertEquals(TileType.SnakeHead, board.getTile(13, 10),
            "New head position (13,10) must be SnakeHead after the move");
    }
}
