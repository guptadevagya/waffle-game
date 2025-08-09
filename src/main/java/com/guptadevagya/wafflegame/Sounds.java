package com.guptadevagya.wafflegame;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

final class Sound {
    private Sound() {
    }

    static void play(String resourcePath) {
        try (InputStream in = Objects.requireNonNull(
                Sound.class.getClassLoader().getResourceAsStream(resourcePath),
                "Missing resource: " + resourcePath);
                AudioInputStream ais = AudioSystem.getAudioInputStream(new java.io.BufferedInputStream(in))) {

            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException
                | NullPointerException ignored) {
            // fail silent if audio unavailable
        }
    }
}