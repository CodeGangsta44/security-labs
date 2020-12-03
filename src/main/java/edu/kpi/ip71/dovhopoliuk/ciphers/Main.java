package edu.kpi.ip71.dovhopoliuk.ciphers;

import edu.kpi.ip71.dovhopoliuk.ciphers.polyalphabetic.PolyalphabeticSolution;

public class Main {

    public static void main(final String... args) {

//        new RailFenceSolution().solve("resources/ciphers/railfence/input.txt", "resources/ciphers/railfence/output.txt");
//        new CaesarXorSolution().solve("resources/ciphers/caesar/input.txt", "resources/ciphers/caesar/output.txt");
//        new VigenereXorSolution().solve("resources/ciphers/vigenere/input.txt", "resources/ciphers/vigenere/output.txt");
//        new SubstitutionSolution().solve("resources/ciphers/substitution/input.txt", "resources/ciphers/substitution/output.txt");
        new PolyalphabeticSolution().solve("resources/ciphers/polyalphabetic/input.txt", "resources/ciphers/polyalphabetic/output.txt");
    }
}
