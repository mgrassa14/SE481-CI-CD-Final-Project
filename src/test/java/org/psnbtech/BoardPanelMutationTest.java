package org.psnbtech;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BoardPanelMutationTest {
    @Mock
    private SnakeGame mockGame;

    private BoardPanel boardPanel;

    @Before
    public void setup(){
        boardPanel=new BoardPanel(mockGame);
        boardPanel.clearBoard();
    }

    /*Okay, so the reason I did mutation testing was that I could nt
    catch the bug with the current 25X25 board no matter what inputs I use,
     because COL_COUNT == ROW_COUNT == 25 makes both formulas produce identical results.
     since COL_COUNT and ROW_COUNT is static and final, one cannot change it at runtime.

     ISP cannot catch this bug. even if we partition x and y into every possible
     combination, every single test passes because the formula give the same answer on a square board.

     So, by mutation testing, we simulate the formula but make the rows not equal to columns
     therefore, the results are different. and the tests fails.
     */
    @Test
    public void Mutant_1() {
        //number of rows in not equal to number of columns
        int columns = 30;
        int rows = 25;
        int x = 5;
        int y = 3;

        int original_code   = y * rows + x; // 3*25+5 = 80 // actual
        int mutant_code = y * columns + x; // 3*30+5 = 95 //Expected

        System.out.println("On non-square board (COL=30, ROW=25):");
        System.out.println("original_code: " + original_code);
        System.out.println("mutant_code: " + mutant_code);

        //this assertion should fail: proving the formulas diverge on non-square boards
        // This is the mutation test that kills M1
        assertEquals(
                "M1 Killed: ROW_COUNT and COL_COUNT produce different result on square board. " +
                        " On non-square board (COL=30,ROW=25), " +
                        "original =" + original_code + " correct index=" + mutant_code,
                original_code, mutant_code
        );
    }

    //verifies that  current board is square
    //If this test FAILS it means someone changed board dimensions
    //and the real bug in setTile/getTile would now be visible
    @Test
    public void boardIsCurrentlySquare() {
        System.out.println("COL_COUNT = " + BoardPanel.COL_COUNT);
        System.out.println("ROW_COUNT = " + BoardPanel.ROW_COUNT);

        int col = BoardPanel.COL_COUNT;
        int row = BoardPanel.ROW_COUNT;

        System.out.println("COL==ROW: " + (col == row));
        System.out.println("Bug status: " + (col == row ? "HIDDEN" : "VISIBLE AND BREAKING"));

        //If this assertEquals PASSES, the bug is hidden
        //If this assertEquals FAILS, the bug is now breaking the game
        assertEquals("Board is non-square — setTile/getTile index formula bug is now ACTIVE. ", col, row);
    }

}
