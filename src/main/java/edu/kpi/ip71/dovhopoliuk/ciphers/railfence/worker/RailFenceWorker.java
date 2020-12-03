package edu.kpi.ip71.dovhopoliuk.ciphers.railfence.worker;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RailFenceWorker implements Callable<String> {

    private final String text;
    private final int lowerKey;
    private final int upperKey;

    public RailFenceWorker(final String text, final int lowerKey, final int upperKey) {

        this.text = text;
        this.lowerKey = lowerKey;
        this.upperKey = upperKey;
    }

    @Override
    public String call() {

        final StringBuilder resultBuffer = new StringBuilder();

        for (int i = lowerKey; i < upperKey; i++) {
            resultBuffer.append("\n-=======Solution for key ").append(i).append(" =======-\n");
            resultBuffer.append(solveForKey(i));
            resultBuffer.append('\n');
        }

        return resultBuffer.toString();
    }

    private String solveForKey(final int key) {

        StringBuffer buffer = new StringBuffer();

        final int cycle = calculateCycle(key);

        final Queue<Character> queue = convertTextToQueue(text);

        final Character[][] zigZag = IntStream.range(0, key)
                .mapToObj(index -> new Character[text.length()])
                .toArray(Character[][]::new);

        IntStream.range(0, key)
                .forEach(index -> fillRow(zigZag, queue, index, cycle));

        IntStream.range(0, text.length())
                .forEach(index -> buffer.append(getChar(zigZag, key, index)));

        return buffer.toString();
    }

    private int calculateCycle(final int key) {

        return (key * 2) - 2;
    }

    private Queue<Character> convertTextToQueue(final String text) {

        return text.chars()
                .mapToObj(index -> (char) index)
                .collect(Collectors.toCollection(ArrayDeque::new));
    }

    public Character getChar(final Character[][] zigZag, final int key, final int index) {

        return IntStream.range(0, key)
                .mapToObj(rowNumber -> zigZag[rowNumber][index])
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }

    private void fillRow(final Character[][] zigZag, final Queue<Character> queue, final int rowIndex, final int cycle) {

        final int distance = cycle - (rowIndex * 2);

        for (int i = rowIndex; i < text.length(); i += cycle) {

            zigZag[rowIndex][i] = queue.poll();

            if (distance != cycle && distance != 0 && i + distance < text.length()) {

                zigZag[rowIndex][i + distance] = queue.poll();
            }
        }
    }
}
