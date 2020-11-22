package edu.kpi.ip71.dovhopoliuk.substitution.worker;

import edu.kpi.ip71.dovhopoliuk.utils.SecurityUtils;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.abs;

public class SubstitutionWorker implements Callable<String> {
    private String text;

    public SubstitutionWorker(String text) {
        this.text = text;
    }

    @Override
    public String call() {
        //TODO Not correct working algorithm
        System.out.println(decrypt("SDXS SN DMBQYOS", "TABDCEFGHIJKLMNOPQRSUVWXYZ"));
        System.out.println(encrypt("TEXT TO ENCRYPT", "TABCDEFGHIJKLMNOPQRSUVWXYZ"));
        return "";
    }

    //TODO Replace duplicate
    private String decrypt(String text, String key) {
        List<Character> alphabetList = getEnglishAlphabetStream()
                .map(Character::toUpperCase)
                .collect(Collectors.toList());
        List<Character> keyList = Arrays.stream(key.split(""))
                .map(st -> st.charAt(0))
                .collect(Collectors.toList());

        if (keyList.size() != alphabetList.size()) {
            throw new IllegalArgumentException("Illegal key");
        }

        String result = text;
        for (int i = 0; i < alphabetList.size(); i++) {
            Character alphabetLetter = alphabetList.get(i);
            Character keyLetter = keyList.get(i);

            result = result.replace(keyLetter, alphabetLetter);
        }
        return result;
    }

    //TODO Replace duplicate
    private String encrypt(String text, String key) {
        List<Character> alphabetList = getEnglishAlphabetStream()
                .map(Character::toUpperCase)
                .collect(Collectors.toList());
        List<Character> keyList = Arrays.stream(key.split(""))
                .map(st -> st.charAt(0))
                .collect(Collectors.toList());

        if (keyList.size() != alphabetList.size()) {
            throw new IllegalArgumentException("Illegal key");
        }

        String result = text;
        for (int i = 0; i < alphabetList.size(); i++) {
            Character alphabetLetter = alphabetList.get(i);
            Character keyLetter = keyList.get(i);


            result = result.replace(alphabetLetter, keyLetter);
        }
        return result;
    }

    private double getFitness(String encryptedText, String key) {
        int textLength = encryptedText.length();
        String decryptedText = decrypt(encryptedText, key);

        List<Character> decryptedTextChars = decryptedText.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());

        final Map<Character, Integer> textCharactersOccurrences = decryptedTextChars
                .stream()
                .distinct()
                .map(character -> Map.entry(character, Collections.frequency(decryptedTextChars, character)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (existing, replacement) -> existing, TreeMap::new));

        double minExpectedOccurrences = getMinExpectedOccurrenceForTextLength(textLength);

        double sum = getEnglishAlphabetStream()
                .map(Character::toUpperCase)
                .mapToDouble(letter -> {
                    double expectedOccurrences = calculateExpectedOccurrences(textLength, letter);
                    double actualOccurrences = textCharactersOccurrences.get(letter);

                    return abs(expectedOccurrences - actualOccurrences);
                }).sum();

        return ((2 * (textLength - minExpectedOccurrences)) - sum) / (2 * (textLength - minExpectedOccurrences));
    }

    private Stream<Character> getEnglishAlphabetStream() {
        return Arrays.stream(SecurityUtils.ENGLISH_ALPHABET.split(""))
                .map(st -> st.charAt(0));
    }

    private double getMinExpectedOccurrenceForTextLength(int textLength) {
        return getEnglishAlphabetStream()
                .map(letter -> calculateExpectedOccurrences(textLength, letter))
                .mapToDouble(Double::valueOf)
                .min().orElseThrow();
    }

    private double calculateExpectedOccurrences(int textLength, Character letter) {
        double frequencyForLetter = SecurityUtils.ENGLISH_LETTERS_FREQUENCY.get(Character.toLowerCase(letter));
        return textLength * frequencyForLetter;
    }
}
