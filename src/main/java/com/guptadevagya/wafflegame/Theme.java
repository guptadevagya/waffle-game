package com.guptadevagya.wafflegame;

import java.awt.Color;

final class Theme {
    private Theme() {
    }

    // Light palette
    static final Color L_BG = new Color(0xF5F6F7);
    static final Color L_PANEL = new Color(0xFFFFFF);
    static final Color L_SHADOW = new Color(0xE6E8EA);
    static final Color L_TEXT = new Color(0x121212);

    static final Color L_TILE_FACE = new Color(0xF0F2F5);
    static final Color L_CORRECT = new Color(0x6FB05C);
    static final Color L_PRESENT = new Color(0xE9BA3A);
    static final Color L_ABSENT = new Color(0xD7DADE);
    static final Color L_BLANK = new Color(0xF4F6F8);
    static final Color L_SELECT = new Color(0x3F7EF5);

    // Dark palette (true dark)
    static final Color D_BG = new Color(0x15171A);
    static final Color D_PANEL = new Color(0x1C1F24);
    static final Color D_SHADOW = new Color(0x0F1113);
    static final Color D_TEXT = new Color(0xEDEFF2);

    static final Color D_TILE_FACE = new Color(0x22262B);
    static final Color D_CORRECT = new Color(0x5AA357);
    static final Color D_PRESENT = new Color(0xD6A22F);
    static final Color D_ABSENT = new Color(0x3A3F45);
    static final Color D_BLANK = new Color(0x24282E);
    static final Color D_SELECT = new Color(0x6AA2FF);

    // active palette
    static Color BG, PANEL, SHADOW, TEXT, TILE_FACE, CORRECT, PRESENT, ABSENT, BLANK, SELECT;

    static void setDark(boolean dark) {
        if (dark) {
            BG = D_BG;
            PANEL = D_PANEL;
            SHADOW = D_SHADOW;
            TEXT = D_TEXT;
            TILE_FACE = D_TILE_FACE;
            CORRECT = D_CORRECT;
            PRESENT = D_PRESENT;
            ABSENT = D_ABSENT;
            BLANK = D_BLANK;
            SELECT = D_SELECT;
        } else {
            BG = L_BG;
            PANEL = L_PANEL;
            SHADOW = L_SHADOW;
            TEXT = L_TEXT;
            TILE_FACE = L_TILE_FACE;
            CORRECT = L_CORRECT;
            PRESENT = L_PRESENT;
            ABSENT = L_ABSENT;
            BLANK = L_BLANK;
            SELECT = L_SELECT;
        }
    }

    static {
        setDark(false);
    }
}