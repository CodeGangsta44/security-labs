package edu.kpi.ip71.dovhopoliuk.railfence;

import edu.kpi.ip71.dovhopoliuk.railfence.worker.RailFenceWorker;
import edu.kpi.ip71.dovhopoliuk.solution.AbstractSolution;

import java.util.concurrent.Callable;
import java.util.stream.IntStream;

public class RailFenceSolution extends AbstractSolution {

    @Override
    protected Callable<String>[] createWorkers(final String text) {

        final int chunkSize = (int) Math.ceil((double) text.length() / (THREADS));

        return IntStream.range(0, THREADS)
                .mapToObj(index -> new RailFenceWorker(text, getLowerKey(2, index, chunkSize), getUpperKey(index, chunkSize, text.length())))
                .toArray(RailFenceWorker[]::new);
    }
}
