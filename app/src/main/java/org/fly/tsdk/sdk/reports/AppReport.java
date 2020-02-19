package org.fly.tsdk.sdk.reports;

import android.content.Context;

import org.fly.tsdk.sdk.models.App;
import org.fly.tsdk.sdk.models.Setting;

public class AppReport extends BaseReport {

    public AppReport(Context context, Setting setting) {
        super(context, setting);
    }

    public void Launch()
    {
        App.Launch appLaunch = new App.Launch();
        appLaunch.device = getDevice();
        appLaunch.property = getProperty();
        appLaunch.app_id = setting.getAppId();

    }
}
