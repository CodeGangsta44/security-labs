package edu.kpi.ip71.dovhopoliuk.ciphers.vigenere.worker;

import edu.kpi.ip71.dovhopoliuk.ciphers.utils.Base16;
import edu.kpi.ip71.dovhopoliuk.ciphers.utils.SecurityUtils;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.pow;

public class VigenereXorWorker implements Callable<String> {

    private static final int KEY_SEARCH_PRECISION = 1000;

    private static final List<Character> CIPHER_ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".chars()
            .mapToObj(c -> (char) c)
            .collect(Collectors.toList());

    private final String text;

    public static final Collector<Character, StringBuilder, String> STRING_FROM_CHARS_COLLECTOR =
            Collector.of(StringBuilder::new, StringBuilder::append, StringBuilder::append, StringBuilder::toString);

    public VigenereXorWorker(final String text) {

        this.text = Base16.getDecoder().decode(text);
    }

    @Override
    public String call() {

        final int lengthOfKey = (int) getKeyLength();

        final Map<Integer, List<Character>> coincidenceTable = createCoincidenceTable(text.toCharArray(), lengthOfKey);

        String key = findKey(coincidenceTable);

        return decodeWithKey(text, key);
    }

    public String decode(final String textToEncode, final String key) {

        return textToEncode.chars().mapToObj(c -> (char) (c ^ key.charAt(0)))
                .collect(STRING_FROM_CHARS_COLLECTOR);
    }

    private String findKey(final Map<Integer, List<Character>> coincidenceTable) {

        return coincidenceTable.values().stream()
                .map(characters -> findKeyLetter(characters.stream().map(String::valueOf).collect(Collectors.joining())))
                .collect(Collectors.joining());
    }

    private String findKeyLetter(final String text) {

        return CIPHER_ALPHABET.stream()
                .map(key -> Map.entry(key, calculateChiSquared(decode(text, String.valueOf(key)))))
                .min(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .map(Object::toString)
                .orElseThrow();
    }

    private double calculateChiSquared(final String decodedText) {

        final List<Character> decodedTextCharacters = decodedText
                .chars().mapToObj(c -> (char) c).collect(Collectors.toList());

        final Map<Character, Integer> textCharactersOccurrences = decodedTextCharacters.stream()
                .distinct()
                .map(character -> Map.entry(character, Collections.frequency(decodedTextCharacters, character)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (existing, replacement) -> existing, TreeMap::new));

        return Arrays.stream(SecurityUtils.ENGLISH_ALPHABET.split(""))
                .map(letter -> letter.charAt(0))
                .mapToDouble(letter -> calculateChiSquaredForLetter(letter, decodedText, textCharactersOccurrences))
                .sum();
    }

    private double calculateChiSquaredForLetter(final Character letter, final String decodedText, final Map<Character, Integer> textCharactersOccurrences) {

        final int textLength = decodedText.length();
        final double letterFrequency = SecurityUtils.ENGLISH_LETTERS_FREQUENCY.get(letter);
        final double actualOccurrences = Optional.ofNullable(textCharactersOccurrences.get(letter)).orElse(0);
        final double expectedOccurrences = letterFrequency * textLength;

        return (pow((actualOccurrences - expectedOccurrences), 2)) / expectedOccurrences;
    }

    private Map<Integer, List<Character>> createCoincidenceTable(final char[] textSymbols, final int keyLength) {

        return IntStream.range(0, textSymbols.length)
                .boxed()
                .collect(Collectors.groupingBy(index -> index % keyLength,
                        Collectors.mapping(index -> textSymbols[index], Collectors.toList())));
    }

    private String decodeWithKey(final String encodedText, final String key) {

        return IntStream.range(0, encodedText.length())
                .mapToObj(index -> String.valueOf((char) (encodedText.charAt(index) ^ key.charAt(index % key.length()))))
                .collect(Collectors.joining());
    }

    private long getKeyLength() {

        final Map<Integer, Double> indexesOfCoincidence = new HashMap<>();

        IntStream.range(1, text.length())
                .forEach(index -> calculateIndexOfCoincidence(indexesOfCoincidence, text, index));

        final int maxIndex = getMaxIndex(indexesOfCoincidence);

        final double average = getAverage(indexesOfCoincidence);

        return getKeyLength(indexesOfCoincidence, indexesOfCoincidence.get(maxIndex), average);
    }

    private void calculateIndexOfCoincidence(final Map<Integer, Double> indexesOfCoincidence, final String text,
                                             final int step) {

        final String movedText = getMovedString(text, step);

        long coincidenceQuantity = IntStream.range(0, text.length())
                .filter(index -> text.charAt(index) == movedText.charAt(index))
                .count();

        indexesOfCoincidence.put(step, ((double) coincidenceQuantity) / text.length());
    }

    private String getMovedString(final String text, final int step) {

        return String.join("", text.substring(text.length() - step), text.chars()
                .limit(text.length() - step)
                .mapToObj(character -> String.valueOf((char) character))
                .collect(Collectors.joining()));

    }

    private int getMaxIndex(final Map<Integer, Double> indexesOfCoincidence) {

        return indexesOfCoincidence.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalArgumentException("No key found"));
    }

    private double getAverage(final Map<Integer, Double> indexesOfCoincidence) {

        return indexesOfCoincidence.values()
                .stream()
                .mapToDouble(value -> value)
                .average()
                .orElseThrow(() -> new IllegalArgumentException("No average found"));
    }

    private long getKeyLength(final Map<Integer, Double> indexesOfCoincidence, final double maxValue,
                              final double averageValue) {

        double step = (maxValue - averageValue) / KEY_SEARCH_PRECISION;

        Map<Long, Long> lengthsMap = IntStream.range(1, KEY_SEARCH_PRECISION)
                .mapToObj(index -> index * step)
                .map(bound -> getPossibleKeyLength(getIndexesOfLargerElements(indexesOfCoincidence, maxValue - bound)))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return lengthsMap.entrySet()
                .stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalArgumentException("No key found"));
    }

    private List<Integer> getIndexesOfLargerElements(final Map<Integer, Double> indexesOfCoincidence,
                                                     final double bound) {

        return indexesOfCoincidence.entrySet()
                .stream()
                .filter(entry -> entry.getValue() > bound)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private long getPossibleKeyLength(List<Integer> indexesOfLargerElements) {

        return IntStream.range(1, indexesOfLargerElements.size())
                .mapToObj(index -> indexesOfLargerElements.get(index) - indexesOfLargerElements.get(index - 1))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalArgumentException("No key found"));
    }
}
