package edu.kpi.ip71.dovhopoliuk.polyalphabetic.population;

import edu.kpi.ip71.dovhopoliuk.substitution.model.Individual;
import edu.kpi.ip71.dovhopoliuk.substitution.model.Population;

import java.util.List;

public class PolyalphabeticPopulation extends Population {

    private int keyIndex;
    private List<List<Character>> keys;

    public PolyalphabeticPopulation(int keyIndex, List<Individual> individuals) {
        super(individuals);
        this.keyIndex = keyIndex;
    }

    public int getKeyIndex() {
        return keyIndex;
    }

    public void setKeyIndex(int keyIndex) {
        this.keyIndex = keyIndex;
    }

    public List<List<Character>> getKeys() {
        return keys;
    }

    public void setKeys(List<List<Character>> keys) {
        this.keys = keys;
    }
}
