package com.guptadevagya.wafflegame;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

public class WaffleGUI {

    public static void main(String[] args) {
        // crisp text & scaling
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        System.setProperty("flatlaf.uiScale", "1.0");
        // mac niceties (safe elsewhere)
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.appearance", "system");

        boolean dark = java.util.prefs.Preferences.userRoot().node("waffle").getBoolean("dark", false);
        if (dark)
            FlatDarkLaf.setup();
        else
            FlatLightLaf.setup();
        Theme.setDark(dark);

        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Waffle");
            window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            // splash / welcome first
            WelcomePanel welcome = new WelcomePanel(window);
            window.setContentPane(welcome);

            window.setSize(560, 600);
            window.setLocationRelativeTo(null);
            window.setVisible(true);

            FlatLaf.updateUI();
        });
    }
}