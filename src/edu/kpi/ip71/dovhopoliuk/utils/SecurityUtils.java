package edu.kpi.ip71.dovhopoliuk.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class SecurityUtils {
    public static final String ENGLISH_ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    public static final Map<Character, Double> ENGLISH_LETTERS_FREQUENCY = Map.ofEntries(
            Map.entry('a', 0.082),
            Map.entry('b', 0.015),
            Map.entry('c', 0.028),
            Map.entry('d', 0.043),
            Map.entry('e', 0.13),
            Map.entry('f', 0.022),
            Map.entry('g', 0.02),
            Map.entry('h', 0.061),
            Map.entry('i', 0.07),
            Map.entry('j', 0.0015),
            Map.entry('k', 0.0077),
            Map.entry('l', 0.04),
            Map.entry('m', 0.024),
            Map.entry('n', 0.067),
            Map.entry('o', 0.075),
            Map.entry('p', 0.019),
            Map.entry('q', 0.00095),
            Map.entry('r', 0.06),
            Map.entry('s', 0.063),
            Map.entry('t', 0.091),
            Map.entry('u', 0.028),
            Map.entry('v', 0.0098),
            Map.entry('w', 0.024),
            Map.entry('x', 0.0015),
            Map.entry('y', 0.02),
            Map.entry('z', 0.00074)
    );

    public static final Map<String, Double> FOURGRAMS = getFourgrams();

    private static final String FOURGRAMS_FILENAME = "resources/substitution/fourgrams.txt";

    private static Map<String, Double> getFourgrams() {
        Map<String, Double> fourgrams = new HashMap<>();
        try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(FOURGRAMS_FILENAME))) {
            Files.lines(Path.of(FOURGRAMS_FILENAME))
                    .forEach(s -> {
                        String tetagram = s.substring(0, 4);
                        double frequency = Double.parseDouble(s.substring(5));
                        fourgrams.put(tetagram, frequency);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fourgrams;
    }
}
