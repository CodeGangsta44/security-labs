package edu.kpi.ip71.dovhopoliuk.ciphers.solution;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public abstract class AbstractSolution implements Solution {

    protected static final int THREADS = 8;

    @Override
    public void solve(String inputFilePath, String outputFilePath) {

        final String text = readFromFile(inputFilePath);

        final String result = solveInner(text);

        writeToFile(outputFilePath, result);
    }

    protected abstract Callable<String>[] createWorkers(final String text);

    protected String solveInner(final String text) {

        StringBuffer buffer = new StringBuffer();

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);

        final Callable<String>[] workers = createWorkers(text);

        final List<Future<String>> futures = submitWorkers(workers, executor);

        futures.forEach(future -> buffer.append(getResultFromFuture(future)));

        executor.shutdown();

        return buffer.toString();
    }

    private List<Future<String>> submitWorkers(final Callable<String>[] workers, final ExecutorService executor) {

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

    protected int getLowerKey(final int lowerBound, final int index, final int chunkSize) {

        return Math.max(lowerBound, index * chunkSize);
    }

    protected int getUpperKey(final int index, final int chunkSize, final int upperBound) {

        return Math.min((index + 1) * chunkSize, upperBound);
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
