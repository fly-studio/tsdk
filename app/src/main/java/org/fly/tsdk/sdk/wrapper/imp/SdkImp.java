package org.fly.tsdk.sdk.wrapper.imp;

import android.content.Context;

import org.fly.tsdk.sdk.TsdkApi;

public abstract class SdkImp {

    protected TsdkApi tsdkApi;

    public SdkImp(TsdkApi tsdkApi)
    {
        this.tsdkApi = tsdkApi;
    }

    public TsdkApi getTsdkApi() {
        return tsdkApi;
    }

    abstract public void init(Context context);

    abstract public void login();
    abstract public void logout();
    abstract public void pay();
    abstract public void exitApp();

    abstract public void onCreate();
    abstract public void onStart();
    abstract public void onResume();
    abstract public void onPause();
    abstract public void onStop();
    abstract public void onDestroy();


}
