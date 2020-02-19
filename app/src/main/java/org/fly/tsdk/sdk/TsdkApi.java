package org.fly.tsdk.sdk;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import org.fly.core.io.IoUtils;
import org.fly.tsdk.sdk.exceptions.SettingInvalidException;
import org.fly.tsdk.sdk.models.Setting;
import org.fly.tsdk.sdk.reports.AppReport;
import org.fly.tsdk.sdk.utils.DeviceHelper;
import org.fly.tsdk.sdk.io.StorageHelper;

import java.io.IOException;

import okio.Source;

public class TsdkApi {

    private static final String TAG = "Tsdk";
    public static final String VERSION = "1.0.0";

    private static TsdkApi self = null;
    private Context context;
    private Setting setting;

    private AppReport appReport;

    public static TsdkApi getInstance()
    {
        if (null == self)
            self = new TsdkApi();

        return self;
    }

    public void init(@NonNull Context context)
    {
        if (context.getApplicationContext() != context)
            throw new RuntimeException("Parameter#context must be an Application context.");

        this.context = context;

        readSetting();

        appReport = new AppReport(context, setting);

        appReport.Launch();

        Log.d(TAG, DeviceHelper.getUUID(context));
    }

    public void readSetting() {

        try  {
            Source source = StorageHelper.readAsset(context, "tsdk.json");

            String json = IoUtils.readJson(source);

            setting = Setting.fromJson(Setting.class, json);

            setting.readSubChannel(context);

        } catch (IOException e) {
            //Log.e(TAG, "Setting file invalid.", e);

            throw new SettingInvalidException(e);
        }

    }


}
