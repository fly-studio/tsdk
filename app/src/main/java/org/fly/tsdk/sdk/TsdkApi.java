package org.fly.tsdk.sdk;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.fly.core.io.IoUtils;
import org.fly.tsdk.sdk.exceptions.InvalidSettingException;
import org.fly.tsdk.sdk.imp.InitSdkListener;
import org.fly.tsdk.sdk.imp.SdkApi;
import org.fly.tsdk.sdk.io.StorageHelper;
import org.fly.tsdk.sdk.models.App;
import org.fly.tsdk.sdk.models.Setting;
import org.fly.tsdk.sdk.query.Query;
import org.fly.tsdk.sdk.query.exceptions.ResponseException;
import org.fly.tsdk.sdk.reports.AppReport;
import org.fly.tsdk.sdk.reports.ReportListener;

import java.io.IOException;

import okio.Source;

final public class TsdkApi extends SdkApi {

    private static final String TAG = "Tsdk";
    public static final String VERSION = "1.0.0";
    private static TsdkApi self = null;

    private AppReport appReport;

    public enum INIT_RESULT {
        SUCCESS,
        FAIL
    }

    private TsdkApi() {}

    public static TsdkApi getInstance()
    {
        if (null == self)
            self = new TsdkApi();

        return self;
    }

    public void init(@NonNull Context context)
    {
        init(context, null);
    }

    public void init(@NonNull Context context, @Nullable final InitSdkListener initSdkListener)
    {
        if (context.getApplicationContext() != context)
            throw new RuntimeException("Parameter#context must be an Application context.");

        // set Application's context
        this.setContext(context);

        // new a Query
        this.setQuery(createQuery());

        // read Setting
        this.setSetting(readSetting());

        // Application's observe


        // report app
        appReport = new AppReport(this);

        appReport.launch(new ReportListener<App.LaunchResult>() {
            @Override
            public void callback(App.LaunchResult launchResult, ResponseException e) {

                if (e != null) // 网络出现问题，或者上报字段有误
                {
                    Log.e(TAG, "SDK init failed: " + e.getMessage() + ", Code:" + e.getCode(), e);

                    if (null != initSdkListener)
                        initSdkListener.callback(INIT_RESULT.FAIL, e.getMessage());

                } else {
                    //重置为线上的public key
                    getQuery().setPublicKey(launchResult.getPublicKey());
                    //配置setting's channel
                    getSetting().setChannel(launchResult.getChannel());
                    //设置alid
                    setAlid(launchResult.getAlid());

                    Log.d(TAG, "Tsdk init success.");

                    if (null != initSdkListener)
                        initSdkListener.callback(INIT_RESULT.SUCCESS, null);

                }

            }
        });

    }

    private Setting readSetting() {

        try  {
            Source source = StorageHelper.readAsset(getContext(), "tsdk.json");

            String json = IoUtils.readJson(source);

            Setting setting = Setting.fromJson(Setting.class, json);

            setting.readSubChannel(getContext());

            return setting;

        } catch (IOException e) {
            //Crash
            throw new InvalidSettingException(e);
        }

    }

    private Query createQuery()
    {
        return new Query();
    }


}
