package org.psnbtech;

import java.awt.Point;
import java.lang.reflect.*;
import java.util.LinkedList;

public abstract class SnakeReflectionHelper {

    /**
     * Creates a SnakeGame and initializes all fields (snake, directions,
     * logicTimer, random) by running startGame() on a daemon thread briefly,
     * then immediately pausing it so the game loop doesn't interfere.
     */
    protected static SnakeGame createGame() throws Exception {
        // Must disable headless mode before JFrame construction
        System.setProperty("java.awt.headless", "false");

        // Instantiate via private constructor
        Constructor<SnakeGame> ctor = SnakeGame.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        SnakeGame game = ctor.newInstance();

        // startGame() initializes snake, directions, logicTimer, random.
        // Run it on a daemon thread so it doesn't block.
        Method startGame = SnakeGame.class.getDeclaredMethod("startGame");
        startGame.setAccessible(true);
        Thread t = new Thread(() -> {
            try { startGame.invoke(game); } catch (Exception ignored) {}
        });
        t.setDaemon(true);
        t.start();

        // Wait until snake field is no longer null (startGame has initialized it)
        long deadline = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < deadline) {
            Field f = SnakeGame.class.getDeclaredField("snake");
            f.setAccessible(true);
            if (f.get(game) != null) break;
            Thread.sleep(10);
        }

        // Pause the logic timer so the game loop doesn't tick during tests
        Field timerField = SnakeGame.class.getDeclaredField("logicTimer");
        timerField.setAccessible(true);
        Clock clock = (Clock) timerField.get(game);
        clock.setPaused(true);

        return game;
    }

    // ------------------------------------------------------------------
    //  Method access
    // ------------------------------------------------------------------

    protected static Method getMethod(Object obj, String name) throws Exception {
        Method m = obj.getClass().getDeclaredMethod(name);
        m.setAccessible(true);
        return m;
    }

    // ------------------------------------------------------------------
    //  Field readers
    // ------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    protected static <T> T getField(Object obj, String name, Class<T> type) throws Exception {
        Field f = findField(obj.getClass(), name);
        f.setAccessible(true);
        return type.cast(f.get(obj));
    }

    @SuppressWarnings("unchecked")
    protected static <T> T getFieldRaw(Object obj, String name) throws Exception {
        Field f = findField(obj.getClass(), name);
        f.setAccessible(true);
        return (T) f.get(obj);
    }

    protected static int getInt(Object obj, String name) throws Exception {
        Field f = findField(obj.getClass(), name);
        f.setAccessible(true);
        return f.getInt(obj);
    }

    protected static boolean getBool(Object obj, String name) throws Exception {
        Field f = findField(obj.getClass(), name);
        f.setAccessible(true);
        return f.getBoolean(obj);
    }

    // ------------------------------------------------------------------
    //  Field writers
    // ------------------------------------------------------------------

    protected static void setField(Object obj, String name, Object value) throws Exception {
        Field f = findField(obj.getClass(), name);
        f.setAccessible(true);
        f.set(obj, value);
    }

    protected static void setInt(Object obj, String name, int value) throws Exception {
        Field f = findField(obj.getClass(), name);
        f.setAccessible(true);
        f.setInt(obj, value);
    }

    protected static void setBool(Object obj, String name, boolean value) throws Exception {
        Field f = findField(obj.getClass(), name);
        f.setAccessible(true);
        f.setBoolean(obj, value);
    }

    // ------------------------------------------------------------------
    //  Game-state shortcuts
    // ------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    protected static LinkedList<Direction> getDirections(SnakeGame g) throws Exception {
        return getFieldRaw(g, "directions");
    }

    @SuppressWarnings("unchecked")
    protected static LinkedList<Point> getSnake(SnakeGame g) throws Exception {
        return getFieldRaw(g, "snake");
    }

    protected static BoardPanel getBoard(SnakeGame g) throws Exception {
        return getField(g, "board", BoardPanel.class);
    }

    // ------------------------------------------------------------------
    //  Private: walks up class hierarchy to find a field by name
    // ------------------------------------------------------------------

    private static Field findField(Class<?> cls, String name) throws NoSuchFieldException {
        try {
            return cls.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            Class<?> parent = cls.getSuperclass();
            if (parent != null) return findField(parent, name);
            throw e;
        }
    }
}