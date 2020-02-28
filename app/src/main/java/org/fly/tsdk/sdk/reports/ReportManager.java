package org.fly.tsdk.sdk.reports;

import android.content.Context;

import org.fly.tsdk.io.Logger;
import org.fly.tsdk.io.ResourceHelper;
import org.fly.tsdk.query.Query;
import org.fly.tsdk.query.exceptions.ResponseException;
import org.fly.tsdk.sdk.TsdkApi;
import org.fly.tsdk.sdk.models.App;
import org.fly.tsdk.sdk.models.Setting;
import org.fly.tsdk.sdk.wrapper.bean.Eventer;
import org.fly.tsdk.task.Task;
import org.fly.tsdk.task.TaskCallback;
import org.fly.tsdk.task.TaskExecutor;
import org.fly.tsdk.task.TaskManager;
import org.greenrobot.eventbus.EventBus;

import java.util.Map;

final public class ReportManager {

    private static final String TAG = "ReportManager";

    private TaskManager taskManager;
    private TsdkApi tsdkApi;
    private AppReport appReport;

    public ReportManager(TsdkApi tsdkApi) {
        this.tsdkApi = tsdkApi;
        taskManager = new TaskManager();
    }

    private Context getContext() {
        return tsdkApi.getContext();
    }

    private Setting getSetting() {
        return tsdkApi.getSetting();
    }

    public void launch()
    {
        taskManager.execute(
                new Task.Builder(new TaskExecutor() {
                    @Override
                    public void call(Map<String, Object> dependencyResults, TaskCallback completeCallback) {
                        doLaunch(dependencyResults, completeCallback);
                    }
                })
                        .name(App.Launch.class.getName())
                        .build()
        );
    }

    public void start()
    {
        taskManager.execute(
                new Task.Builder(new TaskExecutor() {
                    @Override
                    public void call(Map<String, Object> dependencyResults, TaskCallback completeCallback) {
                        doStart(dependencyResults, completeCallback);
                    }
                })
                        .name(App.Start.class.getName())
                        .dependOn(App.Launch.class.getName())
                        .withoutCallback()
                        .build()
        );
    }

    public void doLaunch(Map<String, Object> dependencyResults, TaskCallback callback)
    {
        EventBus.getDefault().postSticky(Eventer.create(Eventer.TYPE.INIT, ResourceHelper.getString(getContext(), "tsdk_init")));

        getAppReport().launch(new ReportListener<App.LaunchResult>() {
            @Override
            public void dispatchResult(App.LaunchResult launchResult, ResponseException e) {

                if (e != null) // 上报字段有误
                {
                    Logger.e(TAG, "App.Launch failed: " + e.getMessage() + ", Code:" + e.getCode(), e);

                    EventBus.getDefault().postSticky(Eventer.create(Eventer.TYPE.INIT_FAIL, ResourceHelper.getString(getContext(), "tsdk_init_success")));

                } else {
                    //重置为线上的public key
                    Query.setPublicKey(launchResult.getPublicKey());
                    //设置alid
                    tsdkApi.setAlid(launchResult.getAlid());

                    callback.callback(launchResult.getAlid());

                    Logger.e(TAG, "App.Launch success: " + launchResult.getAlid());

                    EventBus.getDefault().postSticky(Eventer.create(Eventer.TYPE.INIT_SUCCESS, ResourceHelper.getString(getContext(), "tsdk_init_fail")));

                }
            }
        });
    }

    public void doStart(Map<String, Object> dependencyResults, TaskCallback callback)
    {
        getAppReport().start(null);
    }

    private AppReport getAppReport()
    {
        if (appReport == null)
            appReport = new AppReport(tsdkApi);

        return appReport;
    }

}
