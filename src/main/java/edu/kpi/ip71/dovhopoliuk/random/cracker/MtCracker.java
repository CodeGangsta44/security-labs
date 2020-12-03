package edu.kpi.ip71.dovhopoliuk.random.cracker;

import edu.kpi.ip71.dovhopoliuk.random.CasinoClient;
import edu.kpi.ip71.dovhopoliuk.random.generator.MersenneTwisterGenerator;
import edu.kpi.ip71.dovhopoliuk.random.model.AccountInfo;
import edu.kpi.ip71.dovhopoliuk.random.model.BetInfo;
import edu.kpi.ip71.dovhopoliuk.random.model.PlayMode;

import java.time.Instant;
import java.util.Random;
import java.util.stream.LongStream;

public class MtCracker implements Cracker {

    private static final int SEARCH_THRESHOLD = 1000;
    private static final int AMOUNT_OF_MONEY_TO_WIN = 1000000;

    private final CasinoClient casinoClient = new CasinoClient();
    private final Random random = new Random();

    @Override
    public void crack() {

        logStart();

        final int playerId = random.nextInt();

        final AccountInfo account = casinoClient.createAccount(playerId);

        final long start = Instant.now().getEpochSecond();

        final BetInfo controlBet = casinoClient.makeBet(PlayMode.MT, playerId, 10, 0);
        final long realNumber = controlBet.getRealNumber();

        final long end = Instant.now().getEpochSecond();

        LongStream.range(start - SEARCH_THRESHOLD, end + SEARCH_THRESHOLD)
                .mapToObj(MersenneTwisterGenerator::new)
                .filter(generator -> generator.getNext() == realNumber)
                .findFirst()
                .ifPresentOrElse(generator -> executeWinningSeries(generator, playerId, controlBet.getAccount().getMoney()), () -> System.out.println("No winning generator found :("));
    }

    private void executeWinningSeries(final MersenneTwisterGenerator generator, final int playerId, final long moneyOnAccount) {

        long money = moneyOnAccount;
        while (money < AMOUNT_OF_MONEY_TO_WIN) {
            var bet = casinoClient.makeBet(PlayMode.MT, playerId, money, generator.getNext());
            System.out.println(bet);
            money = bet.getAccount().getMoney();
        }
    }

    private void logStart() {

        System.out.println("\n-== Starting cracking of Mersenne Twister Generator ==-\n");
    }
}
