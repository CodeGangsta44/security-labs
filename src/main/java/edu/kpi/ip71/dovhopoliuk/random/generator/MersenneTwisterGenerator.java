package edu.kpi.ip71.dovhopoliuk.random.generator;

import java.util.stream.IntStream;

public class MersenneTwisterGenerator {

    private static final int W = 32;
    private static final int N = 624;
    private static final int M = 397;
    private static final int A = 0x9908B0DF;
    private static final int U = 11;
    private static final int S = 7;
    private static final int B = 0x9D2C5680;
    private static final int T = 15;
    private static final int C = 0xEFC60000;
    private static final int L = 18;
    private static final int F = 1812433253;

    private static final int LOWER_MASK = 0x80000000;
    private static final int UPPER_MASK = 0x7fffffff;

    private final int[] state;
    int index;

    public MersenneTwisterGenerator(final long seed) {

        state = new int[N];

        index = N;
        state[0] = Long.valueOf(seed).intValue();

        IntStream.range(1, N)
                .forEach(index -> state[index] = F * (state[index - 1] ^ (state[index - 1] >>> (W - 2))) + index);
    }

    public MersenneTwisterGenerator(final int[] state) {

        index = N;
        this.state = state;
    }

    public long getNext() {

        if (index == N) {
            twist();
        }

        int nextNumber = state[index];

        nextNumber ^= (nextNumber >>> U);
        nextNumber ^= ((nextNumber << S) & B);
        nextNumber ^= ((nextNumber << T) & C);
        nextNumber ^= (nextNumber >>> L);

        index++;

        return Integer.toUnsignedLong(nextNumber);
    }

    private void twist() {

        IntStream.range(0, N - M)
                .forEach(index -> {
                    int bits = (state[index] & LOWER_MASK) | (state[index + 1] & UPPER_MASK);
                    state[index] = state[index + M] ^ (bits >>> 1) ^ ((bits & 1) * A);
                });

        IntStream.range(N - M, N - 1)
                .forEach(index -> {
                    int bits = (state[index] & LOWER_MASK) | (state[index + 1] & UPPER_MASK);
                    state[index] = state[index - (N - M)] ^ (bits >>> 1) ^ ((bits & 1) * A);
                });

        int bits = (state[N - 1] & LOWER_MASK) | (state[0] & UPPER_MASK);
        state[N - 1] = state[M - 1] ^ (bits >>> 1) ^ ((bits & 1) * A);

        index = 0;

    }
}
