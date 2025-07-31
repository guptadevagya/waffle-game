package com.guptadevagya.wafflegame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// this class contains all the tests for our WaffleGame model.
// it uses the junit 5 testing framework.
class WaffleGameTest {

        @Test
        @DisplayName("Hints should be identified correctly for a general test case")
        void testIdentifyHints_GeneralCase() {
                // set up the puzzle and solution data for this specific test
                char[][] puzzle = {
                                { 'H', 'S', 'A', 'S', 'E' },
                                { 'R', ' ', 'N', ' ', 'L' },
                                { 'D', 'N', 'L', 'G', 'O' },
                                { 'P', ' ', 'I', ' ', 'N' },
                                { 'E', 'C', 'Y', 'D', 'E' }
                };
                char[][] solution = {
                                { 'H', 'A', 'S', 'T', 'E' },
                                { 'R', ' ', 'U', ' ', 'L' },
                                { 'D', 'I', 'N', 'G', 'O' },
                                { 'P', ' ', 'K', ' ', 'N' },
                                { 'E', 'C', 'Y', 'D', 'E' }
                };

                // define what the hints should look like for the puzzle above
                Hint[][] expectedHints = {
                                { Hint.CORRECT, Hint.WRONG_POSITION, Hint.WRONG_POSITION, Hint.NOT_IN_WORD,
                                                Hint.CORRECT },
                                { Hint.CORRECT, Hint.BLANK, Hint.WRONG_POSITION, Hint.BLANK, Hint.CORRECT },
                                { Hint.CORRECT, Hint.WRONG_POSITION, Hint.NOT_IN_WORD, Hint.CORRECT, Hint.CORRECT },
                                { Hint.CORRECT, Hint.BLANK, Hint.NOT_IN_WORD, Hint.BLANK, Hint.CORRECT },
                                { Hint.CORRECT, Hint.CORRECT, Hint.CORRECT, Hint.CORRECT, Hint.CORRECT }
                };

                // create a game instance using our special test constructor
                WaffleGame game = new WaffleGame(puzzle, solution);
                Hint[][] actualHints = new Hint[WaffleGame.WAFFLE_SIZE][WaffleGame.WAFFLE_SIZE];

                // run the method we want to test
                game.identifyHints(actualHints);

                // check if the actual hints match what we expected, row by row
                // assertArrayEquals will give a clear message if the test fails
                for (int i = 0; i < WaffleGame.WAFFLE_SIZE; i++) {
                        assertArrayEquals(expectedHints[i], actualHints[i], "Hint mismatch in row " + i);
                }
        }

        @Test
        @DisplayName("Hints should be identified correctly for puzzle #1")
        void testIdentifyHints_Puzzle1() {
                // set up data from the first puzzle in waffles.txt
                char[][] puzzle = {
                                { 'G', 'A', 'C', 'F', 'T' },
                                { 'P', ' ', 'O', ' ', 'E' },
                                { 'S', 'U', 'T', 'H', 'R' },
                                { 'R', ' ', 'P', ' ', 'O' },
                                { 'E', 'C', 'O', 'D', 'H' }
                };
                char[][] solution = {
                                { 'G', 'H', 'O', 'S', 'T' },
                                { 'R', ' ', 'U', ' ', 'O' },
                                { 'A', 'F', 'T', 'E', 'R' },
                                { 'P', ' ', 'D', ' ', 'C' },
                                { 'E', 'P', 'O', 'C', 'H' }
                };

                // define the expected hints for this puzzle
                Hint[][] expectedHints = {
                                { Hint.CORRECT, Hint.NOT_IN_WORD, Hint.NOT_IN_WORD, Hint.NOT_IN_WORD, Hint.CORRECT },
                                { Hint.WRONG_POSITION, Hint.BLANK, Hint.WRONG_POSITION, Hint.BLANK, Hint.NOT_IN_WORD },
                                { Hint.NOT_IN_WORD, Hint.NOT_IN_WORD, Hint.CORRECT, Hint.NOT_IN_WORD, Hint.CORRECT },
                                { Hint.WRONG_POSITION, Hint.BLANK, Hint.NOT_IN_WORD, Hint.BLANK, Hint.WRONG_POSITION },
                                { Hint.CORRECT, Hint.WRONG_POSITION, Hint.CORRECT, Hint.NOT_IN_WORD, Hint.CORRECT }
                };

                // create a game instance and run the test
                WaffleGame game = new WaffleGame(puzzle, solution);
                Hint[][] actualHints = new Hint[WaffleGame.WAFFLE_SIZE][WaffleGame.WAFFLE_SIZE];
                game.identifyHints(actualHints);

                // assert that the results are correct
                for (int i = 0; i < WaffleGame.WAFFLE_SIZE; i++) {
                        assertArrayEquals(expectedHints[i], actualHints[i], "Hint mismatch in row " + i);
                }
        }
}