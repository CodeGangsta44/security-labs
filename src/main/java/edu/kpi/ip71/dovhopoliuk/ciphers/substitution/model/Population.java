package edu.kpi.ip71.dovhopoliuk.ciphers.substitution.model;

import java.util.List;
import java.util.Objects;

public class Population {
    private List<Individual> individuals;

    public Population(List<Individual> individuals) {
        this.individuals = individuals;
    }

    public List<Individual> getIndividuals() {
        return individuals;
    }

    public void setIndividuals(List<Individual> individuals) {
        this.individuals = individuals;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Population that = (Population) o;
        return Objects.equals(individuals, that.individuals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(individuals);
    }

    @Override
    public String toString() {
        return "Population{" +
                "individuals=" + individuals +
                '}';
    }
}

