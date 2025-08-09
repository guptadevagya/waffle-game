package com.guptadevagya.wafflegame;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.prefs.Preferences;

final class WelcomePanel extends JPanel {

    private final JFrame window;
    private final JCheckBox darkToggle;

    WelcomePanel(JFrame window) {
        this.window = window;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Theme.BG);

        // --- Hero banner with gradient + title ---
        JPanel hero = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Paint BACKGROUND FIRST (so children draw on top)
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color a = Theme.PANEL;
                Color b = new Color(Theme.PANEL.getRed(), Theme.PANEL.getGreen(), Theme.PANEL.getBlue(), 200);
                g2.setPaint(new GradientPaint(0, 0, a, getWidth(), getHeight(), b));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();

                // NOW let Swing paint children
                super.paintComponent(g);
            }
        };
        hero.setOpaque(false); // we custom-paint the bg
        hero.setLayout(new BorderLayout());
        hero.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel title = new JLabel("Welcome to Waffle");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 30f));
        title.setForeground(Theme.TEXT);

        JLabel subtitle = new JLabel("Arrange the letters to complete all words.");
        subtitle.setForeground(Theme.TEXT);
        subtitle.setBorder(new EmptyBorder(6, 0, 0, 0));

        hero.add(title, BorderLayout.NORTH);
        hero.add(subtitle, BorderLayout.CENTER);

        // --- How to play ---
        JTextArea how = new JTextArea("""
                • Rows 0, 2, 4 and columns 0, 2, 4 each form a five-letter word.
                • Click any two letter tiles to swap them.
                • Green = correct spot; Yellow = wrong spot; Gray = not in that word.
                • You have 20 swaps. Undo (⌘/Ctrl+Z) refunds; Redo (⇧⌘/Ctrl+Z) spends.
                • Shortcuts: R = Random, D = Dark mode, G = Give up (reveal).
                """);
        how.setEditable(false);
        how.setOpaque(false);
        how.setLineWrap(true);
        how.setWrapStyleWord(true);
        how.setFont(new Font("Inter", Font.PLAIN, 14));
        how.setForeground(Theme.TEXT);
        how.setBorder(new EmptyBorder(14, 6, 6, 6));

        JPanel centerCard = new JPanel(new BorderLayout());
        centerCard.setOpaque(true);
        centerCard.setBackground(Theme.PANEL);
        centerCard.setBorder(new EmptyBorder(10, 12, 10, 12));
        centerCard.add(how, BorderLayout.CENTER);

        // --- Bottom controls ---
        boolean darkPref = Preferences.userRoot().node("waffle").getBoolean("dark", false);
        darkToggle = new JCheckBox("Start in dark mode", darkPref);
        darkToggle.setOpaque(false);
        darkToggle.setForeground(Theme.TEXT);
        darkToggle.addActionListener(_ -> applyDark(darkToggle.isSelected()));
        darkToggle.setEnabled(true); // ensure not disabled by accident

        JButton play = new JButton("Let’s play!");
        play.setFont(play.getFont().deriveFont(Font.BOLD, 16f));
        play.setPreferredSize(new Dimension(160, 44));
        play.addActionListener(_ -> startGame());

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(12, 4, 0, 4));
        bottom.add(darkToggle, BorderLayout.WEST);
        bottom.add(play, BorderLayout.EAST);

        add(hero, BorderLayout.NORTH);
        add(centerCard, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private void applyDark(boolean on) {
        Preferences.userRoot().node("waffle").putBoolean("dark", on);
        Theme.setDark(on);
        if (on)
            FlatDarkLaf.setup();
        else
            FlatLightLaf.setup();
        FlatLaf.updateUI();
        setBackground(Theme.BG);
        repaint();
    }

    private void startGame() {
        try {
            boolean on = darkToggle.isSelected();
            Theme.setDark(on);
            if (on)
                FlatDarkLaf.setup();
            else
                FlatLightLaf.setup();
            FlatLaf.updateUI();

            WaffleGame game = WaffleGame.randomFromDatabase();
            MainPanel main = new MainPanel(game);
            window.setContentPane(main);
            window.revalidate();
            window.repaint();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Failed to start game: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}