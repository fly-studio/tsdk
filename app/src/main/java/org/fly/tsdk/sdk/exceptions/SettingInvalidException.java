package org.fly.tsdk.sdk.exceptions;

import android.annotation.TargetApi;

public class SettingInvalidException extends RuntimeException {

    public SettingInvalidException() {
        super();
    }

    public SettingInvalidException(String message) {
        super(message);
    }

    public SettingInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

    public SettingInvalidException(Throwable cause) {
        super(cause);
    }

    @TargetApi(24)
    protected SettingInvalidException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
