package org.fly.tsdk.sdk.reports;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.fly.tsdk.io.DeviceHelper;
import org.fly.tsdk.sdk.TsdkApi;
import org.fly.tsdk.sdk.models.App;
import org.fly.tsdk.sdk.utils.Constants;

public class AppReport extends BaseReport {
    private static final String TAG = "AppReport";

    public AppReport(TsdkApi tsdkApi) {
        super(tsdkApi);
    }

    public void launch(@NonNull final ReportListener<App.LaunchResult> reportListener)
    {
        App.Launch appLaunch = new App.Launch();
        appLaunch.device = getDevice();
        appLaunch.property = getProperty();
        appLaunch.app_id = getSetting().getAppId();
        appLaunch.uuid = DeviceHelper.getUUID(getContext());
        appLaunch.channel = getSetting().getChannel();
        appLaunch.sub_channel = getSetting().getSubChannel();

        call(
                buildQuery(Constants.LAUNCH_URL, appLaunch)
                        .unlimitRetry() // always retry
                ,
                App.LaunchResult.class,
                reportListener
        );
    }

    public void start(@Nullable final ReportListener<App.StartResult> reportListener) {
        App.Start appStart = new App.Start();
        appStart.device = getDevice();
        appStart.property = getProperty();

        call(
                buildQuery(Constants.START_URL, appStart),
                App.StartResult.class,
                reportListener
        );
    }
}
