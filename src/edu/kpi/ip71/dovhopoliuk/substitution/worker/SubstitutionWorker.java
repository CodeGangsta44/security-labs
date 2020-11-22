package edu.kpi.ip71.dovhopoliuk.substitution.worker;

import edu.kpi.ip71.dovhopoliuk.substitution.model.Individual;
import edu.kpi.ip71.dovhopoliuk.utils.SecurityUtils;

import java.util.*;
import java.util.concurrent.Callable;
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

        System.out.println(decrypt("SDXSSNDMBQYOS", "TABCDEFGHIJKLMNOPQRSUVWXYZ"));
        System.out.println(encrypt("TEXTTOENCRYPT", "TABCDEFGHIJKLMNOPQRSUVWXYZ"));

        System.out.println(getFitness("SDXSSNDMBQYOS", "TABCDEFGHIJKLMNOPQRSUVWXYZ"));

        System.out.println(generateInitialIndividuals(15, text));

        return "";
    }

    private List<Individual> generateInitialIndividuals(final int sizeOfPopulation, final String encryptedText) {

        final Set<List<Character>> generatedKeys = new HashSet<>();

        List<Character> alpha = getEnglishAlphabetStream().map(Character::toUpperCase).collect(Collectors.toList());

        while (generatedKeys.size() < sizeOfPopulation) {

            Collections.shuffle(alpha);
            generatedKeys.add(new ArrayList<>(alpha));
        }

        return generatedKeys.stream()
                .map(characters -> {
                    String ketStr = characters.stream().map(String::valueOf).collect(Collectors.joining());
                    return new Individual(characters, getFitness(encryptedText, ketStr));
                }).collect(Collectors.toList());
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

        return text.chars()
                .mapToObj(character -> Optional.of(keyList.indexOf((char) character))
                        .filter(value -> value >= 0)
                        .map(value -> String.valueOf(alphabetList.get(value)))
                        .orElse(" "))
                .collect(Collectors.joining());
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

        return text.chars()
                .mapToObj(character ->
                        Optional.of(alphabetList.indexOf((char) character))
                                .filter(value -> value >= 0)
                                .map(value -> String.valueOf(keyList.get(value)))
                                .orElse(" "))
                .collect(Collectors.joining());
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
                    double actualOccurrences = textCharactersOccurrences.getOrDefault(letter, 0);

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
