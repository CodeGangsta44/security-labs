package edu.kpi.ip71.dovhopoliuk.random;

import edu.kpi.ip71.dovhopoliuk.random.cracker.Cracker;
import edu.kpi.ip71.dovhopoliuk.random.cracker.LcgCracker;

public class Main {
    public static void main(String[] args) {
        Cracker lcgCracker = new LcgCracker();
        lcgCracker.crack();
    }
}
