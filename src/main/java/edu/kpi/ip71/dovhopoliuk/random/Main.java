package edu.kpi.ip71.dovhopoliuk.random;

import edu.kpi.ip71.dovhopoliuk.random.cracker.LcgCracker;
import edu.kpi.ip71.dovhopoliuk.random.cracker.MtCracker;

public class Main {

    public static void main(String[] args) {

        new LcgCracker().crack();
        new MtCracker().crack();
    }
}
