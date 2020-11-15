package edu.kpi.ip71.dovhopoliuk;

import edu.kpi.ip71.dovhopoliuk.caesar.CaesarXorSolution;
import edu.kpi.ip71.dovhopoliuk.railfence.RailFenceSolution;
import edu.kpi.ip71.dovhopoliuk.vigenere.VigenereXorSolution;

public class Main {

    public static void main(final String... args) {

//        new RailFenceSolution().solve("resources/railfence/input.txt", "resources/railfence/output.txt");
//        new CaesarXorSolution().solve("resources/caesar/input.txt", "resources/caesar/output.txt");
        new VigenereXorSolution().solve("resources/vigenere/input.txt", "resources/vigenere/output.txt");
    }
}
