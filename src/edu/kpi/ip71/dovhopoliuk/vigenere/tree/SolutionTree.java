package edu.kpi.ip71.dovhopoliuk.vigenere.tree;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SolutionTree {

    private static final int QUANTITY_OF_BEST_RESULTS_TO_KEEP = 1;

    private final List<Map.Entry<Character, Double>> letters;
    private final Map<Character, Double> englishLettersFrequencies;
    private final List<Node> leafNodes;

    public SolutionTree(final List<Map.Entry<Character, Double>> letters, final Map<Character, Double> englishLettersFrequencies) {

        this.letters = letters;
        this.leafNodes = new LinkedList<>();
        this.englishLettersFrequencies = englishLettersFrequencies;
    }

    private static class Node {
        final List<Character> currentResult;
        final List<Map.Entry<Character, Double>> availableLetters;
        final List<Map.Entry<Character, Double>> unusedEnglishLetters;
        final Map.Entry<Character, Double> letter;
        final char keyLetter;
        final boolean isTerminated;
        final double mark;

        private Node(final List<Character> currentResult,
                     final List<Map.Entry<Character, Double>> unusedEnglishLetters,
                     final List<Map.Entry<Character, Double>> availableLetters,
                     final Map.Entry<Character, Double> letter, final char keyLetter,
                     final boolean isTerminated, final double mark) {

            this.currentResult = currentResult;
            this.unusedEnglishLetters = unusedEnglishLetters;
            this.availableLetters = availableLetters;
            this.letter = letter;
            this.keyLetter = keyLetter;
            this.isTerminated = isTerminated;
            this.mark = mark;
        }
    }

    public List<Character> solve(final List<Map.Entry<Character, Double>> englishLetters) {

        createRootNodes(englishLetters);

        System.out.println("Root nodes...: " + leafNodes.size());

        solveInner();

        return collectResults();
    }

    private void createRootNodes(final List<Map.Entry<Character, Double>> englishLetters) {
        final List<Node> nodesToAdd = new ArrayList<>();

//        englishLetters.forEach(engLetter -> createRootNode(letters.get(0), engLetter, englishLetters, nodesToAdd));
//        letters.forEach(letter ->
//                createRootNode(letter, englishLetters.get(0), englishLetters, nodesToAdd));
//        letters.forEach(letter ->
//                createRootNode(letter, englishLetters.get(1), englishLetters, nodesToAdd));
        letters.forEach(letter ->
//                createRootNode(letter, englishLetters.get(2), englishLetters, nodesToAdd));
                englishLetters.forEach(engLetter ->
                        createRootNode(letter, engLetter, englishLetters, nodesToAdd)));
        leafNodes.addAll(nodesToAdd);
    }

    private void createRootNode(final Map.Entry<Character, Double> letter, final Map.Entry<Character, Double> engLetter, final List<Map.Entry<Character, Double>> unusedEnglishLetters, final List<Node> nodesToAdd) {

        final char keyLetter = (char) (letter.getKey() ^ engLetter.getKey());

//        final List<Node> nodesToAdd = new ArrayList<>();
        Node node = createNode(List.of(letter.getKey()), letter, engLetter, letters, unusedEnglishLetters, keyLetter, 0);

        solveForNode(node, nodesToAdd);

//        leafNodes.addAll(nodesToAdd);
//        leafNodes.add(node);
    }

    private void solveInner() {

//        final List<Node> nodesToAdd = new ArrayList<>();
//
//        leafNodes
//                .stream()
//                .filter(node -> !node.isTerminated)
//                .forEach(node -> solveForNode(node, nodesToAdd));
//
//        leafNodes.addAll(nodesToAdd);

//        while (!solutionCompleted()) {
//
//            System.out.println("+cycle");
//
//            final List<Node> nodesToAdd = new ArrayList<>();
//
//            leafNodes
//                    .stream()
//                    .filter(node -> !node.isTerminated)
//                    .forEach(node -> solveForNode(node, nodesToAdd));
//
//            leafNodes.addAll(nodesToAdd);
//        }
    }

    private boolean solutionCompleted() {

       return leafNodes.stream()
                .allMatch(node -> node.isTerminated);
    }

    private void solveForNode(final Node node, List<Node> nodesToAdd) {

        for (Map.Entry<Character, Double> availableLetter : node.availableLetters) {
            for (Map.Entry<Character, Double> unusedEnglishLetter : node.unusedEnglishLetters) {
                createChildNode(node, availableLetter, unusedEnglishLetter, node.unusedEnglishLetters, nodesToAdd)
                        .ifPresent(childNode -> solveForNode(childNode, nodesToAdd));
            }
        }

//        node.availableLetters.forEach(letter ->{
//
//            var possibleLetters = node.unusedEnglishLetters.stream()
//                    .filter(engLetter -> (letter.getKey() ^ engLetter.getKey()) == node.keyLetter)
//                    .collect(Collectors.toList());
//
////            node.unusedEnglishLetters
//                possibleLetters
//                        .forEach(engLetter -> createChildNode(node, letter, engLetter, node.unusedEnglishLetters, nodesToAdd)
//                                .ifPresent(childNode -> solveForNode(childNode, nodesToAdd)));
//        });
    }

    private Optional<Node> createChildNode(final Node parentNode, final Map.Entry<Character, Double> letter, final Map.Entry<Character, Double> engLetter, final List<Map.Entry<Character, Double>> unusedEnglishLetters, List<Node> nodesToAdd) {

//        System.out.println("Parent available letters: " + parentNode.availableLetters.size());
        if ((letter.getKey() ^ engLetter.getKey()) == parentNode.keyLetter && isEffectiveToAdd(parentNode, letter, engLetter, nodesToAdd)) {

            var result = new ArrayList<>(parentNode.currentResult);
            result.add(letter.getKey());

            Node node = createNode(result, letter, engLetter, parentNode.availableLetters, unusedEnglishLetters, parentNode.keyLetter, parentNode.mark);


            if (node.isTerminated) {
                nodesToAdd.add(node);
            }

            return Optional.of(node);
        }

        return Optional.empty();
    }

    private Node createNode(final List<Character> result, final Map.Entry<Character, Double> letter, final Map.Entry<Character, Double> engLetter, final List<Map.Entry<Character, Double>> letters, final List<Map.Entry<Character, Double>> unusedEnglishLetters, final char keyLetter, final double mark) {

        var availableLetters = excludeFromList(letter, letters);

        var addToMark = Math.pow(englishLettersFrequencies.get(engLetter.getKey()) - letter.getValue(), 2);

        return new Node(result,
                excludeFromList(engLetter, unusedEnglishLetters),
                excludeFromList(letter, letters),
                letter,
                keyLetter,
                availableLetters.size() == 0,
                mark + addToMark);
    }

    private List<Map.Entry<Character, Double>> excludeFromList(final Map.Entry<Character, Double> letter, final List<Map.Entry<Character, Double>> letters) {

        return letters.stream()
                .filter(entry -> !entry.getKey().equals(letter.getKey()))
                .collect(Collectors.toList());
    }

    private List<Character> collectResults() {

        return leafNodes.stream()
                .filter(node -> node.isTerminated)
                .map(node -> node.keyLetter)
                .collect(Collectors.toList());
    }

    private boolean isEffectiveToAdd(final Node parentNode, final Map.Entry<Character, Double> letter, final Map.Entry<Character, Double> engLetter, final List<Node> nodesToAdd) {

        var possibleMark = parentNode.mark + Math.pow(englishLettersFrequencies.get(engLetter.getKey()) - letter.getValue(), 2);

        return Stream.of(leafNodes, nodesToAdd)
                .flatMap(Collection::stream)
                .filter(node -> node.mark <= possibleMark)
                .filter(node -> node.isTerminated)
                .count()
                < QUANTITY_OF_BEST_RESULTS_TO_KEEP;
    }
}
