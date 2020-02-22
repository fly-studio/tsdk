package org.fly.tsdk.sdk;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.OnLifecycleEvent;

import org.apache.commons.lang3.StringUtils;
import org.fly.core.function.FunctionUtils;
import org.fly.core.io.IoUtils;
import org.fly.tsdk.sdk.exceptions.BindMainActivityException;
import org.fly.tsdk.sdk.exceptions.InvalidSettingException;
import org.fly.tsdk.sdk.imp.InitSdkListener;
import org.fly.tsdk.sdk.imp.ReportFragment;
import org.fly.tsdk.sdk.imp.wrapper.SdkImp;
import org.fly.tsdk.sdk.io.StorageHelper;
import org.fly.tsdk.sdk.models.App;
import org.fly.tsdk.sdk.models.Setting;
import org.fly.tsdk.sdk.query.Query;
import org.fly.tsdk.sdk.query.exceptions.ResponseException;
import org.fly.tsdk.sdk.reports.AppReport;
import org.fly.tsdk.sdk.reports.ReportListener;

import java.io.IOException;

import okio.Source;

final public class TsdkApi implements LifecycleObserver, LifecycleOwner {

    private static final String TAG = "Tsdk";
    public static final String VERSION = "1.0.0";
    private static TsdkApi self = null;

    private Query query;
    private String alid;
    private Context context;
    private Setting setting;
    private LifecycleRegistry mLifecycleRegistry;
    private Activity mainActivity;
    private SdkImp sdkImp;
    private INIT_RESULT initResult = INIT_RESULT.PREPARING;

    private AppReport appReport;

    public enum INIT_RESULT {
        PREPARING,
        SUCCESS,
        FAIL
    }

    private TsdkApi() {

    }

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
        this.setQuery(new Query());

        // read Setting
        this.setSetting(readSetting());

        // Application's observe

        try {
            Class clazz = Class.forName(this.getClass().getPackage().getName() + ".imp.wrapper." + StringUtils.capitalize(getSetting().getChannel()) + "Imp");
            sdkImp = (SdkImp) FunctionUtils.newInstance(clazz, this);

        } catch (ClassNotFoundException e) {
            //Imp 不存在
            throw new RuntimeException(e);
        }

        // report app
        appReport = new AppReport(this);

        appReport.launch(new ReportListener<App.LaunchResult>() {
            @Override
            public void callback(App.LaunchResult launchResult, ResponseException e) {

                if (e != null) // 网络出现问题，或者上报字段有误
                {
                    Log.e(TAG, "SDK init failed: " + e.getMessage() + ", Code:" + e.getCode(), e);
                    initResult = INIT_RESULT.FAIL;

                    if (null != initSdkListener)
                        initSdkListener.callback(INIT_RESULT.FAIL, e.getMessage());

                } else {
                    //重置为线上的public key
                    getQuery().setPublicKey(launchResult.getPublicKey());
                    //设置alid
                    setAlid(launchResult.getAlid());

                    Log.d(TAG, "Tsdk init success.");

                    initResult = INIT_RESULT.SUCCESS;

                    if (null != initSdkListener)
                        initSdkListener.callback(INIT_RESULT.SUCCESS, null);

                }
            }
        });

    }

    public boolean isInit()
    {
        return initResult == INIT_RESULT.SUCCESS;
    }

    public Context getContext() {
        return context;
    }

    public Setting getSetting() {
        return setting;
    }

    public String getAlid() {
        return alid;
    }

    public Query getQuery() {
        return query;
    }

    private void setContext(Context context) {
        this.context = context;
    }

    private void setSetting(Setting setting) {
        this.setting = setting;
    }

    private void setAlid(String alid) {
        this.alid = alid;
    }

    private void setQuery(Query query) {
        this.query = query;
    }

    protected void setSdkImp(SdkImp sdkImp) {
        this.sdkImp = sdkImp;
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return mLifecycleRegistry;
    }

    public Activity getMainActivity()
    {
        return mainActivity;
    }

    public void bindMainActivity(Activity activity)
    {
        if (mainActivity != null)
            throw new BindMainActivityException("SDK can bound ONE MainActivity. or unbind first.");

        mainActivity = activity;

        ReportFragment.injectIfNeededIn(activity, this);

        if (mLifecycleRegistry == null)
        {
            mLifecycleRegistry = new LifecycleRegistry(this);
            mLifecycleRegistry.addObserver(this);
        }

        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);

    }

    public void unbindMainActivity()
    {
        if (mainActivity != null)
        {
            ReportFragment.removeFrom(mainActivity);
            mainActivity = null;
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate() {
        Log.d(TAG, getMainActivity().getLocalClassName() + ".onCreate");


        sdkImp.onCreate();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        Log.d(TAG, getMainActivity().getLocalClassName() + ".onStart");

        sdkImp.onStart();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        Log.d(TAG, getMainActivity().getLocalClassName() + ".onResume");

        appReport.start(null);

        sdkImp.onResume();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        Log.d(TAG, getMainActivity().getLocalClassName() + ".onPause");

        sdkImp.onPause();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        Log.d(TAG, getMainActivity().getLocalClassName() + ".onStop");

        sdkImp.onStop();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        Log.d(TAG, getMainActivity().getLocalClassName() + ".onDestroy");

        sdkImp.onDestroy();
    }

    public void login() {
        sdkImp.login();
    }

    public void logout() {
        sdkImp.logout();
    }

    public void pay() {
        sdkImp.pay();
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

    private void waitForInit() {
        if (initResult == INIT_RESULT.PREPARING)
        {

        } else {

        }
    }

}
