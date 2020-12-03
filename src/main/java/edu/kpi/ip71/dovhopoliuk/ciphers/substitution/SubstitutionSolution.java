package edu.kpi.ip71.dovhopoliuk.ciphers.substitution;

import edu.kpi.ip71.dovhopoliuk.ciphers.solution.AbstractSolution;
import edu.kpi.ip71.dovhopoliuk.ciphers.substitution.worker.SubstitutionWorker;

import java.util.concurrent.Callable;
import java.util.stream.Stream;

public class SubstitutionSolution extends AbstractSolution {
    @Override
    protected Callable<String>[] createWorkers(String text) {
        return Stream.of(new SubstitutionWorker(text))
                .toArray(SubstitutionWorker[]::new);
    }
}
