package edu.kpi.ip71.dovhopoliuk.polyalphabetic.worker;

import edu.kpi.ip71.dovhopoliuk.substitution.model.Individual;
import edu.kpi.ip71.dovhopoliuk.substitution.model.Population;
import edu.kpi.ip71.dovhopoliuk.utils.SecurityUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.pow;

public class PolyalphabeticWorker implements Callable<String> {
    private static final int KEY_SEARCH_PRECISION = 2000;
    private static final int TOURNAMENT_SELECTION = 100;
    private static final boolean ELITISM = true;
    private static final double ELITISM_PERCENTAGE = 0.1;
    private static final int SIZE_OF_POPULATION = 300;
    private static final int MAX_GENERATION = 500;
    private static final int ALPHABET_LENGTH = 26;
    private static final double CROSSOVER_POSSIBILITY = 0.7;
    private static final double MUTATION_POSSIBILITY = 0.1;
    private static final char EMPTY_CHAR = '_';
    private static final Random random = new Random();
    private static final boolean SIMPLE_FITNESS = true;

    private final String text;

    public PolyalphabeticWorker(String text) {
        this.text = text;
    }

    @Override
    public String call() {
        int keyLength = (int) getKeyLength();
        System.out.println(keyLength);
        Map<Integer, List<Character>> coincidenceTable = createCoincidenceTable(text.toCharArray(), keyLength);

        List<Population> populations = IntStream.range(0, keyLength).mapToObj(index ->
                new Population(generateInitialIndividuals(SIZE_OF_POPULATION, text, keyLength, index)))
                .collect(Collectors.toList());

        for (int generationCount = 1; generationCount < MAX_GENERATION; generationCount++) {
            List<Individual> fittestIndividuals =
                    populations.stream().map(this::getFittest).collect(Collectors.toList());

            System.out.println("Generation number #" + generationCount);
            fittestIndividuals.forEach(fittest ->
                    System.out.printf("Most fittest individual with fit %s with key %s%n", fittest.getFitness(),
                            fittest.getKey()));

            List<Population> list = new ArrayList<>();
            for (int i = 0; i < populations.size(); i++) {
                Population evolvePopulation =
                        evolvePopulation(populations.get(i), text, keyLength, i);
                list.add(evolvePopulation);
            }
            populations = list;
        }


        List<List<Character>> keys =
                populations.stream().map(this::getFittest).map(Individual::getKey).collect(Collectors.toList());
        keys.forEach(System.out::println);

        String decrypt = decrypt(text, keys);
        System.out.println(decrypt);
        return decrypt + "\n";
    }

    private List<Individual> generateInitialIndividuals(final int sizeOfPopulation, final String encryptedText, int keyLength, int keyIndex) {

        final Set<List<Character>> generatedKeys = new HashSet<>();

        List<Character> alpha = getEnglishAlphabetStream().map(Character::toUpperCase).collect(Collectors.toList());

        while (generatedKeys.size() < sizeOfPopulation) {

            Collections.shuffle(alpha);
            generatedKeys.add(new ArrayList<>(alpha));
        }

        return generatedKeys.stream()
                .map(characters -> {
                    String ketStr = characters.stream().map(String::valueOf).collect(Collectors.joining());
                    return new Individual(characters, getFitness(encryptedText, ketStr, keyLength, keyIndex));
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

    private String decrypt(String text, List<List<Character>> keys) {
        int keyLength = keys.size();

        List<Character> alphabetList = getEnglishAlphabetStream()
                .map(Character::toUpperCase)
                .collect(Collectors.toList());

        if (keys.get(0).size() != alphabetList.size()) {
            throw new IllegalArgumentException("Illegal key");
        }

        List<Character> textChars = text.chars().mapToObj(intChar -> (char) intChar).collect(Collectors.toList());

        List<Character> result = new ArrayList<>();
        for (int i = 0; i < textChars.size(); i++) {
            int indexOfChar = keys.get(i % keyLength).indexOf(textChars.get(i));
            if (indexOfChar >= 0) {
                Character replacedCharacter = alphabetList.get(indexOfChar);
                result.add(replacedCharacter);
            }
        }
        return result.stream().map(String::valueOf).collect(Collectors.joining());
    }

    private String decrypt(String text, List<Character> key, int keyLength, int keyIndex) {

        List<Character> alphabetList = getEnglishAlphabetStream()
                .map(Character::toUpperCase)
                .collect(Collectors.toList());

        if (key.size() != alphabetList.size()) {
            throw new IllegalArgumentException("Illegal key");
        }

        List<Character> textChars = text.chars().mapToObj(intChar -> (char) intChar).collect(Collectors.toList());

        for (int i = keyIndex; i < textChars.size() - keyIndex; i += keyLength) {
            int indexOfChar = key.indexOf(textChars.get(i));
            if (indexOfChar >= 0) {
                Character replacedCharacter = alphabetList.get(indexOfChar);
                textChars.set(i, replacedCharacter);
            }
        }
        return textChars.stream().map(String::valueOf).collect(Collectors.joining());
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

    private Population evolvePopulation(Population parentPopulation, String text, int keyLength, int keyIndex) {
        int elitismOffset;
        Population childPopulation = new Population(new ArrayList<>());

        if (ELITISM) {
            final int eliteIndividualAmount = (int) (SIZE_OF_POPULATION * ELITISM_PERCENTAGE);
            childPopulation.getIndividuals().addAll(getFittests(parentPopulation,
                    eliteIndividualAmount));
            elitismOffset = eliteIndividualAmount;
        } else {
            elitismOffset = 0;
        }

        for (int i = elitismOffset; i < parentPopulation.getIndividuals().size(); i++) {
            Individual p1 = tournamentSelection(parentPopulation);
            Individual p2 = tournamentSelection(parentPopulation);
            Individual child = crossover(p1, p2);
            mutate(child);

            child.setFitness(
                    getFitness(text, child.getKey().stream().map(String::valueOf).collect(Collectors.joining()), keyLength, keyIndex));

            childPopulation.getIndividuals().add(child);
        }

        return childPopulation;
    }

    private Individual tournamentSelection(Population currentPopulation) {
        Population tournamentPopulation = new Population(new ArrayList<>());
        for (int i = 0; i < TOURNAMENT_SELECTION; i++) {
            int randomIndex = random.nextInt(currentPopulation.getIndividuals().size());
            tournamentPopulation.getIndividuals().add(currentPopulation.getIndividuals().get(randomIndex));
        }

        return getFittest(tournamentPopulation);
    }

    private Individual getFittest(Population population) {
        return getFittests(population, 1).get(0);
    }

    private List<Individual> getFittests(Population population, int amount) {
        return population.getIndividuals().stream()
                .sorted(Comparator.comparingDouble(Individual::getFitness).reversed())
                .limit(amount)
                .collect(Collectors.toList());
    }

    private double getFitness(String encryptedText, String key, int keyLength, int keyIndex) {
        String newText = decrypt(encryptedText, key.chars().mapToObj(c -> (char) c).collect(Collectors.toList()), keyLength, keyIndex);
        Map<String, Integer> cipherTrigrams = new HashMap<>();

        IntStream.range(0, newText.length() - 3)
                .mapToObj(i -> newText.substring(i, i + 3))
                .forEach(tri -> {
                    if (cipherTrigrams.containsKey(tri)) {
                        Integer count = cipherTrigrams.get(tri);
                        cipherTrigrams.put(tri, count + 1);
                    } else {
                        cipherTrigrams.put(tri, 1);
                    }
                });

        if (SIMPLE_FITNESS) {
            return cipherTrigrams.entrySet().stream()
                    .mapToDouble(entry -> {
                        final String decipherTrigram = entry.getKey();
                        final Integer decipherTrigramFreq = entry.getValue();

                        final double tableFreq =
                                Optional.ofNullable(SecurityUtils.TRIGRAMS.get(decipherTrigram)).orElse(0.0);

                        if (tableFreq != 0) {
                            return decipherTrigramFreq * (Math.log(tableFreq) / Math.log(2.0));
                        } else {
                            return 0;
                        }
                    }).sum();
        } else {
            double sigma = 2.0;

            return cipherTrigrams.entrySet().stream()
                    .filter(entry -> SecurityUtils.TRIGRAMS.containsKey(entry.getKey()))
                    .mapToDouble(entry -> {
                        Double sourceLogFreq = SecurityUtils.TRIGRAMS.get(entry.getKey());
                        Double logFreq = Math.log(entry.getValue()
                                / (double) (newText.length() - 3));

                        return Math.exp(-(logFreq - sourceLogFreq)
                                * (logFreq - sourceLogFreq) / (2 * pow(sigma, 2)));
                    })
                    .sum();
        }
    }

    private Stream<Character> getEnglishAlphabetStream() {
        return Arrays.stream(SecurityUtils.ENGLISH_ALPHABET.split(""))
                .map(st -> st.charAt(0));
    }

    private Individual crossover(final Individual firstParent, final Individual secondParent) {

        Individual child = new Individual();

        child.setKey(IntStream.range(0, ALPHABET_LENGTH).mapToObj(index -> EMPTY_CHAR).collect(Collectors.toList()));

        IntStream.range(0, ALPHABET_LENGTH)
                .forEach(index -> {
                    List<Character> childKey = child.getKey();
                    Character characterToAdd = getParentForGen(firstParent, secondParent).getKey().get(index);

                    if (!childKey.contains(characterToAdd)) {
                        childKey.set(index, characterToAdd);
                    }
                });

        getEnglishAlphabetStream()
                .map(Character::toUpperCase)
                .filter(character -> !child.getKey().contains(character))
                .forEach(character -> {
                    List<Integer> freeIndexes = IntStream.range(0, ALPHABET_LENGTH)
                            .filter(index -> child.getKey().get(index).equals(EMPTY_CHAR))
                            .boxed()
                            .collect(Collectors.toList());
                    int randomIndex = random.nextInt(freeIndexes.size());

                    child.getKey().set(freeIndexes.get(randomIndex), character);
                });

        return child;
    }

    private void mutate(final Individual child) {

        List<Character> key = child.getKey();

        IntStream.range(0, ALPHABET_LENGTH)
                .forEach(index -> {
                    if (random.nextDouble() <= MUTATION_POSSIBILITY) {
                        final int firstPosition = random.nextInt(ALPHABET_LENGTH);
                        final int secondPosition = random.nextInt(ALPHABET_LENGTH);

                        char buffer = key.get(firstPosition);
                        key.set(firstPosition, key.get(secondPosition));
                        key.set(secondPosition, buffer);
                    }
                });
    }

    private Individual getParentForGen(Individual firstParent, Individual secondParent) {

        return random.nextDouble() <= CROSSOVER_POSSIBILITY ? firstParent : secondParent;
    }

    private Map<Integer, List<Character>> createCoincidenceTable(final char[] textSymbols, final int keyLength) {

        return IntStream.range(0, textSymbols.length)
                .boxed()
                .collect(Collectors.groupingBy(index -> index % keyLength,
                        Collectors.mapping(index -> textSymbols[index], Collectors.toList())));
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

    private String getMovedString(final String text, final int step) {

        return String.join("", text.substring(text.length() - step), text.chars()
                .limit(text.length() - step)
                .mapToObj(character -> String.valueOf((char) character))
                .collect(Collectors.joining()));

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
