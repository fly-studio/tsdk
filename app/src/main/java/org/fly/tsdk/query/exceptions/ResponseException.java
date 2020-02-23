package org.fly.tsdk.query.exceptions;

public class ResponseException extends RuntimeException {

    private int code;

    private ResponseException() {
    }

    public ResponseException(String message, int code) {
        super(message);
        this.code = code;

    }

    public ResponseException(String message, int code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode()
    {
        return code;
    }
}

