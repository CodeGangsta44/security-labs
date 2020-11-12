package edu.kpi.ip71.dovhopoliuk.caesar.worker;

import java.util.concurrent.Callable;

public class CaesarXorWorker implements Callable<String> {

    private final String text;
    private final int lowerKey;
    private final int upperKey;

    public CaesarXorWorker(final String text, final int lowerKey, final int upperKey) {

        this.text = text;
        this.lowerKey = lowerKey;
        this.upperKey = upperKey;
    }

    @Override
    public String call() {

        final StringBuilder builder = new StringBuilder();

        for (int i = lowerKey; i < upperKey; i++) {
            builder.append("\n-=======Solution for key ").append(i).append(" =======-\n");
            builder.append(solveForKey(text, i));
            builder.append('\n');
        }

        return builder.toString();
    }

    private String solveForKey(final String text, final int key) {

        char[] result = new char[text.length()];
        char[] textChars = text.toCharArray();

        for (int i = 0; i < text.length(); i++) {

            result[i] = (char) (textChars[i] ^ key);
        }

        return new String(result);
    }
}
