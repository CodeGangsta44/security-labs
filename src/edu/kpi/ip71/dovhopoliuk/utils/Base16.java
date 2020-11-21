package edu.kpi.ip71.dovhopoliuk.utils;

import java.math.BigInteger;

public class Base16 {
    private static final Decoder DECODER = new Decoder();
    private static final Encoder ENCODER = new Encoder();

    public static class Decoder {
        private Decoder() {
            //Deny outer construct
        }

        public String decode(String text) {
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < text.length(); i += 2) {
                char decodedChar = (char) Integer.parseInt(text.substring(i, i + 2), 16);
                builder.append(decodedChar);
            }
            return builder.toString();
        }
    }

    public static class Encoder {
        private Encoder() {
            //Deny outer construct
        }

        public String encode(String text) {
            return String.format("%x", new BigInteger(1, text.getBytes()));
        }
    }

    public static Decoder getDecoder() {
        return DECODER;
    }

    public static Encoder getEncoder() {
        return ENCODER;
    }
}
