package org.fly.tsdk.sdk.exceptions;

import android.annotation.TargetApi;

public class InvalidSettingException extends RuntimeException {

    public InvalidSettingException() {
        super();
    }

    public InvalidSettingException(String message) {
        super(message);
    }

    public InvalidSettingException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidSettingException(Throwable cause) {
        super(cause);
    }

    @TargetApi(24)
    protected InvalidSettingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
