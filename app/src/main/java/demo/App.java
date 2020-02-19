package demo;

import android.app.Application;

import org.fly.tsdk.sdk.TsdkApi;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        TsdkApi.getInstance().init(this);
    }
}
