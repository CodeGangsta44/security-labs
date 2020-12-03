package edu.kpi.ip71.dovhopoliuk.random.utils;

public class CasinoException extends RuntimeException {
    public CasinoException() {
    }

    public CasinoException(String message) {
        super(message);
    }

    public CasinoException(String message, Throwable cause) {
        super(message, cause);
    }

    public CasinoException(Throwable cause) {
        super(cause);
    }

    public CasinoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
