package edu.kpi.ip71.dovhopoliuk.ciphers.substitution.worker;

import edu.kpi.ip71.dovhopoliuk.ciphers.substitution.model.Individual;
import edu.kpi.ip71.dovhopoliuk.ciphers.substitution.model.Population;
import edu.kpi.ip71.dovhopoliuk.ciphers.utils.SecurityUtils;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.pow;

public class SubstitutionWorker implements Callable<String> {
    private static final int TOURNAMENT_SELECTION = 100;
    private static final boolean ELITISM = true;
    private static final int SIZE_OF_POPULATION = 500;
    private static final int MAX_GENERATION = 50;
    private static final int ALPHABET_LENGTH = 26;
    private static final double CROSSOVER_POSSIBILITY = 0.5;
    private static final double MUTATION_POSSIBILITY = 0.1;
    private static final char EMPTY_CHAR = '_';

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

        for (int i = elitismOffset; i < parentPopulation.getIndividuals().size(); i++) {
            Individual p1 = tournamentSelection(parentPopulation);
            Individual p2 = tournamentSelection(parentPopulation);
            Individual child = crossover(p1, p2);
            mutate(child);

            child.setFitness(getFitness(text, child.getKey().stream().map(String::valueOf).collect(Collectors.joining())));

            childPopulation.getIndividuals().add(child);
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
        String newText = decrypt(encryptedText, key);
        Map<String, Integer> cipherFourgrams = new HashMap<>();

        IntStream.range(0, newText.length() - 4)
                .mapToObj(i -> newText.substring(i, i + 4))
                .forEach(tet -> {
                    if (cipherFourgrams.containsKey(tet)) {
                        Integer count = cipherFourgrams.get(tet);
                        cipherFourgrams.put(tet, count + 1);
                    } else {
                        cipherFourgrams.put(tet, 1);
                    }
                });


        double sigma = 2.0;

        return cipherFourgrams.entrySet().stream()
                .filter(entry -> SecurityUtils.FOURGRAMS.containsKey(entry.getKey()))
                .mapToDouble(entry -> {
                    Double sourceLogFreq = SecurityUtils.FOURGRAMS.get(entry.getKey());
                    Double logFreq = Math.log(entry.getValue()
                            / (double) (newText.length() - 3));

                    return Math.exp(-(logFreq - sourceLogFreq)
                            * (logFreq - sourceLogFreq) / (2 * pow(sigma, 2)));
                })
                .sum();

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
                    int randomIndex = (int) (Math.random() * freeIndexes.size());

                    child.getKey().set(freeIndexes.get(randomIndex), character);
                });

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
