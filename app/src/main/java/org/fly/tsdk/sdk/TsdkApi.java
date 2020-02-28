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
import org.fly.tsdk.sdk.exceptions.BindActivityException;
import org.fly.tsdk.sdk.exceptions.InvalidSettingException;
import org.fly.tsdk.sdk.models.Setting;
import org.fly.tsdk.sdk.reports.ReportManager;
import org.fly.tsdk.sdk.utils.RunEnvironmentCheck;
import org.fly.tsdk.sdk.view.LoadingDialogFragment;
import org.fly.tsdk.sdk.view.ReportFragment;
import org.fly.tsdk.sdk.wrapper.bean.Eventer;
import org.fly.tsdk.sdk.wrapper.imp.SdkImp;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

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
    private ReportManager reportManager;

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
        setContext(context);

        // check
        RunEnvironmentCheck.checkClasses();
        RunEnvironmentCheck.checkResoure(context);
        RunEnvironmentCheck.checkPermissionsInManifest(context);

        // read Setting
        readSetting(context);

        reportManager = new ReportManager(this);

        // Application's observer

        // Call Imp
        try {
            Class clazz = Class.forName(getClass().getPackage().getName() + ".wrapper.imp." + StringUtils.capitalize(getSetting().getChannel()) + "Imp");
            sdkImp = (SdkImp) FunctionUtils.newInstance(clazz, this);
        } catch (ClassNotFoundException e) {
            //Imp 不存在
            throw new RuntimeException(e);
        }

        sdkImp.init(context);

        Logger.d(TAG, "Tsdk init success.");

        // report app
        reportManager.launch();
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

    public ReportManager getReportManager() {
        return reportManager;
    }

    private void setContext(Context context) {
        this.context = context;
    }

    private void setSetting(Setting setting) {
        this.setting = setting;
    }

    public synchronized void setAlid(String alid) {
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

        sdkImp.onActivityCreate(getMainActivity());
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onActivityStart() {
        Logger.d(TAG, getMainActivity().getLocalClassName() + ".onStart");

        sdkImp.onActivityStart(getMainActivity());
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onActivityResume() {
        Logger.d(TAG, getMainActivity().getLocalClassName() + ".onResume");

        reportManager.start();

        sdkImp.onActivityResume(getMainActivity());
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onActivityPause() {
        Logger.d(TAG, getMainActivity().getLocalClassName() + ".onPause");

        sdkImp.onActivityPause(getMainActivity());
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onActivityStop() {
        Logger.d(TAG, getMainActivity().getLocalClassName() + ".onStop");

        sdkImp.onActivityStop(getMainActivity());
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onActivityDestroy() {
        Logger.d(TAG, getMainActivity().getLocalClassName() + ".onDestroy");

        if (null != loadingDialogFragment)
            loadingDialogFragment.dettachFromActivity();

        EventBus.getDefault().unregister(this);

        sdkImp.onActivityDestroy(getMainActivity());
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

    private void readSetting(Context context) {

        try  {
            Source source = ResourceHelper.readAsset(context, "tsdk.json");

            String json = IoUtils.readJson(source);

            Setting setting = Setting.fromJson(Setting.class, json);

            setting.readSubChannel(context);

            setSetting(setting);

        } catch (IOException e) {
            //Crash
            throw new InvalidSettingException(e);
        }

    }

}
