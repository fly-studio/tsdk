package org.fly.tsdk.query.exceptions;

import org.fly.tsdk.query.Response;

public class ResponseException extends RuntimeException {

    private int code;
    private Response response;

    private ResponseException() {
    }

    public ResponseException(String message, int code, Response response) {
        super(message);
        this.code = code;
        this.response = response;
    }

    public ResponseException(String message, int code, Response response, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.response = response;
    }

    public int getCode()
    {
        return code;
    }

    public Response getResponse() {
        return response;
    }
}

