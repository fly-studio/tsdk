package org.fly.tsdk.query.exceptions;

import org.fly.tsdk.query.Response;

public class InvalidFiledException extends ResponseException {

    public InvalidFiledException(String message, int code, Response response) {
        super(message, code, response);
    }

    public InvalidFiledException(String message, int code, Response response, Throwable cause) {
        super(message, code, response, cause);
    }
}
