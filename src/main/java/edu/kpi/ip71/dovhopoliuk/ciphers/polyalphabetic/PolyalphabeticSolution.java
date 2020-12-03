package edu.kpi.ip71.dovhopoliuk.ciphers.polyalphabetic;

import edu.kpi.ip71.dovhopoliuk.ciphers.polyalphabetic.worker.PolyalphabeticWorker;
import edu.kpi.ip71.dovhopoliuk.ciphers.solution.AbstractSolution;

import java.util.concurrent.Callable;
import java.util.stream.Stream;

public class PolyalphabeticSolution extends AbstractSolution {
    @Override
    protected Callable<String>[] createWorkers(String text) {
        return Stream.of(new PolyalphabeticWorker(text))
                .toArray(PolyalphabeticWorker[]::new);
    }
}
