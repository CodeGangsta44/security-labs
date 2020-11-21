package edu.kpi.ip71.dovhopoliuk.vigenere.worker;

import edu.kpi.ip71.dovhopoliuk.utils.Base16;
import edu.kpi.ip71.dovhopoliuk.utils.SecurityUtils;
import edu.kpi.ip71.dovhopoliuk.vigenere.tree.SolutionTree;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.pow;
import static java.util.Objects.isNull;

public class VigenereXorWorker implements Callable<String> {

    private static final int KEY_SEARCH_PRECISION = 1000;

    private static final List<Character> CIPHER_ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".chars()
            .mapToObj(c -> (char) c)
            .collect(Collectors.toList());

    private String text;

    public static final Collector<Character, StringBuilder, String> STRING_FROM_CHARS_COLLECTOR =
            Collector.of(StringBuilder::new, StringBuilder::append, StringBuilder::append, StringBuilder::toString);

    public VigenereXorWorker(final String text) {
        this.text = text;
    }

    @Override
    public String call() {
        text = Base16.getDecoder().decode(text);

        final int lengthOfKey = (int) getKeyLength();

        Map<Integer, List<Character>> coincidenceTable = createCoincidenceTable(text.toCharArray(), lengthOfKey);

        StringBuilder keyBuilder = new StringBuilder();
        for (Map.Entry<Integer, List<Character>> sameKeyEncryptedChars : coincidenceTable.entrySet()) {
            String sameLetterEncryptedText = sameKeyEncryptedChars.getValue().stream().collect(STRING_FROM_CHARS_COLLECTOR);
            keyBuilder.append(findKey(sameLetterEncryptedText));
        }
        String key = keyBuilder.toString();

        return decodeWithKey(text, key);
    }

    public String decode(String textToEncode, String key) {
        return textToEncode.chars().mapToObj(c -> (char) (c ^ key.charAt(0)))
                .collect(STRING_FROM_CHARS_COLLECTOR);
    }

    private String findKey(String text) {
        Map<Character, Double> chiSquaredForKeys = new LinkedHashMap<>();
        for (Character key : CIPHER_ALPHABET) {
            String decodedText = decode(text, String.valueOf(key));
            double chiSquared = calculateChiSquared(decodedText);
            chiSquaredForKeys.put(key, chiSquared);
        }
        Comparator<Map.Entry<Character, Double>> entryComparator = Comparator
                .comparingDouble(Map.Entry::getValue);

        Map.Entry<Character, Double> minChiSquaredForKey = chiSquaredForKeys
                .entrySet()
                .stream()
                .min(entryComparator)
                .orElseThrow();

        return minChiSquaredForKey.getKey().toString();
    }

    private double calculateChiSquared(String decodedText) {
        Map<Character, Integer> textCharactersOccurrences = new TreeMap<>();

        List<Character> decodedTextCharacters = decodedText
                .chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        List<Character> distinctChars = decodedTextCharacters.stream().distinct().collect(Collectors.toList());

        for (Character character : distinctChars) {
            int charOccurrences = Collections.frequency(decodedTextCharacters, character);
            textCharactersOccurrences.put(character, charOccurrences);
        }

        return Arrays.stream(SecurityUtils.ENGLISH_ALPHABET.split(""))
                .map(letter -> letter.charAt(0))
                .mapToDouble(letter -> {
                    int textLength = decodedText.length();
                    double letterFrequency = SecurityUtils.ENGLISH_LETTERS_FREQUENCY.get(letter);
                    double actualOccurrences = Optional.ofNullable(textCharactersOccurrences.get(letter)).orElse(0);
                    double expectedOccurrences = letterFrequency * textLength;

                    return (pow((actualOccurrences - expectedOccurrences), 2)) / expectedOccurrences;
                }).sum();
    }

    private Map<Integer, List<Character>> createCoincidenceTable(char[] textSymbols, int keyLength) {
        Map<Integer, List<Character>> sameKeyLetterEncryptedChars = new LinkedHashMap<>();
        for (int i = 0; i < textSymbols.length; i++) {
            int keyLetterPosition = i % keyLength;

            List<Character> characters = sameKeyLetterEncryptedChars.get(keyLetterPosition);
            if (isNull(characters)) {
                characters = new ArrayList<>();
                characters.add(textSymbols[i]);
                sameKeyLetterEncryptedChars.put(keyLetterPosition, characters);
            } else {
                characters.add(textSymbols[i]);
            }
        }
        return sameKeyLetterEncryptedChars;
    }

    private String decodeWithKey(String encodedText, String key) {
        char[] charsFromText = encodedText.toCharArray();
        List<Character> decodedCharacters = new ArrayList<>();
        for (int i = 0; i < charsFromText.length; i++) {
            char charFromText = charsFromText[i];
            char decodedChar = (char) (charFromText ^ key.charAt(i % key.length()));
            decodedCharacters.add(decodedChar);
        }
        Optional<String> reduce = decodedCharacters.stream().map(String::valueOf).reduce(String::concat);
        return reduce.orElse("");
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
