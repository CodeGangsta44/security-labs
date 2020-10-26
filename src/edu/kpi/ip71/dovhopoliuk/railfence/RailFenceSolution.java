package edu.kpi.ip71.dovhopoliuk.railfence;

import edu.kpi.ip71.dovhopoliuk.railfence.worker.RailFenceWorker;
import edu.kpi.ip71.dovhopoliuk.solution.Solution;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RailFenceSolution implements Solution {

    private static final int THREADS = 8;

    @Override
    public void solve(final String inputFilePath, final String outputFilePath) {

        final String text = readFromFile(inputFilePath);

        final String result = solveInner(text);

        writeToFile(outputFilePath, result);
    }

    private String solveInner(final String text) {

        StringBuffer buffer = new StringBuffer();

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);

        final RailFenceWorker[] workers = createWorkers(text);

        final List<Future<String>> futures = submitWorkers(workers, executor);

        futures.forEach(future -> buffer.append(getResultFromFuture(future)));

        executor.shutdown();

        return buffer.toString();
    }

    private RailFenceWorker[] createWorkers(final String text) {

        final int chunkSize = (int) Math.ceil((double) text.length() / (THREADS));

        return IntStream.range(0, THREADS)
                .mapToObj(index -> new RailFenceWorker(text, getLowerKey(index, chunkSize), getUpperKey(index, chunkSize, text.length())))
                .toArray(RailFenceWorker[]::new);
    }

    private List<Future<String>> submitWorkers(final RailFenceWorker[] workers, final ExecutorService executor) {

        return Arrays.stream(workers)
                .map(executor::submit)
                .collect(Collectors.toList());
    }

    private String getResultFromFuture(final Future<String> future) {

        String result = "";

        try {

            result = future.get();

        } catch (final Exception e) {

            e.printStackTrace();
        }

        return result;
    }

    private int getLowerKey(final int index, final int chunkSize) {

        return Math.max(2, index * chunkSize);
    }

    private int getUpperKey(final int index, final int chunkSize, final int textLength) {

        return Math.min((index + 1) * chunkSize, textLength);
    }

    private void writeToFile(final String path, final String result) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {

            writer.append(result);

        } catch (final IOException e) {

            e.printStackTrace();
        }
    }

    private String readFromFile(final String path) {

        String result = null;

        try (final BufferedReader reader = new BufferedReader(new FileReader(path))) {

            result = reader.lines().collect(Collectors.joining("\n"));

        } catch (final IOException e) {

            e.printStackTrace();
        }

        return result;
    }
}
