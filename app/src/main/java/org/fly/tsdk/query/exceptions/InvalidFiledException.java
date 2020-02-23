package org.fly.tsdk.query.exceptions;

public class InvalidFiledException extends ResponseException {

    public InvalidFiledException(String message, int code) {
        super(message, code);
    }

    public InvalidFiledException(String message, int code, Throwable cause) {
        super(message, code, cause);
    }
}
