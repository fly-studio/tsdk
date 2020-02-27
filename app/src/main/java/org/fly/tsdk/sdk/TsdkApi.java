package org.fly.tsdk.sdk;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.OnLifecycleEvent;

import org.apache.commons.lang3.StringUtils;
import org.fly.core.function.FunctionUtils;
import org.fly.core.io.IoUtils;
import org.fly.tsdk.io.DeviceHelper;
import org.fly.tsdk.io.Logger;
import org.fly.tsdk.io.ResourceHelper;
import org.fly.tsdk.query.Query;
import org.fly.tsdk.query.exceptions.ResponseException;
import org.fly.tsdk.sdk.exceptions.BindActivityException;
import org.fly.tsdk.sdk.exceptions.InvalidSettingException;
import org.fly.tsdk.sdk.models.App;
import org.fly.tsdk.sdk.models.ReportResult;
import org.fly.tsdk.sdk.models.Setting;
import org.fly.tsdk.sdk.reports.AppReport;
import org.fly.tsdk.sdk.reports.ReportListener;
import org.fly.tsdk.sdk.utils.RunEnvironmentCheck;
import org.fly.tsdk.sdk.view.LoadingDialogFragment;
import org.fly.tsdk.sdk.view.ReportFragment;
import org.fly.tsdk.sdk.wrapper.bean.Eventer;
import org.fly.tsdk.sdk.wrapper.imp.SdkImp;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okio.Source;

final public class TsdkApi implements LifecycleObserver, LifecycleOwner {

    private static final String TAG = "Tsdk";
    public static final String VERSION = "1.0.0";
    private static TsdkApi self = null;

    private String alid;
    private Context context;
    private Setting setting;
    private LifecycleRegistry mLifecycleRegistry;
    private Activity mainActivity;
    private SdkImp sdkImp;

    private LoadingDialogFragment loadingDialogFragment;


    private TsdkApi() {

    }

    public synchronized static TsdkApi getInstance()
    {
        if (null == self)
            self = new TsdkApi();

        return self;
    }

    public void init(@NonNull Context context)
    {
        if (context.getApplicationContext() != context)
            throw new RuntimeException("Parameter#context must be an Application context.");

        // set Application's context
        this.setContext(context);

        // check
        RunEnvironmentCheck.checkClasses();
        RunEnvironmentCheck.checkResoure(context);
        RunEnvironmentCheck.checkPermissionsInManifest(context);

        // read Setting
        this.setSetting(readSetting());

        //Register EventBus

        // Application's observer

        // Call Imp
        try {
            Class clazz = Class.forName(this.getClass().getPackage().getName() + ".wrapper.imp." + StringUtils.capitalize(getSetting().getChannel()) + "Imp");
            sdkImp = (SdkImp) FunctionUtils.newInstance(clazz, this);
        } catch (ClassNotFoundException e) {
            //Imp 不存在
            throw new RuntimeException(e);
        }

        sdkImp.init(context);

        Logger.d(TAG, "Tsdk init success.");

        // report app
        reportLaunch();
    }

    private void reportLaunch(){

        EventBus.getDefault().postSticky(Eventer.create(Eventer.TYPE.INIT, ResourceHelper.getString(getContext(), "tsdk_init")));

        AppReport.launch(new ReportListener<App.LaunchResult>() {
            @Override
            public void callback(App.LaunchResult launchResult, ResponseException e) {

                if (e != null) // 上报字段有误
                {
                    Logger.e(TAG, "App.Launch failed: " + e.getMessage() + ", Code:" + e.getCode(), e);

                    EventBus.getDefault().postSticky(Eventer.create(Eventer.TYPE.INIT_FAIL, ResourceHelper.getString(getContext(), "tsdk_init_success")));

                } else {
                    //重置为线上的public key
                    Query.setPublicKey(launchResult.getPublicKey());
                    //设置alid
                    setAlid(launchResult.getAlid());

                    Logger.e(TAG, "App.Launch success: " + getAlid());

                    EventBus.getDefault().postSticky(Eventer.create(Eventer.TYPE.INIT_SUCCESS, ResourceHelper.getString(getContext(), "tsdk_init_fail")));

                }
            }
        });
    }

    public Context getContext() {
        return context;
    }

    public Setting getSetting() {
        return setting;
    }

    public synchronized String getAlid() {
        return alid;
    }

    private void setContext(Context context) {
        this.context = context;
    }

    private void setSetting(Setting setting) {
        this.setting = setting;
    }

    private synchronized void setAlid(String alid) {
        this.alid = alid;
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
            throw new BindActivityException("SDK can bound ONE MainActivity. or unbind first.");

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
    public void onActivityCreate() {
        Logger.d(TAG, getMainActivity().getLocalClassName() + ".onCreate");
        DeviceHelper.requirePermissions(getMainActivity(), RunEnvironmentCheck.PERMISSIONS);
        loadingDialogFragment = LoadingDialogFragment.getInstance(getMainActivity());

        EventBus.getDefault().register(this);

        sdkImp.onCreate();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onActivityStart() {
        Logger.d(TAG, getMainActivity().getLocalClassName() + ".onStart");

        sdkImp.onStart();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onActivityResume() {
        Logger.d(TAG, getMainActivity().getLocalClassName() + ".onResume");

        AppReport.start(null);

        sdkImp.onResume();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onActivityPause() {
        Logger.d(TAG, getMainActivity().getLocalClassName() + ".onPause");

        sdkImp.onPause();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onActivityStop() {
        Logger.d(TAG, getMainActivity().getLocalClassName() + ".onStop");

        sdkImp.onStop();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onActivityDestroy() {
        Logger.d(TAG, getMainActivity().getLocalClassName() + ".onDestroy");

        if (null != loadingDialogFragment)
            loadingDialogFragment.dettachFromActivity();

        EventBus.getDefault().unregister(this);

        sdkImp.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onActivityEvent(Eventer eventer)
    {
        loadingDialogFragment.setMessage(eventer.getMessage());
        switch (eventer.getType())
        {
            case INIT:
                loadingDialogFragment.show(getMainActivity());
                break;
            case INIT_FAIL:
                loadingDialogFragment.show(getMainActivity());
                break;
            case INIT_SUCCESS:
                loadingDialogFragment.hide();
                break;
            case LOADING:
                break;
            case LOADED:
                break;
        }
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
            Source source = ResourceHelper.readAsset(getContext(), "tsdk.json");

            String json = IoUtils.readJson(source);

            Setting setting = Setting.fromJson(Setting.class, json);

            setting.readSubChannel(getContext());

            return setting;

        } catch (IOException e) {
            //Crash
            throw new InvalidSettingException(e);
        }

    }

}
