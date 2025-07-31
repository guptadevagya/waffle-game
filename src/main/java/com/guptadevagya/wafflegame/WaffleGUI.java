package com.guptadevagya.wafflegame;

import javax.swing.*;
import java.io.FileNotFoundException;

// this class is the main entry point for the application.
// its job is to set up the model and the view, and launch the window.
public class WaffleGUI {

    public static void main(String[] args) {
        final String WAFFLE_FILENAME = "waffles.txt";
        WaffleGame game; // this will hold our game model instance

        // prompt the user to pick a puzzle number
        int puzzleNumber = 0;
        boolean havePuzzleNumber = false;
        do {
            String puzzleNumberStr = JOptionPane.showInputDialog(
                    "Enter a waffle puzzle number from 1 to 10.", 1);
            if (puzzleNumberStr == null) { // user pressed cancel
                return;
            }
            try {
                puzzleNumber = Integer.parseInt(puzzleNumberStr);
                if (puzzleNumber >= 1 && puzzleNumber <= 11) { // puzzles 1 through 11
                    havePuzzleNumber = true;
                }
            } catch (NumberFormatException e) {
                // do nothing, loop will continue
            }
        } while (!havePuzzleNumber);

        // create the game model instance
        // if this fails, show an error and exit
        try {
            // we assume waffles.txt is in the 'src/main/resources' folder
            String filePath = WaffleGUI.class.getClassLoader().getResource(WAFFLE_FILENAME).getPath();
            game = new WaffleGame(filePath, puzzleNumber);
        } catch (FileNotFoundException | IllegalStateException | NullPointerException e) {
            JOptionPane.showMessageDialog(null, "Error loading puzzle: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // create the main window (the jframe)
        JFrame window = new JFrame();
        window.setTitle("Waffle #" + puzzleNumber);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // create the main panel (the view) and pass it the game model
        MainPanel puzzlePanel = new MainPanel(game);
        window.add(puzzlePanel);

        // set the window size and make it visible
        window.setSize(400, 400);
        window.setLocationRelativeTo(null); // center the window on the screen
        window.setVisible(true);
    }
}