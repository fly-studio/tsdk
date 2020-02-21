package org.fly.tsdk.sdk.reports;

import androidx.annotation.NonNull;

import org.fly.tsdk.sdk.imp.SdkApi;
import org.fly.tsdk.sdk.models.App;
import org.fly.tsdk.sdk.utils.Constants;
import org.fly.tsdk.sdk.utils.DeviceHelper;

public class AppReport extends BaseReport {
    private static final String TAG = "AppReport";

    public AppReport(SdkApi sdkApi) {
        super(sdkApi);
    }

    public void launch(@NonNull final ReportListener<App.LaunchResult> reportListener)
    {
        App.Launch appLaunch = new App.Launch();
        appLaunch.device = getDevice();
        appLaunch.property = getProperty();
        appLaunch.app_id = getSetting().getAppId();
        appLaunch.uuid = DeviceHelper.getUUID(getContext());
        appLaunch.sub_channel = getSetting().getSubChannel();

        post(buildUrl(Constants.LAUNCH_URL), appLaunch, App.LaunchResult.class, reportListener);

    }
}
