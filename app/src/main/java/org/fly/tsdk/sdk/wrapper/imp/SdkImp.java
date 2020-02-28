package org.fly.tsdk.sdk.wrapper.imp;

import android.app.Activity;
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

    abstract public void onActivityCreate(Activity activity);
    abstract public void onActivityStart(Activity activity);
    abstract public void onActivityResume(Activity activity);
    abstract public void onActivityPause(Activity activity);
    abstract public void onActivityStop(Activity activity);
    abstract public void onActivityDestroy(Activity activity);


}
