package org.fly.tsdk.sdk.wrapper;

import android.app.Application;

import org.fly.tsdk.sdk.TsdkApi;

public class TsdkApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        TsdkApi.getInstance().init(this);

    }
}
