package edu.kpi.ip71.dovhopoliuk.random.cracker;

import edu.kpi.ip71.dovhopoliuk.random.CasinoClient;
import edu.kpi.ip71.dovhopoliuk.random.generator.MersenneTwisterGenerator;
import edu.kpi.ip71.dovhopoliuk.random.generator.MersenneTwisterUntemper;
import edu.kpi.ip71.dovhopoliuk.random.model.AccountInfo;
import edu.kpi.ip71.dovhopoliuk.random.model.PlayMode;

import java.util.Random;
import java.util.stream.IntStream;

public class BetterMtCracker implements Cracker {

    private static final int AMOUNT_OF_MONEY_TO_WIN = 1000000;
    private static final int REGISTER_CAPACITY = 624;

    private final CasinoClient casinoClient = new CasinoClient();
    private final Random random = new Random();

    @Override
    public void crack() {
        logStart();

        final int playerId = random.nextInt();

        final AccountInfo account = casinoClient.createAccount(playerId);

        int[] generatorState = IntStream.range(0, REGISTER_CAPACITY)
                .map(index -> {
                    logProgress(index);
                    return MersenneTwisterUntemper.untemp((int) casinoClient.makeBet(PlayMode.BETTER_MT, playerId, 1, 0).getRealNumber());
                })
                .toArray();

        logFinishedBuildingOfState();

        var cloneGenerator = new MersenneTwisterGenerator(generatorState);

        executeWinningSeries(cloneGenerator, playerId, account.getMoney() - REGISTER_CAPACITY);
    }

    private void executeWinningSeries(final MersenneTwisterGenerator generator, final int playerId, final long moneyOnAccount) {

        long money = moneyOnAccount;
        while (money < AMOUNT_OF_MONEY_TO_WIN) {
            var bet = casinoClient.makeBet(PlayMode.BETTER_MT, playerId, money, generator.getNext());
            System.out.println(bet);
            money = bet.getAccount().getMoney();
        }
    }

    private void logStart() {

        System.out.println("\n-== Starting cracking of Better Mersenne Twister Generator ==-\n");
    }

    private void logProgress(final int index) {

        int chunkSize = (REGISTER_CAPACITY / 10) + 1;

        if (index % chunkSize == 0) {
            System.out.print((index / chunkSize) * 10 + "%...");
        }

    }

    private void logFinishedBuildingOfState() {

        System.out.println("100%");
    }
}
