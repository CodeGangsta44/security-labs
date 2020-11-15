package edu.kpi.ip71.dovhopoliuk.vigenere.worker;

import edu.kpi.ip71.dovhopoliuk.vigenere.tree.SolutionTree;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VigenereXorWorker implements Callable<String> {

    private static final int KEY_SEARCH_PRECISION = 1000;

    private static final Map<Character, Double> ENGLISH_LETTERS_FREQUENCY =

            Map.ofEntries(
                    new AbstractMap.SimpleEntry<>(' ', 10.0),
                    new AbstractMap.SimpleEntry<>('e', 12.02),
                    new AbstractMap.SimpleEntry<>('t', 9.10),
                    new AbstractMap.SimpleEntry<>('a', 8.12),
                    new AbstractMap.SimpleEntry<>('o', 7.68),
                    new AbstractMap.SimpleEntry<>('i', 7.31),
                    new AbstractMap.SimpleEntry<>('n', 6.95),
                    new AbstractMap.SimpleEntry<>('s', 6.28),
                    new AbstractMap.SimpleEntry<>('r', 6.02),
                    new AbstractMap.SimpleEntry<>('h', 5.92),
                    new AbstractMap.SimpleEntry<>('d', 4.32),
                    new AbstractMap.SimpleEntry<>('l', 3.98),
                    new AbstractMap.SimpleEntry<>('u', 2.88),
                    new AbstractMap.SimpleEntry<>('c', 2.71),
                    new AbstractMap.SimpleEntry<>('m', 2.61),
                    new AbstractMap.SimpleEntry<>('f', 2.30),
                    new AbstractMap.SimpleEntry<>('y', 2.11),
                    new AbstractMap.SimpleEntry<>('w', 2.09),
                    new AbstractMap.SimpleEntry<>('g', 2.03),
                    new AbstractMap.SimpleEntry<>('p', 1.82),
                    new AbstractMap.SimpleEntry<>('b', 1.49),
                    new AbstractMap.SimpleEntry<>('v', 1.11),
                    new AbstractMap.SimpleEntry<>('k', 0.69),
                    new AbstractMap.SimpleEntry<>('x', 0.17),
                    new AbstractMap.SimpleEntry<>('q', 0.11),
                    new AbstractMap.SimpleEntry<>('j', 0.10),
                    new AbstractMap.SimpleEntry<>('z', 0.07)
            );

    private static final List<Map.Entry<Character, Double>> SORTED_ENGLISH_LETTERS_FREQUENCY =
            ENGLISH_LETTERS_FREQUENCY.entrySet()
                    .stream()
                    .sorted(Comparator.comparingDouble((ToDoubleFunction<Map.Entry<Character, Double>>) Map.Entry::getValue).reversed())
                    .collect(Collectors.toList());

    private final String text;

    public VigenereXorWorker(final String text) {

        this.text = text;
    }

    @Override
    public String call() {

        final int lengthOfKey = (int) getKeyLength();

        System.out.println("Key length: " + lengthOfKey);

        final List<Map<Character, Double>> mapsOfFrequency = getMapsOfFrequency(text, lengthOfKey);

        System.out.println(mapsOfFrequency.get(0).size());
        System.out.println(mapsOfFrequency.get(1).size());
        System.out.println(mapsOfFrequency.get(2).size());
        System.out.println(mapsOfFrequency.get(3).size());
        System.out.println(mapsOfFrequency.get(4).size());
        System.out.println(mapsOfFrequency.get(5).size());

        var start = System.nanoTime();
        List<Character> keyLetters1 = findKeyLettersForGroup(mapsOfFrequency.get(0));
        List<Character> keyLetters2 = findKeyLettersForGroup(mapsOfFrequency.get(1));
        List<Character> keyLetters3 = findKeyLettersForGroup(mapsOfFrequency.get(2));
        List<Character> keyLetters4 = findKeyLettersForGroup(mapsOfFrequency.get(3));
        List<Character> keyLetters5 = findKeyLettersForGroup(mapsOfFrequency.get(4));
        List<Character> keyLetters6 = findKeyLettersForGroup(mapsOfFrequency.get(5));

//        var start = System.nanoTime();
        System.out.println(keyLetters1);
        System.out.println(keyLetters2);
        System.out.println(keyLetters3);
        System.out.println(keyLetters4);
        System.out.println(keyLetters5);
        System.out.println(keyLetters6);

        System.out.println("Execution time = " + (System.nanoTime() - start)+ "ns");








//        mapsOfFrequency
//                .forEach(map -> {
//                    System.out.println();
//                    map.entrySet()
//                            .stream()
//                            .sorted(Comparator.comparingDouble(Map.Entry::getValue))
//                            .forEach(System.out::println);
//                    System.out.println();
//                });

        return null;
    }

    private long getKeyLength() {

        final Map<Integer, Double> indexesOfCoincidence = new HashMap<>();

        IntStream.range(1, text.length())
                .forEach(index -> calculateIndexOfCoincidence(indexesOfCoincidence, text, index));

        final int maxIndex = getMaxIndex(indexesOfCoincidence);

        final double average = getAverage(indexesOfCoincidence);

        return getKeyLength(indexesOfCoincidence, indexesOfCoincidence.get(maxIndex), average);
    }

    private void calculateIndexOfCoincidence(final Map<Integer, Double> indexesOfCoincidence, final String text, final int step) {

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

    private long getKeyLength(final Map<Integer, Double> indexesOfCoincidence, final double maxValue, final double averageValue) {

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

    private List<Integer> getIndexesOfLargerElements(final Map<Integer, Double> indexesOfCoincidence, final double bound) {

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

    private List<Map<Character, Double>> getMapsOfFrequency(final String text, final int lengthOfKey) {

        final List<List<Character>> groups = IntStream.range(0, lengthOfKey)
                .mapToObj(key -> new ArrayList<Character>())
                .collect(Collectors.toList());

        IntStream.range(0, text.length())
                .forEach(index -> groups.get(index % lengthOfKey).add(text.charAt(index)));


        return groups.stream()
                .map(this::calculateFrequency)
                .collect(Collectors.toList());
    }

    private Map<Character, Double> calculateFrequency(final List<Character> group) {

        return group.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), ((double) entry.getValue() / group.size()) * 100))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    private List<Character> findKeyLettersForGroup(final Map<Character, Double> group) {

        List<Map.Entry<Character, Double>> sortedLetters = group.entrySet()
                .stream()
                .sorted(Comparator.comparingDouble((ToDoubleFunction<Map.Entry<Character, Double>>) Map.Entry::getValue).reversed())
                .collect(Collectors.toList());


        SolutionTree tree = new SolutionTree(sortedLetters, ENGLISH_LETTERS_FREQUENCY);

        return tree.solve(SORTED_ENGLISH_LETTERS_FREQUENCY);
    }

}
