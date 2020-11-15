package edu.kpi.ip71.dovhopoliuk.vigenere;

import edu.kpi.ip71.dovhopoliuk.solution.AbstractSolution;
import edu.kpi.ip71.dovhopoliuk.vigenere.worker.VigenereXorWorker;

import java.util.concurrent.Callable;
import java.util.stream.Stream;

public class VigenereXorSolution extends AbstractSolution {

    @Override
    protected Callable<String>[] createWorkers(final String text) {

        return Stream.of(new VigenereXorWorker(text))
                .toArray(VigenereXorWorker[]::new);
    }
}
