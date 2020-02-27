package org.fly.tsdk.sdk.exceptions;

import android.annotation.TargetApi;

public class BindActivityException extends RuntimeException {

    public BindActivityException() {
        super();
    }

    public BindActivityException(String message) {
        super(message);
    }

    public BindActivityException(String message, Throwable cause) {
        super(message, cause);
    }

    public BindActivityException(Throwable cause) {
        super(cause);
    }

    @TargetApi(24)
    protected BindActivityException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
