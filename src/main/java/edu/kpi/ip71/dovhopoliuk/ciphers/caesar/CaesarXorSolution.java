package edu.kpi.ip71.dovhopoliuk.ciphers.caesar;

import edu.kpi.ip71.dovhopoliuk.ciphers.caesar.worker.CaesarXorWorker;
import edu.kpi.ip71.dovhopoliuk.ciphers.solution.AbstractSolution;

import java.util.concurrent.Callable;
import java.util.stream.IntStream;

public class CaesarXorSolution extends AbstractSolution {

    private static final int BITS_IN_KEY = 1;
    private static final int BITS_IN_BYTE = 8;

    @Override
    protected Callable<String>[] createWorkers(final String text) {

        final int upperBound = (int) Math.pow(2, BITS_IN_KEY * BITS_IN_BYTE);
        final int chunkSize = (int) Math.ceil((double) upperBound / (THREADS));

        return IntStream.range(0, THREADS)
                .mapToObj(index -> new CaesarXorWorker(text, getLowerKey(0, index, chunkSize), getUpperKey(index, chunkSize, text.length())))
                .toArray(CaesarXorWorker[]::new);
    }
}
