package com.example.api.sandbox.exception;

public class InternalServerException extends RuntimeException {

    private static final long serialVersionUID = -8226871115486341444L;

    public InternalServerException() {
        super();
    }

    public InternalServerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public InternalServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public InternalServerException(String message) {
        super(message);
    }

    public InternalServerException(Throwable cause) {
        super(cause);
    }
}
