package com.github.t1.rest;

public class HttpTimeoutException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public HttpTimeoutException() {
        super();
    }

    public HttpTimeoutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public HttpTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpTimeoutException(String message) {
        super(message);
    }

    public HttpTimeoutException(Throwable cause) {
        super(cause);
    }
}
