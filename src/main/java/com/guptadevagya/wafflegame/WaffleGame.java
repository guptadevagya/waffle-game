package com.guptadevagya.wafflegame;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class WaffleGame {

    public static final int WAFFLE_SIZE = 5;
    public static final int MAX_SWAPS = 20;

    private static final Random RNG = new Random();

    private char[][] puzzle;
    private char[][] solution;
    private int swapsRemaining;
    private boolean gaveUp = false;

    public WaffleGame(String filename, int puzzleNumber) throws FileNotFoundException {
        this.puzzle = new char[WAFFLE_SIZE][WAFFLE_SIZE];
        this.solution = new char[WAFFLE_SIZE][WAFFLE_SIZE];
        this.swapsRemaining = MAX_SWAPS;
        loadPuzzleFromFile(filename, puzzleNumber);
    }

    WaffleGame(char[][] puzzle, char[][] solution) {
        this.puzzle = puzzle;
        this.solution = solution;
        this.swapsRemaining = MAX_SWAPS;
    }

    public static WaffleGame randomFromDatabase() {
        WaffleGame g = new WaffleGame(new char[WAFFLE_SIZE][WAFFLE_SIZE], new char[WAFFLE_SIZE][WAFFLE_SIZE]);
        g.resetToRandom();
        return g;
    }

    public void resetToRandom() {
        this.swapsRemaining = MAX_SWAPS;
        this.gaveUp = false;
        buildRandomSolutionFromDictionary();
        scramblePuzzleFromSolution(60);
    }

    public void resetToFile(String filename, int puzzleNumber) throws FileNotFoundException {
        this.swapsRemaining = MAX_SWAPS;
        this.gaveUp = false;
        loadPuzzleFromFile(filename, puzzleNumber);
    }

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

    private boolean findPuzzleInFile(Scanner input, int puzzleNumber) {
        while (input.hasNextLine()) {
            if (input.hasNextInt() && input.nextInt() == puzzleNumber) {
                input.nextLine();
                return true;
            }
            input.nextLine();
        }
        return false;
    }

    private void loadGrid(Scanner input, char[][] grid) {
        for (int i = 0; i < WAFFLE_SIZE; i++) {
            String line = input.nextLine();
            for (int j = 0; j < WAFFLE_SIZE; j++) {
                grid[i][j] = line.charAt(j);
            }
        }
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < WAFFLE_SIZE && c >= 0 && c < WAFFLE_SIZE;
    }

    public boolean swap(int r1, int c1, int r2, int c2) {
        if (!inBounds(r1, c1) || !inBounds(r2, c2))
            return false;
        char tmp = puzzle[r1][c1];
        puzzle[r1][c1] = puzzle[r2][c2];
        puzzle[r2][c2] = tmp;
        swapsRemaining = Math.max(0, swapsRemaining - 1);
        return true;
    }

    public boolean swapNoCost(int r1, int c1, int r2, int c2) {
        if (!inBounds(r1, c1) || !inBounds(r2, c2))
            return false;
        char tmp = puzzle[r1][c1];
        puzzle[r1][c1] = puzzle[r2][c2];
        puzzle[r2][c2] = tmp;
        return true;
    }

    public void adjustSwaps(int delta) {
        swapsRemaining = Math.max(0, swapsRemaining + delta);
    }

    public boolean isCompleted() {
        for (int r = 0; r < WAFFLE_SIZE; r++)
            for (int c = 0; c < WAFFLE_SIZE; c++)
                if (puzzle[r][c] != solution[r][c])
                    return false;
        return true;
    }

    public void identifyHints(Hint[][] hints) {
        for (int r = 0; r < WAFFLE_SIZE; r++) {
            for (int c = 0; c < WAFFLE_SIZE; c++) {
                if (solution[r][c] == ' ')
                    hints[r][c] = Hint.BLANK;
                else if (puzzle[r][c] == solution[r][c])
                    hints[r][c] = Hint.CORRECT;
                else
                    hints[r][c] = Hint.NOT_IN_WORD;
            }
        }
        for (int row = 0; row < WAFFLE_SIZE; row += 2)
            checkWordForWrongPosition(hints, row, true);
        for (int col = 0; col < WAFFLE_SIZE; col += 2)
            checkWordForWrongPosition(hints, col, false);
    }

    private void checkWordForWrongPosition(Hint[][] hints, int lineIndex, boolean isHorizontal) {
        ArrayList<Character> unmatched = getUnmatchedSolutionLetters(hints, lineIndex, isHorizontal);
        markWrongPositionLetters(hints, lineIndex, isHorizontal, unmatched);
    }

    private ArrayList<Character> getUnmatchedSolutionLetters(Hint[][] hints, int lineIndex, boolean isHorizontal) {
        ArrayList<Character> res = new ArrayList<>();
        for (int i = 0; i < WAFFLE_SIZE; i++) {
            int r = isHorizontal ? lineIndex : i;
            int c = isHorizontal ? i : lineIndex;
            if (hints[r][c] != Hint.CORRECT)
                res.add(solution[r][c]);
        }
        return res;
    }

    private void markWrongPositionLetters(Hint[][] hints, int lineIndex, boolean isHorizontal,
            ArrayList<Character> unmatched) {
        for (int i = 0; i < WAFFLE_SIZE; i++) {
            int r = isHorizontal ? lineIndex : i;
            int c = isHorizontal ? i : lineIndex;
            if (hints[r][c] != Hint.CORRECT && unmatched.contains(puzzle[r][c])) {
                hints[r][c] = Hint.WRONG_POSITION;
                unmatched.remove(Character.valueOf(puzzle[r][c]));
            }
        }
    }

    public void revealSolution() {
        for (int r = 0; r < WAFFLE_SIZE; r++) {
            System.arraycopy(solution[r], 0, puzzle[r], 0, WAFFLE_SIZE);
        }
        gaveUp = true;
    }

    public boolean isGaveUp() {
        return gaveUp;
    }

    public char[][] getPuzzleGrid() {
        return puzzle;
    }

    public int getSwapsRemaining() {
        return swapsRemaining;
    }

    public char[][] getSolutionGrid() {
        return solution;
    }

    // ----- random board generation -----
    private void buildRandomSolutionFromDictionary() {
        final int MAX_TRIES = 8000;

        for (int attempt = 0; attempt < MAX_TRIES; attempt++) {
            java.util.List<String> rows = Dictionary.randomWords(3);
            String r0 = rows.get(0);
            String r1 = rows.get(1);
            String r2 = rows.get(2);

            java.util.List<String> c0s = Dictionary.get024(r0.charAt(0), r1.charAt(0), r2.charAt(0));
            java.util.List<String> c2s = Dictionary.get024(r0.charAt(2), r1.charAt(2), r2.charAt(2));
            java.util.List<String> c4s = Dictionary.get024(r0.charAt(4), r1.charAt(4), r2.charAt(4));
            if (c0s.isEmpty() || c2s.isEmpty() || c4s.isEmpty())
                continue;

            String c0 = c0s.get(RNG.nextInt(c0s.size()));
            String c2 = c2s.get(RNG.nextInt(c2s.size()));
            String c4 = c4s.get(RNG.nextInt(c4s.size()));

            char[][] sol = initSolutionMask();
            placeRowWord(sol, 0, r0);
            placeRowWord(sol, 2, r1);
            placeRowWord(sol, 4, r2);
            placeOddRowFromColumns(sol, 1, c0, c2, c4);
            placeOddRowFromColumns(sol, 3, c0, c2, c4);

            this.solution = sol;
            this.puzzle = copy(sol);
            return;
        }
        throw new IllegalStateException("Could not generate a board from words.txt.");
    }

    private static char[][] initSolutionMask() {
        char[][] m = new char[WAFFLE_SIZE][WAFFLE_SIZE];
        for (int r = 0; r < WAFFLE_SIZE; r++) {
            for (int c = 0; c < WAFFLE_SIZE; c++) {
                m[r][c] = ((r % 2 == 1) || (c % 2 == 1)) ? ' ' : '?';
            }
        }
        return m;
    }

    private static void placeRowWord(char[][] grid, int row, String w) {
        for (int col = 0; col < WAFFLE_SIZE; col++)
            grid[row][col] = w.charAt(col);
    }

    private static void placeOddRowFromColumns(char[][] grid, int row, String c0, String c2, String c4) {
        grid[row][0] = c0.charAt(row);
        grid[row][2] = c2.charAt(row);
        grid[row][4] = c4.charAt(row);
    }

    private void scramblePuzzleFromSolution(int swaps) {
        ArrayList<int[]> cells = new ArrayList<>();
        for (int r = 0; r < WAFFLE_SIZE; r++) {
            for (int c = 0; c < WAFFLE_SIZE; c++) {
                if (solution[r][c] != ' ')
                    cells.add(new int[] { r, c });
            }
        }

        Collections.shuffle(cells, RNG);
        this.puzzle = copy(this.solution);

        int n = 0;
        while (n < swaps) {
            int[] a = cells.get(RNG.nextInt(cells.size()));
            int[] b = cells.get(RNG.nextInt(cells.size()));
            if (a[0] == b[0] && a[1] == b[1])
                continue;
            char tmp = puzzle[a[0]][a[1]];
            puzzle[a[0]][a[1]] = puzzle[b[0]][b[1]];
            puzzle[b[0]][b[1]] = tmp;
            n++;
        }
        if (isCompleted())
            scramblePuzzleFromSolution(swaps);
    }

    private static char[][] copy(char[][] src) {
        char[][] dst = new char[src.length][src[0].length];
        for (int i = 0; i < src.length; i++)
            System.arraycopy(src[i], 0, dst[i], 0, src[i].length);
        return dst;
    }
}