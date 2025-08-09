package com.guptadevagya.wafflegame;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayDeque;
import java.util.Deque;

class MainPanel extends JPanel {

    private static final Font TILE_FONT = new Font("Inter", Font.BOLD, 28);

    private static final String SND_CLICK = "sounds/click.wav";
    private static final String SND_SWAP = "sounds/swap.wav";
    private static final String SND_DONE = "sounds/complete.wav";

    private final transient WaffleGame game;

    private final Tile[][] tiles = new Tile[WaffleGame.WAFFLE_SIZE][WaffleGame.WAFFLE_SIZE];
    private final Hint[][] hints = new Hint[WaffleGame.WAFFLE_SIZE][WaffleGame.WAFFLE_SIZE];

    private final JLabel swapsLabel = new JLabel("", SwingConstants.LEFT);
    private final JLabel correctLabel = new JLabel("", SwingConstants.RIGHT);
    private final JLabel timerLabel = new JLabel("00:00", SwingConstants.CENTER);

    private javax.swing.Timer timer;
    private boolean darkMode;

    private final Deque<int[]> undo = new ArrayDeque<>();
    private final Deque<int[]> redo = new ArrayDeque<>();

    private Tile selected = null;
    private JToolBar toolbar;

    MainPanel(WaffleGame game) {
        this.game = game;

        // theme pref
        darkMode = java.util.prefs.Preferences.userRoot().node("waffle").getBoolean("dark", false);
        Theme.setDark(darkMode);

        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildGrid(), BorderLayout.CENTER);
        add(buildStatus(), BorderLayout.SOUTH);

        installShortcuts();

        startTimer();
        applyTheme();
        updatePanel();
    }

    private void installShortcuts() {
        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        int meta = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, meta), "undo");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, meta | InputEvent.SHIFT_DOWN_MASK), "redo");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "random");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "dark");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, 0), "giveup");

        am.put("undo", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doUndo();
            }
        });
        am.put("redo", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doRedo();
            }
        });
        am.put("random", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                triggerRandom();
            }
        });
        am.put("dark", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                toggleDark();
            }
        });
        am.put("giveup", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doGiveUp();
            }
        });
    }

    private JToolBar buildToolbar() {
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setOpaque(true);
        toolbar.setBorder(new EmptyBorder(6, 8, 6, 8));

        JButton rndBtn = new JButton("Random");
        rndBtn.addActionListener(_ -> triggerRandom());

        JButton undoBtn = new JButton("Undo");
        undoBtn.addActionListener(_ -> doUndo());

        JButton redoBtn = new JButton("Redo");
        redoBtn.addActionListener(_ -> doRedo());

        JButton giveUpBtn = new JButton("Give up");
        giveUpBtn.addActionListener(_ -> doGiveUp());

        JToggleButton dark = new JToggleButton("Dark", darkMode);
        dark.addActionListener(_ -> toggleDark());

        toolbar.add(rndBtn);
        toolbar.addSeparator();
        toolbar.add(undoBtn);
        toolbar.add(redoBtn);
        toolbar.add(giveUpBtn);
        toolbar.addSeparator();
        toolbar.add(new JLabel("  Time: "));
        toolbar.add(timerLabel);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(dark);
        return toolbar;
    }

    private void applyTheme() {
        setBackground(Theme.BG);
        if (toolbar != null)
            toolbar.setBackground(Theme.PANEL);
        swapsLabel.setForeground(Theme.TEXT);
        correctLabel.setForeground(Theme.TEXT);
        timerLabel.setForeground(Theme.TEXT);
        for (Tile[] row : tiles)
            for (Tile t : row)
                t.refreshTheme();
        repaint();
    }

    private void toggleDark() {
        darkMode = !darkMode;
        Theme.setDark(darkMode);

        if (darkMode)
            FlatDarkLaf.setup();
        else
            FlatLightLaf.setup();
        FlatLaf.updateUI();

        applyTheme();
        java.util.prefs.Preferences.userRoot().node("waffle").putBoolean("dark", darkMode);
    }

    private void triggerRandom() {
        try {
            clearSelection();
            redo.clear();
            undo.clear();
            game.resetToRandom();
            startTimer();
            updatePanel();
        } catch (RuntimeException ex) {
            showError(ex);
        }
    }

    private JPanel buildGrid() {
        JPanel grid = new JPanel(new GridLayout(WaffleGame.WAFFLE_SIZE, WaffleGame.WAFFLE_SIZE, 8, 8));
        grid.setBorder(new EmptyBorder(12, 12, 12, 12));
        grid.setOpaque(false);

        for (int r = 0; r < WaffleGame.WAFFLE_SIZE; r++) {
            for (int c = 0; c < WaffleGame.WAFFLE_SIZE; c++) {
                Tile t = new Tile(r, c);
                tiles[r][c] = t;
                grid.add(t);
            }
        }
        return grid;
    }

    private JPanel buildStatus() {
        JPanel status = new JPanel(new BorderLayout());
        status.setOpaque(false);
        status.setBorder(new EmptyBorder(8, 4, 0, 4));
        swapsLabel.setFont(swapsLabel.getFont().deriveFont(Font.BOLD));
        status.add(swapsLabel, BorderLayout.WEST);
        status.add(correctLabel, BorderLayout.EAST);
        return status;
    }

    private void startTimer() {
        final long startMs = System.currentTimeMillis();
        if (timer != null)
            timer.stop();
        timer = new javax.swing.Timer(1000, _ -> {
            long s = (System.currentTimeMillis() - startMs) / 1000;
            timerLabel.setText(String.format("%02d:%02d", s / 60, s % 60));
        });
        timer.start();
    }

    private void doUndo() {
        if (undo.isEmpty())
            return;
        int[] m = undo.pop(); // r1,c1,r2,c2
        if (game.swapNoCost(m[2], m[3], m[0], m[1])) {
            game.adjustSwaps(+1);
            redo.push(m);
            Sound.play(SND_SWAP);
            updatePanel();
        }
    }

    private void doRedo() {
        if (redo.isEmpty())
            return;
        int[] m = redo.pop();
        if (game.swapNoCost(m[0], m[1], m[2], m[3])) {
            game.adjustSwaps(-1);
            undo.push(m);
            Sound.play(SND_SWAP);
            updatePanel();
        }
    }

    private void doGiveUp() {
        int choice = JOptionPane.showConfirmDialog(
                this, "Reveal the solution and end this game?",
                "Give up", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION)
            return;

        clearSelection();
        undo.clear();
        redo.clear();

        game.revealSolution();
        Sound.play(SND_DONE);
        if (timer != null)
            timer.stop();
        updatePanel();
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void clearSelection() {
        if (selected != null) {
            selected.setSelected(false);
            selected = null;
            repaint();
        }
    }

    // -------------------- update / render --------------------
    void updatePanel() {
        int correct = refreshTilesAndCounts();
        swapsLabel.setText(game.getSwapsRemaining() + " swaps left");
        correctLabel.setText(correct + " / 25 correct");
        handleEndOfGame();
        repaint();
    }

    private int refreshTilesAndCounts() {
        game.identifyHints(hints);
        char[][] g = game.getPuzzleGrid();

        int correct = 0;
        for (int r = 0; r < WaffleGame.WAFFLE_SIZE; r++) {
            for (int c = 0; c < WaffleGame.WAFFLE_SIZE; c++) {
                Tile t = tiles[r][c];
                char letter = g[r][c];
                t.setLetter(letter);
                t.setEnabled(letter != ' ');

                switch (hints[r][c]) {
                    case CORRECT -> {
                        t.animateTo(Theme.CORRECT);
                        correct++;
                    }
                    case WRONG_POSITION -> t.animateTo(Theme.PRESENT);
                    case NOT_IN_WORD -> t.animateTo(Theme.ABSENT);
                    case BLANK -> t.animateTo(Theme.BLANK);
                }
            }
        }
        return correct;
    }

    private void handleEndOfGame() {
        boolean solved = game.isCompleted();
        boolean outOfSwaps = game.getSwapsRemaining() <= 0;
        boolean gaveUp = game.isGaveUp();

        boolean autoReveal = shouldAutoReveal(solved, outOfSwaps, gaveUp);
        if (autoReveal) {
            performAutoReveal();
            solved = true;
            gaveUp = true;
        }

        if (gaveUp || solved || outOfSwaps) {
            disableBoardAndStopTimer();
            JOptionPane.showMessageDialog(this, resolveEndMessage(autoReveal, gaveUp, solved));
        }
    }

    private static boolean shouldAutoReveal(boolean solved, boolean outOfSwaps, boolean gaveUp) {
        return !solved && outOfSwaps && !gaveUp;
    }

    private void performAutoReveal() {
        undo.clear();
        redo.clear();
        game.revealSolution();
        Sound.play(SND_DONE);
        char[][] g = game.getPuzzleGrid();
        for (int r = 0; r < WaffleGame.WAFFLE_SIZE; r++) {
            for (int c = 0; c < WaffleGame.WAFFLE_SIZE; c++) {
                tiles[r][c].setLetter(g[r][c]);
                tiles[r][c].animateTo(Theme.CORRECT);
            }
        }
    }

    private void disableBoardAndStopTimer() {
        for (Tile[] row : tiles) {
            for (Tile t : row)
                t.setEnabled(false);
        }
        if (timer != null)
            timer.stop();
    }

    private static String resolveEndMessage(boolean autoReveal, boolean gaveUp, boolean solved) {
        if (autoReveal)
            return "Out of swaps. Here’s the solution.";
        if (gaveUp)
            return "Solution revealed. Better luck next time!";
        if (solved)
            return "Nice! You solved it.";
        return "No swaps remaining.";
    }

    // -------------------- Tile --------------------
    private class Tile extends JComponent {
        final int r;
        final int c;

        private char letter = ' ';
        private Color current = Theme.ABSENT;
        private Color target = Theme.ABSENT;

        private boolean selectedState = false;
        private boolean hover = false;

        private javax.swing.Timer anim;

        Tile(int r, int c) {
            this.r = r;
            this.c = c;
            setPreferredSize(new Dimension(88, 88));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Click two tiles to swap");

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (!isEnabled() || letter == ' ')
                        return;
                    Sound.play(SND_CLICK);
                    if (selected == null) {
                        selected = Tile.this;
                        setSelected(true);
                    } else if (selected == Tile.this) {
                        setSelected(false);
                        selected = null;
                    } else {
                        Tile other = selected;
                        clearSelection();
                        trySwap(other, Tile.this);
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
            setFocusable(false);
        }

        void refreshTheme() {
            repaint();
        }

        void setLetter(char ch) {
            this.letter = ch;
            repaint();
        }

        void setSelected(boolean on) {
            this.selectedState = on;
            repaint();
        }

        void animateTo(Color to) {
            if (to.equals(target))
                return;
            target = to;
            if (anim != null && anim.isRunning())
                anim.stop();

            final Color from = (current == null) ? Theme.TILE_FACE : current;
            final int ms = 220;
            final int steps = 11;

            anim = new javax.swing.Timer(ms / steps, null);
            anim.addActionListener(new ActionListener() {
                int i = 0;

                @Override
                public void actionPerformed(ActionEvent e) {
                    float t = i++ / (float) steps;
                    // clamp without nested ternary (Sonar-friendly)
                    t = Math.clamp(t, 0f, 1f);
                    current = blend(from, target, t);
                    repaint();
                    if (i > steps)
                        anim.stop();
                }
            });
            anim.start();
        }

        private void trySwap(Tile a, Tile b) {
            if (a == null || b == null || a == b)
                return;
            redo.clear();
            if (game.swap(a.r, a.c, b.r, b.c)) { // consumes one
                undo.push(new int[] { a.r, a.c, b.r, b.c });
                Sound.play(SND_SWAP);
                updatePanel();
            }
        }

        private Color blend(Color a, Color b, float t) {
            int rr = (int) (a.getRed() + t * (b.getRed() - a.getRed()));
            int gg = (int) (a.getGreen() + t * (b.getGreen() - a.getGreen()));
            int bb = (int) (a.getBlue() + t * (b.getBlue() - a.getBlue()));
            return new Color(rr, gg, bb);
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            setOpaque(enabled);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 18;

            // shadow
            g2.setColor(Theme.SHADOW);
            g2.fillRoundRect(3, 6, w - 6, h - 9, arc, arc);

            // face (no nested ternaries)
            Color face;
            if (letter == ' ')
                face = Theme.BLANK;
            else if (current != null)
                face = current;
            else
                face = Theme.TILE_FACE;

            if (hover && isEnabled() && letter != ' ') {
                face = blend(face, Color.WHITE, 0.06f);
            }

            g2.setColor(face);
            g2.fillRoundRect(3, 3, w - 6, h - 9, arc, arc);

            if (selectedState) {
                g2.setStroke(new BasicStroke(3.5f));
                g2.setColor(Theme.SELECT);
                g2.drawRoundRect(3, 3, w - 6, h - 9, arc, arc);
            }

            if (letter != ' ') {
                g2.setColor(Theme.TEXT);
                g2.setFont(TILE_FONT);
                FontMetrics fm = g2.getFontMetrics();
                String s = String.valueOf(letter);
                int x = (w - fm.stringWidth(s)) / 2;
                int y = (h + fm.getAscent() - fm.getDescent()) / 2 - 2;
                g2.drawString(s, x, y);
            }
            g2.dispose();
        }
    }
}