package com.guptadevagya.wafflegame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Loads 5-letter words from resources/words.txt and indexes letters at 0,2,4.
 */
final class Dictionary {
    private static final String RESOURCE = "words.txt";

    private Dictionary() {
    }

    /** Lazy holder for immutable data. */
    static final class Holder {
        private Holder() {
        } // hide implicit public ctor (Sonar)

        static final List<String> WORDS = loadWords();
        static final Map<Integer, List<String>> INDEX024 = buildIndex024(WORDS);

        private static List<String> loadWords() {
            try (InputStream in = Objects.requireNonNull(
                    Dictionary.class.getClassLoader().getResourceAsStream(RESOURCE),
                    "Missing " + RESOURCE + " in resources");
                    BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

                List<String> list = br.lines()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(String::toUpperCase)
                        .filter(Dictionary::isFiveLettersAZ)
                        .distinct()
                        .toList();
                if (list.isEmpty()) {
                    throw new IllegalStateException("words.txt contains no valid 5-letter words.");
                }
                return Collections.unmodifiableList(list);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load " + RESOURCE, e);
            }
        }

        private static Map<Integer, List<String>> buildIndex024(List<String> words) {
            @SuppressWarnings("java:S6485") // prefer HashMap.newHashMap(...) (not on Java 17)
            Map<Integer, List<String>> map = new HashMap<>(Math.max(32, words.size() / 2));
            for (String w : words) {
                int k = key(w.charAt(0), w.charAt(2), w.charAt(4));
                map.computeIfAbsent(k, _ -> new ArrayList<>()).add(w);
            }
            map.replaceAll((_, v) -> Collections.unmodifiableList(v));
            return Collections.unmodifiableMap(map);
        }

        private static int key(char a, char b, char c) {
            int au = Character.toUpperCase(a);
            int bu = Character.toUpperCase(b);
            int cu = Character.toUpperCase(c);
            return (au << 16) | (bu << 8) | cu;
        }
    }

    /** All words (uppercased). */
    static List<String> words() {
        return Holder.WORDS;
    }

    /** Return k random distinct words (uppercased). */
    static List<String> randomWords(int k) {
        List<String> src = words();
        int size = src.size();
        int count = clamp(k, 0, size);

        @SuppressWarnings("java:S6485") // prefer HashSet.newHashSet(...) (not on Java 17)
        Set<Integer> used = new HashSet<>(Math.max(16, count * 2));
        ArrayList<String> out = new ArrayList<>(count);
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        while (out.size() < count) {
            int i = rng.nextInt(size);
            if (used.add(i)) {
                out.add(src.get(i));
            }
        }
        return out;
    }

    /** Candidates whose letters at 0,2,4 equal a,b,c (uppercased). */
    static List<String> get024(char a, char b, char c) {
        int key = Holder.key(a, b, c);
        List<String> list = Holder.INDEX024.get(key);
        return (list == null) ? Collections.emptyList() : list;
    }

    // ---- helpers ----

    // Sonar suggests Math.clamp (Java 21+). We keep Java 17 support;
    // replace with Math.clamp when upgrading the toolchain.
    @SuppressWarnings("java:S6885")
    private static int clamp(int v, int min, int max) {
        if (v < min)
            return min;
        if (v > max)
            return max;
        return v;
    }

    private static boolean isFiveLettersAZ(String s) {
        if (s.length() != 5)
            return false;
        for (int i = 0; i < 5; i++) {
            char ch = s.charAt(i);
            if (ch < 'A' || ch > 'Z')
                return false;
        }
        return true;
    }
}