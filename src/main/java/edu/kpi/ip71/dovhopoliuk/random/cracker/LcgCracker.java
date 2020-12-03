package edu.kpi.ip71.dovhopoliuk.random.cracker;

import edu.kpi.ip71.dovhopoliuk.random.CasinoClient;
import edu.kpi.ip71.dovhopoliuk.random.model.AccountInfo;
import edu.kpi.ip71.dovhopoliuk.random.model.BetInfo;
import edu.kpi.ip71.dovhopoliuk.random.model.PlayMode;

import java.math.BigInteger;
import java.util.Random;

public class LcgCracker implements Cracker {
    private static final long M = (long) Math.pow(2, 32);
    private final CasinoClient casinoClient = new CasinoClient();
    private final Random random = new Random();

    private long modInverse(long a, long b) {
        BigInteger aB = new BigInteger(String.valueOf(a));
        BigInteger bB = new BigInteger(String.valueOf(b));

        return aB.modInverse(bB).longValueExact();
    }

    @Override
    public void crack() {
        int playerId = random.nextInt();
        AccountInfo account = casinoClient.createAccount(playerId);

        BetInfo firstTry = casinoClient.makeBet(PlayMode.LCG, playerId, 10, 10);
        BetInfo secondTry = casinoClient.makeBet(PlayMode.LCG, playerId, 10, 10);
        BetInfo thirdTry = casinoClient.makeBet(PlayMode.LCG, playerId, 10, 10);

        long n1 = firstTry.getRealNumber();
        long n2 = secondTry.getRealNumber();
        long n3 = thirdTry.getRealNumber();

        long a = ((n3 - n2) * modInverse(n2 - n1, M)) % M;

        long c = (n2 - n1 * a) % M;

        LcgRandom lcgRandom = new LcgRandom(a, c, M, n3);

        BetInfo currentBet;
        for (int i = 0; i < 10; i++) {
            long winBet = lcgRandom.next();
            currentBet = casinoClient.makeBet(PlayMode.LCG, playerId, 500, winBet);
            System.out.println(currentBet);
        }
    }

    static class LcgRandom {
        private final long a;
        private final long c;
        private final long m;
        private long seed;

        public LcgRandom(long a, long c, long m, long seed) {
            this.a = a;
            this.c = c;
            this.m = m;
            this.seed = seed;
        }

        public long next() {
            seed = (a * seed + c) % m;
            return seed;
        }
    }
}
