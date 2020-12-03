package edu.kpi.ip71.dovhopoliuk.ciphers.polyalphabetic.worker;

import edu.kpi.ip71.dovhopoliuk.ciphers.polyalphabetic.population.PolyalphabeticPopulation;
import edu.kpi.ip71.dovhopoliuk.ciphers.substitution.model.Individual;
import edu.kpi.ip71.dovhopoliuk.ciphers.substitution.model.Population;
import edu.kpi.ip71.dovhopoliuk.ciphers.utils.SecurityUtils;

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
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.pow;

public class PolyalphabeticWorker implements Callable<String> {
    private static final int KEY_SEARCH_PRECISION = 2000;
    private static final int TOURNAMENT_SELECTION = 100;
    private static final boolean ELITISM = false;
    private static final double ELITISM_PERCENTAGE = 0.1;
    private static final int SIZE_OF_POPULATION = 100;
    private static final int MAX_GENERATION = 500;
    private static final int ALPHABET_LENGTH = 26;
    private static final double MUTATION_POSSIBILITY = 0.1;
    private static final Random random = new Random();
    private static final boolean SIMPLE_FITNESS = true;
    private static final int EXCHANGE_DIVIDER = 10;

    private final String text;

    public PolyalphabeticWorker(String text) {
        this.text = text;
    }

    @Override
    public String call() {

        List<PolyalphabeticPopulation> populations = init();

        for (int generationCount = 1; generationCount < MAX_GENERATION; generationCount++) {

            List<PolyalphabeticPopulation> list = new ArrayList<>();

            for (PolyalphabeticPopulation population : populations) {
                PolyalphabeticPopulation evolvePopulation =
                        evolvePopulation(population, text);
                list.add(evolvePopulation);
            }
            populations = list;

            if (generationCount % EXCHANGE_DIVIDER == 0) {
                exchangeKeys(populations);

                logPopulations(populations, generationCount);
            }
        }


        List<List<Character>> keys = populations.get(0).getKeys();
        keys.forEach(System.out::println);

        String decrypt = decrypt(text, keys);
        System.out.println(decrypt);
        return decrypt + "\n";
    }

    private void logPopulations(final List<PolyalphabeticPopulation> populations, final int generationCount) {

        double fitness = getFitness(decrypt(text, populations.get(0).getKeys()));

        System.out.println("Generation #" + generationCount);
        populations.get(0).getKeys().forEach(System.out::println);
        System.out.println("Fitness: " + fitness);
        System.out.println();
    }

    private void exchangeKeys(final List<PolyalphabeticPopulation> populations) {

        List<List<Character>> keysToExchange = populations.stream().map(this::getFittest).map(Individual::getKey).collect(Collectors.toList());

        populations.forEach(population -> population.setKeys(keysToExchange));
    }

    private List<PolyalphabeticPopulation> init() {

        int keyLength = (int) getKeyLength();
        System.out.println(keyLength);
        Map<Integer, List<Character>> coincidenceTable = createCoincidenceTable(text.toCharArray(), keyLength);

        List<PolyalphabeticPopulation> populations = IntStream.range(0, keyLength).mapToObj(index ->
                new PolyalphabeticPopulation(index, generateInitialIndividuals(SIZE_OF_POPULATION, coincidenceTable.get(index).stream().map(String::valueOf).collect(Collectors.joining()))))
                .collect(Collectors.toList());

        List<List<Character>> keys = populations.stream()
                .map(this::getFittest)
                .map(Individual::getKey)
                .collect(Collectors.toList());

        populations.forEach(population -> population.setKeys(keys));

        populations.forEach(population ->
                population.getIndividuals().forEach(individual ->
                        individual.setFitness(getFitness(decrypt(text, getCompositeKey(population.getKeys(), individual.getKey(), population.getKeyIndex()))))));

        return populations;
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
                    return new Individual(characters, getChiFitness(decrypt(encryptedText, ketStr)));
                }).collect(Collectors.toList());
    }

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

    private PolyalphabeticPopulation evolvePopulation(PolyalphabeticPopulation parentPopulation, String text) {
        int elitismOffset;
        PolyalphabeticPopulation childPopulation = new PolyalphabeticPopulation(parentPopulation.getKeyIndex(), new ArrayList<>());
        childPopulation.setKeys(parentPopulation.getKeys());

        if (ELITISM) {
            final int eliteIndividualAmount = (int) (SIZE_OF_POPULATION * ELITISM_PERCENTAGE);
            childPopulation.getIndividuals().addAll(getFittests(parentPopulation,
                    eliteIndividualAmount));
            elitismOffset = eliteIndividualAmount;
        } else {
            elitismOffset = 0;
        }

        for (int i = elitismOffset; i < parentPopulation.getIndividuals().size() - 1; i += 2) {
            Individual p1 = tournamentSelection(parentPopulation);
            Individual p2 = tournamentSelection(parentPopulation);
            List<Individual> children = crossover(p1, p2);

            final Individual c1 = children.get(0);
            final Individual c2 = children.get(1);

            mutate(c1);
            mutate(c2);

            c1.setFitness(getFitness(decrypt(text, getCompositeKey(parentPopulation.getKeys(), c1.getKey(), parentPopulation.getKeyIndex()))));
            c2.setFitness(getFitness(decrypt(text, getCompositeKey(parentPopulation.getKeys(), c2.getKey(), parentPopulation.getKeyIndex()))));

            childPopulation.getIndividuals().add(c1);
            childPopulation.getIndividuals().add(c2);
        }

        return childPopulation;
    }

    private List<List<Character>> getCompositeKey(final List<List<Character>> keys, final List<Character> key, final int keyIndex) {

        return IntStream.range(0, keys.size())
                .mapToObj(index -> index == keyIndex ? key : keys.get(index))
                .collect(Collectors.toList());
    }

    private Individual tournamentSelection(Population currentPopulation) {

        List<Individual> individuals = new ArrayList<>(currentPopulation.getIndividuals());
        Population tournamentPopulation = new Population(new ArrayList<>());
        for (int i = 0; i < TOURNAMENT_SELECTION; i++) {
            int randomIndex = random.nextInt(individuals.size());
            tournamentPopulation.getIndividuals().add(currentPopulation.getIndividuals().get(randomIndex));
            individuals.remove(randomIndex);
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

    private double getFitness(String newText) {

        Map<String, Long> cipherTrigrams = getNgramsForText(newText, 3);

        if (SIMPLE_FITNESS) {

            return getNgramSum(cipherTrigrams, SecurityUtils.TRIGRAMS);

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

    private double getChiFitness(final String decodedText) {

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
        final double actualOccurrences = Optional.ofNullable(textCharactersOccurrences.get(Character.toUpperCase(letter))).orElse(0);
        final double expectedOccurrences = letterFrequency * textLength;

        return (pow((actualOccurrences - expectedOccurrences), 2)) / expectedOccurrences;
    }

    private double getNgramSum(Map<String, Long> cipherNgrams, Map<String, Double> ngrams) {
        return cipherNgrams.entrySet().stream()
                .mapToDouble(entry -> {
                    final String decipherNgram = entry.getKey();
                    final long decipherNgramFreq = entry.getValue();

                    final double tableFreq =
                            Optional.ofNullable(ngrams.get(decipherNgram)).orElse(0.0);

                    if (tableFreq != 0) {
                        return decipherNgramFreq * (Math.log(tableFreq) / Math.log(2.0));
                    } else {
                        return 0;
                    }
                }).sum();
    }

    private Map<String, Long> getNgramsForText(String newText, int n) {

        return IntStream.range(0, newText.length() - n)
                .mapToObj(i -> newText.substring(i, i + n))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    private Stream<Character> getEnglishAlphabetStream() {
        return Arrays.stream(SecurityUtils.ENGLISH_ALPHABET.split(""))
                .map(st -> st.charAt(0));
    }

    public List<Individual> crossover(final Individual firstParent, final Individual secondParent) {

        Individual ltrChild = new Individual();
        Individual rtlChild = new Individual();

        final List<Character> alphabet =
                SecurityUtils.ENGLISH_ALPHABET.chars().mapToObj(c -> (char) c).map(Character::toUpperCase)
                        .collect(Collectors.toList());

        List<Character> unusedCharacters = new ArrayList<>(alphabet);

        final List<Character> firstParentKey = firstParent.getKey();
        final List<Character> secondParentKey = secondParent.getKey();

        final List<Character> ltrChildKey = new ArrayList<>();

        for (int i = 0; i < alphabet.size(); i++) {
            final Character fpChar = firstParentKey.get(i);
            final Character spChar = secondParentKey.get(i);

            final double fcFreq = SecurityUtils.ENGLISH_LETTERS_FREQUENCY.get(Character.toLowerCase(fpChar));
            final double scFreq = SecurityUtils.ENGLISH_LETTERS_FREQUENCY.get(Character.toLowerCase(spChar));

            if (fcFreq >= scFreq) {
                if (!ltrChildKey.contains(fpChar)) {
                    ltrChildKey.add(fpChar);
                    unusedCharacters.remove(fpChar);
                } else if (!ltrChildKey.contains(spChar)) {
                    ltrChildKey.add(spChar);
                    unusedCharacters.remove(spChar);
                } else {
                    final int randI = random.nextInt(unusedCharacters.size());
                    ltrChildKey.add(unusedCharacters.get(randI));
                    unusedCharacters.remove(randI);
                }
            } else {
                if (!ltrChildKey.contains(spChar)) {
                    ltrChildKey.add(spChar);
                    unusedCharacters.remove(spChar);
                } else if (!ltrChildKey.contains(fpChar)) {
                    ltrChildKey.add(fpChar);
                    unusedCharacters.remove(fpChar);
                } else {
                    final int randI = random.nextInt(unusedCharacters.size());
                    ltrChildKey.add(unusedCharacters.get(randI));
                    unusedCharacters.remove(randI);
                }
            }
        }
        ltrChild.setKey(ltrChildKey);

        final List<Character> rtlChildKey = new ArrayList<>();
        unusedCharacters = new ArrayList<>(alphabet);
        for (int i = alphabet.size() - 1; i >= 0; i--) {
            final Character fpChar = firstParentKey.get(i);
            final Character spChar = secondParentKey.get(i);

            final double fcFreq = SecurityUtils.ENGLISH_LETTERS_FREQUENCY.get(Character.toLowerCase(fpChar));
            final double scFreq = SecurityUtils.ENGLISH_LETTERS_FREQUENCY.get(Character.toLowerCase(spChar));

            if (fcFreq <= scFreq) {
                if (!rtlChildKey.contains(fpChar)) {
                    rtlChildKey.add(0, fpChar);
                    unusedCharacters.remove(fpChar);
                } else if (!rtlChildKey.contains(spChar)) {
                    rtlChildKey.add(0, spChar);
                    unusedCharacters.remove(spChar);
                } else {
                    final int randI = random.nextInt(unusedCharacters.size());
                    rtlChildKey.add(0, unusedCharacters.get(randI));
                    unusedCharacters.remove(randI);
                }
            } else {
                if (!rtlChildKey.contains(spChar)) {
                    rtlChildKey.add(0, spChar);
                    unusedCharacters.remove(spChar);
                } else if (!rtlChildKey.contains(fpChar)) {
                    rtlChildKey.add(0, fpChar);
                    unusedCharacters.remove(fpChar);
                } else {
                    final int randI = random.nextInt(unusedCharacters.size());
                    rtlChildKey.add(0, unusedCharacters.get(randI));
                    unusedCharacters.remove(randI);
                }
            }
        }
        rtlChild.setKey(rtlChildKey);

        return List.of(ltrChild, rtlChild);
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
