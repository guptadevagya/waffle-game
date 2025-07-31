package com.guptadevagya.wafflegame;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

// this class holds all the data and logic for a single waffle game.
// it doesn't know anything about the user interface.
// this is the 'model' in our mvc pattern.
public class WaffleGame {

    // constants for the game board
    public static final int WAFFLE_SIZE = 5;
    public static final int MAX_SWAPS = 15;

    // stores the current state of the puzzle grid
    private char[][] puzzle;

    // stores the solution grid
    private char[][] solution;

    // tracks how many swaps the player has left
    private int swapsRemaining;

    // constructor to create a new waffle game instance from a file.
    public WaffleGame(String filename, int puzzleNumber) throws FileNotFoundException {
        // initialize the game state
        this.puzzle = new char[WAFFLE_SIZE][WAFFLE_SIZE];
        this.solution = new char[WAFFLE_SIZE][WAFFLE_SIZE];
        this.swapsRemaining = MAX_SWAPS;

        // load the puzzle from the file
        loadPuzzleFromFile(filename, puzzleNumber);
    }

    // constructor for testing purposes only
    // it lets us create a game with specific puzzle and solution arrays
    WaffleGame(char[][] puzzle, char[][] solution) {
        this.puzzle = puzzle;
        this.solution = solution;
        this.swapsRemaining = MAX_SWAPS;
    }

    // loads a puzzle from the specified file.
    private void loadPuzzleFromFile(String filename, int puzzleNumber) throws FileNotFoundException {
        File file = new File(filename);

        try (Scanner input = new Scanner(file)) {
            if (findPuzzleInFile(input, puzzleNumber)) {
                loadGrid(input, this.puzzle);
                loadGrid(input, this.solution);
            } else {
                throw new IllegalStateException("puzzle number " + puzzleNumber + " not found in file.");
            }
        }
    }

    // searches for the specified puzzle number in the file.
    private boolean findPuzzleInFile(Scanner input, int puzzleNumber) {
        while (input.hasNextLine()) {
            if (input.hasNextInt() && input.nextInt() == puzzleNumber) {
                input.nextLine(); // consume the rest of the line
                return true;
            }
            input.nextLine(); // skip lines that aren't the one we're looking for
        }
        return false;
    }

    // loads a single grid (5x5) from the scanner.
    private void loadGrid(Scanner input, char[][] grid) {
        for (int i = 0; i < WAFFLE_SIZE; i++) {
            String line = input.nextLine();
            for (int j = 0; j < WAFFLE_SIZE; j++) {
                grid[i][j] = line.charAt(j);
            }
        }
    }

    // swaps two letters on the puzzle board.
    public boolean swap(int row1, int col1, int row2, int col2) {
        // check if coordinates are valid
        if (row1 < 0 || row1 >= WAFFLE_SIZE || col1 < 0 || col1 >= WAFFLE_SIZE ||
                row2 < 0 || row2 >= WAFFLE_SIZE || col2 < 0 || col2 >= WAFFLE_SIZE) {
            return false;
        }

        // perform the swap
        char temp = puzzle[row1][col1];
        puzzle[row1][col1] = puzzle[row2][col2];
        puzzle[row2][col2] = temp;

        this.swapsRemaining--;
        return true;
    }

    // checks if the current puzzle grid matches the solution grid.
    public boolean isCompleted() {
        for (int i = 0; i < WAFFLE_SIZE; i++) {
            for (int j = 0; j < WAFFLE_SIZE; j++) {
                if (puzzle[i][j] != solution[i][j]) {
                    return false; // found a letter that doesn't match
                }
            }
        }
        return true; // all letters match
    }

    // fills a 2d array with hints based on the current puzzle state.
    public void identifyHints(Hint[][] hints) {
        // first pass: find all correct letters and blank spaces
        for (int row = 0; row < WAFFLE_SIZE; row++) {
            for (int col = 0; col < WAFFLE_SIZE; col++) {
                if (solution[row][col] == ' ') {
                    hints[row][col] = Hint.BLANK;
                } else if (puzzle[row][col] == solution[row][col]) {
                    hints[row][col] = Hint.CORRECT;
                } else {
                    hints[row][col] = Hint.NOT_IN_WORD;
                }
            }
        }

        // check horizontal and vertical words for letters in the wrong position
        // by checking horizontal words (rows)
        for (int row = 0; row < WAFFLE_SIZE; row += 2) {
            checkWordForWrongPosition(hints, row, true);
        }
        // by checking vertical words (columns)
        for (int col = 0; col < WAFFLE_SIZE; col += 2) {
            checkWordForWrongPosition(hints, col, false);
        }
    }

    // checks a single word (a row or column) for letters in the wrong position.
    private void checkWordForWrongPosition(Hint[][] hints, int lineIndex, boolean isHorizontal) {
        ArrayList<Character> unmatchedSolutionLetters = getUnmatchedSolutionLetters(hints, lineIndex, isHorizontal);
        markWrongPositionLetters(hints, lineIndex, isHorizontal, unmatchedSolutionLetters);
    }

    // collects all unmatched solution letters for a word
    private ArrayList<Character> getUnmatchedSolutionLetters(Hint[][] hints, int lineIndex, boolean isHorizontal) {
        ArrayList<Character> unmatchedSolutionLetters = new ArrayList<>();
        for (int i = 0; i < WAFFLE_SIZE; i++) {
            int row = isHorizontal ? lineIndex : i;
            int col = isHorizontal ? i : lineIndex;
            if (hints[row][col] != Hint.CORRECT) {
                unmatchedSolutionLetters.add(solution[row][col]);
            }
        }
        return unmatchedSolutionLetters;
    }

    // marks letters that are in the wrong position within a word
    private void markWrongPositionLetters(Hint[][] hints, int lineIndex, boolean isHorizontal,
            ArrayList<Character> unmatchedSolutionLetters) {
        for (int i = 0; i < WAFFLE_SIZE; i++) {
            int row = isHorizontal ? lineIndex : i;
            int col = isHorizontal ? i : lineIndex;
            if (hints[row][col] != Hint.CORRECT && unmatchedSolutionLetters.contains(puzzle[row][col])) {
                hints[row][col] = Hint.WRONG_POSITION;
                unmatchedSolutionLetters.remove(Character.valueOf(puzzle[row][col]));
            }
        }
    }

    // getter methods for the ui to access game data
    public char[][] getPuzzleGrid() {
        return this.puzzle;
    }

    public int getSwapsRemaining() {
        return this.swapsRemaining;
    }
}