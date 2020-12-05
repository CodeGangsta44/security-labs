package edu.kpi.ip71.dovhopoliuk.random.generator;

public class MersenneTwisterUntemper {

    private static final int U = 11;
    private static final int B = 0x9D2C5680;
    private static final int T = 15;
    private static final int C = 0xEFC60000;
    private static final int L = 18;

    public static int untemp(final int numberToUntemp) {

        int number = numberToUntemp;
        number ^= number >>> L;
        number ^= (number << T) & C;

        int temp = number;

        for (int i = 0; i < 5; i++) {
            temp = number ^ ((temp << 7) & B);
        }

        int k = temp >>> U;
        int l = temp ^ k;
        int m = l >>> 11;

        return temp ^ m;
    }
}
