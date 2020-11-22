package edu.kpi.ip71.dovhopoliuk.substitution.worker;

import edu.kpi.ip71.dovhopoliuk.substitution.model.Individual;
import edu.kpi.ip71.dovhopoliuk.substitution.model.Population;
import edu.kpi.ip71.dovhopoliuk.utils.SecurityUtils;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.abs;
import static java.lang.Math.pow;

public class SubstitutionWorker implements Callable<String> {
    private static final int TOURNAMENT_SELECTION = 10;
    private static final boolean ELITISM = false;
    private static final int SIZE_OF_POPULATION = 50;
    private static final int MAX_GENERATION = 500;
    private static final int ALPHABET_LENGTH = 26;
    private static final double CROSSOVER_POSSIBILITY = 0.5;
    private static final double MUTATION_POSSIBILITY = 0.01;
    private static final char EMPTY_CHAR = '_';

//    private static final List<String> bigrams = List.of("th", "en", "ng", "he", "ed", "of", "in", "to", "al", "er", "it", "de", "an", "ou", "se", "re", "ea", "le", "nd", "hi", "sa", "at", "is", "si", "on", "or", "ar", "nt", "ti", "ve", "ha", "as", "ra", "es", "te", "ld", "st", "et", "ur");

    private static final List<String> bigrams = List.of("th", "en", "ng", "he", "ed", "of", "in", "to");

    private static final List<String> trigrams = List.of("the", "and", "tha", "ent", "ing", "ion", "tio", "for", "nde", "has", "nce", "edt", "tis", "oft", "sth", "men");

    private String text;

    public SubstitutionWorker(String text) {
        this.text = text;
    }

    @Override
    public String call() {
        Population population = new Population(generateInitialIndividuals(SIZE_OF_POPULATION, text));
        for (int generationCount = 1; generationCount < MAX_GENERATION; generationCount++) {
            Individual fittest = getFittest(population);

            System.out.println("Generation number #" + generationCount);
            System.out.println("Most fittest individual with fit " + fittest.getFitness() + " with key " + fittest.getKey());

            population = evolvePopulation(population);
        }

        List<Character> key = getFittest(population).getKey();
        return decrypt(text, key.stream().map(String::valueOf).collect(Collectors.joining()));
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

    private Population evolvePopulation(Population parentPopulation) {
        int elitismOffset;
        Population childPopulation = new Population(new ArrayList<>());

        if (ELITISM) {
            childPopulation.getIndividuals().add(getFittest(parentPopulation));
            elitismOffset = 1;
        } else {
            elitismOffset = 0;
        }

        for (int i = elitismOffset; i < SIZE_OF_POPULATION; i++) {
            Individual p1 = tournamentSelection(parentPopulation);
            Individual p2 = tournamentSelection(parentPopulation);
            Individual child1 = crossover(p1, p2, false);
            Individual child2 = crossover(p1, p2, true);
            mutate(child1);
            mutate(child2);
//
            child1.setFitness(getFitness(text, child1.getKey().stream().map(String::valueOf).collect(Collectors.joining())));
            child2.setFitness(getFitness(text, child2.getKey().stream().map(String::valueOf).collect(Collectors.joining())));
//
            childPopulation.getIndividuals().add(child1);
            childPopulation.getIndividuals().add(child2);
        }

        return childPopulation;
    }

    private Individual tournamentSelection(Population currentPopulation) {
        Population tournamentPopulation = new Population(new ArrayList<>());
        for (int i = 0; i < TOURNAMENT_SELECTION; i++) {
            int randomIndex = (int) (Math.random() * currentPopulation.getIndividuals().size());
            tournamentPopulation.getIndividuals().add(currentPopulation.getIndividuals().get(randomIndex));
        }

        return getFittest(tournamentPopulation);
    }

    private Individual getFittest(Population population) {
        return population.getIndividuals().stream()
                .max(Comparator.comparingDouble(Individual::getFitness))
                .orElseThrow();
    }

    private double getFitness(String encryptedText, String key) {
        int textLength = encryptedText.length();
        String decryptedText = decrypt(encryptedText, key);
//        return  -calculateChiSquared(decryptedText);

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

        return ((2 * (textLength - minExpectedOccurrences)) - sum) / (2 * (textLength - minExpectedOccurrences))
                + (0.25 * calculateOccurrencesOfBigramsInText(decryptedText))
                + (0.5 * calculateOccurrencesOfTrigramsInText(decryptedText));
    }

    private int calculateOccurrencesOfBigramsInText(final String text) {

        return bigrams.stream()
                .mapToInt(bigram -> (text.length() - text.replaceAll(bigram.toUpperCase(), "").length()) / 2)
                .sum();
    }

    private int calculateOccurrencesOfTrigramsInText(final String text) {

        return trigrams.stream()
                .mapToInt(bigram -> (text.length() - text.replaceAll(bigram.toUpperCase(), "").length()) / 3)
                .sum();
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
        final double actualOccurrences = Optional.ofNullable(textCharactersOccurrences.get(Character.toUpperCase(letter))).orElse(0);
        final double expectedOccurrences = letterFrequency * textLength;

        return (pow((actualOccurrences - expectedOccurrences), 2)) / expectedOccurrences;
    }

    private int calculateOccurrencesOfLetterInText(final Character letter, final String text) {

        return (int) text.chars()
                .filter(character -> Character.valueOf((char) character).equals(letter))
                .count();
    }

    private int calculateEthalonOccurrencesOfLetterInText(final Character letter, final String text) {

        final double letterFrequency = SecurityUtils.ENGLISH_LETTERS_FREQUENCY.get(Character.toLowerCase(letter));
        return (int) letterFrequency * text.length();
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

    private Individual crossover(final Individual firstParent, final Individual secondParent, final boolean reversed) {

        Individual child = new Individual();

        child.setKey(IntStream.range(0, ALPHABET_LENGTH).mapToObj(index -> EMPTY_CHAR).collect(Collectors.toList()));

        List<Character> alph = getEnglishAlphabetStream().map(Character::toUpperCase).collect(Collectors.toList());

        IntStream.range(0, ALPHABET_LENGTH)
                .map(index -> reversed ? 25 - index : index)
                .forEach(index -> {

                    int firstParentOcc = calculateOccurrencesOfLetterInText(firstParent.getKey().get(index), text);
                    int secondParentOcc = calculateOccurrencesOfLetterInText(secondParent.getKey().get(index), text);
                    int realOcc = calculateEthalonOccurrencesOfLetterInText(alph.get(index), text);

                    double firstDiff = Math.pow(realOcc - firstParentOcc, 2);
                    double secondDiff = Math.pow(realOcc - secondParentOcc, 2);

                    List<Character> childKey = child.getKey();

                    if (firstDiff < secondDiff && !childKey.contains(firstParent.getKey().get(index))) {

                        childKey.set(index, firstParent.getKey().get(index));

                    } else if (firstDiff > secondDiff && !childKey.contains(secondParent.getKey().get(index))) {

                        childKey.set(index, secondParent.getKey().get(index));

                    } else if (!childKey.contains(firstParent.getKey().get(index))) {

                        childKey.set(index, firstParent.getKey().get(index));

                    } else if (!childKey.contains(secondParent.getKey().get(index))) {

                        childKey.set(index, secondParent.getKey().get(index));
                    }
                });

        IntStream.range(0, ALPHABET_LENGTH)
                .map(index -> reversed ? 25 - index : index)
                .filter(index -> child.getKey().get(index).equals(EMPTY_CHAR))
                .forEach(index -> {
                    Character charToSet = getEnglishAlphabetStream()
                            .map(Character::toUpperCase)
                            .filter(character -> !child.getKey().contains(character))
                            .map(character -> {
                                int occ = calculateOccurrencesOfLetterInText(character, text);
                                int realOcc = calculateEthalonOccurrencesOfLetterInText(alph.get(index), text);
                                double diff = Math.pow(realOcc - occ, 2);

                                return Map.entry(character, diff);
                            })
                            .min(Comparator.comparingDouble(Map.Entry::getValue))
                            .map(Map.Entry::getKey)
                            .orElseThrow();

                    child.getKey().set(index, charToSet);
                });

//        getEnglishAlphabetStream()
//                .map(Character::toUpperCase)
//                .filter(character -> !child.getKey().contains(character))
//                .forEach(character -> {
//
//
//
//                    List<Integer> freeIndexes = IntStream.range(0, ALPHABET_LENGTH)
//                            .filter(index -> child.getKey().get(index).equals(EMPTY_CHAR))
//                            .boxed()
//                            .collect(Collectors.toList());
//
//                    int randomIndex = (int) (Math.random() * freeIndexes.size());
//
//                    child.getKey().set(freeIndexes.get(randomIndex), character);
//                });

        return child;
    }

    private void mutate(final Individual child) {

        List<Character> key = child.getKey();

        IntStream.range(0, ALPHABET_LENGTH)
                .forEach(index -> {
                    if (Math.random() <= MUTATION_POSSIBILITY) {
                        final int firstPosition = (int) (Math.random() * ALPHABET_LENGTH);
                        final int secondPosition = (int) (Math.random() * ALPHABET_LENGTH);

                        char buffer = key.get(firstPosition);
                        key.set(firstPosition, key.get(secondPosition));
                        key.set(secondPosition, buffer);
                    }
                });
    }

    private Individual getParentForGen(Individual firstParent, Individual secondParent) {

        return Math.random() <= CROSSOVER_POSSIBILITY ? firstParent : secondParent;
    }
}
