package org.fly.tsdk.sdk.exceptions;

import android.annotation.TargetApi;

public class BindMainActivityException extends RuntimeException {

    public BindMainActivityException() {
        super();
    }

    public BindMainActivityException(String message) {
        super(message);
    }

    public BindMainActivityException(String message, Throwable cause) {
        super(message, cause);
    }

    public BindMainActivityException(Throwable cause) {
        super(cause);
    }

    @TargetApi(24)
    protected BindMainActivityException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
