package edu.kpi.ip71.dovhopoliuk.substitution.worker;

import java.util.concurrent.Callable;

public class SubstitutionWorker implements Callable<String> {
    private String text;

    public SubstitutionWorker(String text) {
        this.text = text;
    }

    @Override
    public String call() {
        return null;
    }
}
