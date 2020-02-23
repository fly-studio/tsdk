package org.fly.tsdk.query.exceptions;

public class InvalidJsonException extends ResponseException {
    public InvalidJsonException(String message, int code) {
        super(message, code);
    }

    public InvalidJsonException(String message, int code, Throwable cause) {
        super(message, code, cause);
    }
}
