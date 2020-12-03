package edu.kpi.ip71.dovhopoliuk.random;

public class Main {
    public static void main(String[] args) {
        CasinoClient casinoClient = new CasinoClient();
        System.out.println(casinoClient.createAccount(454445));
        System.out.println(casinoClient.makeBet(PlayMode.BETTER_MT, 454445, 500, 3));
        System.out.println(casinoClient.makeBet(PlayMode.BETTER_MT, 454445, 600, 3));
    }
}
