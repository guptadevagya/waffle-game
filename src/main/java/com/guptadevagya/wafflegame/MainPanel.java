package com.guptadevagya.wafflegame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// this class is the main panel for the game ui.
// it holds all the buttons and labels.
// this is the 'view' in our mvc pattern.
class MainPanel extends JPanel implements ActionListener {

    // ui colors and fonts
    private static final Color CORRECT_COLOR = new Color(111, 176, 92);
    private static final Color POSITION_WRONG_COLOR = new Color(233, 186, 58);
    private static final Color NOT_IN_WORD_COLOR = new Color(237, 239, 241);
    private static final Font LETTER_FONT = new Font("Arial", Font.BOLD, 25);

    // a reference to the game model, which holds all the logic
    private transient WaffleGame game;

    // ui-specific arrays for the buttons and their hints
    private JToggleButton[][] buttons;
    private Hint[][] hints;

    // keeps track of the first button clicked for a swap
    private JToggleButton firstButton = null;

    // a label to show the player how many swaps they have left
    private JLabel swapsRemainingLabel;

    // constructor to create the main panel.
    // it takes a game model object and builds the ui based on its state.
    public MainPanel(WaffleGame game) {
        this.game = game;

        // initialize ui-specific arrays based on the model's size
        this.buttons = new JToggleButton[WaffleGame.WAFFLE_SIZE][WaffleGame.WAFFLE_SIZE];
        this.hints = new Hint[WaffleGame.WAFFLE_SIZE][WaffleGame.WAFFLE_SIZE];

        // use a borderlayout to position the grid in the center and a label at the
        // bottom
        this.setLayout(new BorderLayout());

        // create a sub-panel with a gridlayout for the waffle buttons
        JPanel wafflePanel = new JPanel(new GridLayout(WaffleGame.WAFFLE_SIZE, WaffleGame.WAFFLE_SIZE));

        for (int row = 0; row < WaffleGame.WAFFLE_SIZE; row++) {
            for (int col = 0; col < WaffleGame.WAFFLE_SIZE; col++) {
                JToggleButton button = new JToggleButton();
                button.setFont(LETTER_FONT);
                button.setOpaque(true);
                button.addActionListener(this);
                buttons[row][col] = button;
                wafflePanel.add(button);
            }
        }
        this.add(wafflePanel, BorderLayout.CENTER);

        // set up the label for swaps remaining
        swapsRemainingLabel = new JLabel();
        swapsRemainingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(swapsRemainingLabel, BorderLayout.SOUTH);

        // do an initial update to draw the board for the first time
        updatePanel();
    }

    // this is called to redraw the entire panel.
    // it gets the latest data from the game model and updates the ui.
    public void updatePanel() {
        updateButtonsAndHints();
        handleGameEndState();
        this.repaint(); // tells swing to redraw the component
    }

    // updates all buttons with their current text and color based on hints
    private void updateButtonsAndHints() {
        // ask the model to calculate the latest hints
        game.identifyHints(this.hints);
        char[][] puzzleGrid = game.getPuzzleGrid();

        for (int row = 0; row < WaffleGame.WAFFLE_SIZE; row++) {
            for (int col = 0; col < WaffleGame.WAFFLE_SIZE; col++) {
                JToggleButton button = buttons[row][col];
                button.setText(String.valueOf(puzzleGrid[row][col]));

                switch (hints[row][col]) {
                    case CORRECT:
                        button.setBackground(CORRECT_COLOR);
                        break;
                    case WRONG_POSITION:
                        button.setBackground(POSITION_WRONG_COLOR);
                        break;
                    case NOT_IN_WORD:
                        button.setBackground(NOT_IN_WORD_COLOR);
                        break;
                    case BLANK:
                        // blank spaces can't be clicked
                        button.setEnabled(false);
                        break;
                }
            }
        }
        // update the swaps remaining text from the model
        swapsRemainingLabel.setText(game.getSwapsRemaining() + " SWAPS REMAINING");
    }

    // handles end game state by disabling buttons and showing messages
    private void handleGameEndState() {
        if (game.isCompleted() || game.getSwapsRemaining() <= 0) {
            // disable all buttons if the game is over
            for (JToggleButton[] buttonRow : buttons) {
                for (JToggleButton button : buttonRow) {
                    button.setEnabled(false);
                }
            }
            // show the correct end-game message
            String message = game.isCompleted() ? "Congratulations!" : "Sorry - no swaps remaining.";
            JOptionPane.showMessageDialog(this, message);
        }
    }

    // this method handles all button clicks on the grid.
    @Override
    public void actionPerformed(ActionEvent event) {
        JToggleButton currentButton = (JToggleButton) event.getSource();

        if (firstButton == null) {
            selectFirstButton(currentButton);
        } else if (firstButton == currentButton) {
            deselectFirstButton();
        } else {
            performSwap(currentButton);
        }
    }

    // selects the first button for a swap
    private void selectFirstButton(JToggleButton button) {
        firstButton = button;
        firstButton.setSelected(true);
    }

    // deselects the first button if clicked again
    private void deselectFirstButton() {
        if (firstButton != null) {
            firstButton.setSelected(false);
            firstButton = null;
        }
    }

    // performs the swap between first button and current button
    private void performSwap(JToggleButton currentButton) {
        int[] firstCoords = findButtonCoordinates(firstButton);
        int[] currentCoords = findButtonCoordinates(currentButton);

        // ask the model to perform the swap
        if (firstCoords != null && currentCoords != null
                && game.swap(firstCoords[0], firstCoords[1], currentCoords[0], currentCoords[1])) {
            updatePanel();
        }

        // reset selection state for the next turn
        deselectFirstButton();
    }

    // finds the coordinates of a button in the grid
    private int[] findButtonCoordinates(JToggleButton button) {
        for (int row = 0; row < WaffleGame.WAFFLE_SIZE; row++) {
            for (int col = 0; col < WaffleGame.WAFFLE_SIZE; col++) {
                if (buttons[row][col] == button) {
                    return new int[] { row, col };
                }
            }
        }
        return null; // should not happen if button is on grid
    }
}