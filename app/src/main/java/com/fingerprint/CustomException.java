package com.fingerprint;

public class CustomException extends Exception {

    public CustomException() {
        super();
    }

    public CustomException(final String message) {
        super(message);
    }

    public CustomException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public CustomException(final Throwable cause) {
        super(cause);
    }
}