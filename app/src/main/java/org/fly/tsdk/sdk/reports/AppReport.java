package org.fly.tsdk.sdk.reports;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.fly.core.annotation.NotProguard;
import org.fly.tsdk.io.DeviceHelper;
import org.fly.tsdk.sdk.models.App;
import org.fly.tsdk.sdk.utils.Constants;

@NotProguard
public class AppReport extends BaseReport {
    private static final String TAG = "AppReport";

    public static void launch(@NonNull final ReportListener<App.LaunchResult> reportListener)
    {
        App.Launch appLaunch = new App.Launch();
        appLaunch.device = getDevice();
        appLaunch.property = getProperty();
        appLaunch.app_id = getSetting().getAppId();
        appLaunch.uuid = DeviceHelper.getUUID(getContext());
        appLaunch.channel = getSetting().getChannel();
        appLaunch.sub_channel = getSetting().getSubChannel();

        post(Constants.LAUNCH_URL, appLaunch, App.LaunchResult.class, reportListener)
            .unlimitRetry() // always retry
            .execute();
    }

    public static void start(@Nullable final ReportListener<App.StartResult> reportListener) {
        App.Start appStart = new App.Start();
        appStart.device = getDevice();
        appStart.property = getProperty();

        post(Constants.START_URL, appStart, App.StartResult.class, reportListener)
            .execute();
    }
}
