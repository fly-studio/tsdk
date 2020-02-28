package org.fly.tsdk.query.exceptions;

import org.fly.tsdk.query.Response;

public class InvalidJsonException extends ResponseException {
    public InvalidJsonException(String message, int code, Response response) {
        super(message, code, response);
    }

    public InvalidJsonException(String message, int code, Response response, Throwable cause) {
        super(message, code, response, cause);
    }
}
