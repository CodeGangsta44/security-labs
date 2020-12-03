package edu.kpi.ip71.dovhopoliuk.ciphers.substitution.model;

import java.util.List;
import java.util.Objects;

public class Individual {
    private List<Character> key;
    private double fitness;

    public Individual() {
    }

    public Individual(List<Character> key, double fitness) {
        this.key = key;
        this.fitness = fitness;
    }

    public void setKey(List<Character> key) {
        this.key = key;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public List<Character> getKey() {
        return key;
    }

    public double getFitness() {
        return fitness;
    }

    @Override
    public String toString() {
        return "Individual{" +
                "key=" + key +
                ", fitness=" + fitness +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Individual that = (Individual) o;
        return Double.compare(that.fitness, fitness) == 0 &&
                Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, fitness);
    }
}
