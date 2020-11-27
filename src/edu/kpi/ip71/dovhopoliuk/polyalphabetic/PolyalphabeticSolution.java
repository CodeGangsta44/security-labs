package edu.kpi.ip71.dovhopoliuk.polyalphabetic;

import edu.kpi.ip71.dovhopoliuk.polyalphabetic.worker.PolyalphabeticWorker;
import edu.kpi.ip71.dovhopoliuk.solution.AbstractSolution;

import java.util.concurrent.Callable;
import java.util.stream.Stream;

public class PolyalphabeticSolution extends AbstractSolution {
    @Override
    protected Callable<String>[] createWorkers(String text) {
        return Stream.of(new PolyalphabeticWorker(text), new PolyalphabeticWorker(text), new PolyalphabeticWorker(text),
                new PolyalphabeticWorker(text), new PolyalphabeticWorker(text), new PolyalphabeticWorker(text),
                new PolyalphabeticWorker(text), new PolyalphabeticWorker(text))
                .toArray(PolyalphabeticWorker[]::new);
    }
}
