package edu.kpi.ip71.dovhopoliuk.ciphers.vigenere;

import edu.kpi.ip71.dovhopoliuk.ciphers.solution.AbstractSolution;
import edu.kpi.ip71.dovhopoliuk.ciphers.vigenere.worker.VigenereXorWorker;

import java.util.concurrent.Callable;
import java.util.stream.Stream;

public class VigenereXorSolution extends AbstractSolution {

    @Override
    protected Callable<String>[] createWorkers(final String text) {

        return Stream.of(new VigenereXorWorker(text))
                .toArray(VigenereXorWorker[]::new);
    }
}
