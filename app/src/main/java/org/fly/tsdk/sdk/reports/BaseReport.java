package org.fly.tsdk.sdk.reports;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;

import com.lahm.library.EasyProtectorLib;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.fly.core.io.network.result.Result;
import org.fly.core.text.json.Jsonable;
import org.fly.tsdk.sdk.TsdkApi;
import org.fly.tsdk.sdk.models.Device;
import org.fly.tsdk.sdk.models.Property;
import org.fly.tsdk.sdk.models.ReportResult;
import org.fly.tsdk.sdk.models.Setting;
import org.fly.tsdk.query.Query;
import org.fly.tsdk.query.QueryListener;
import org.fly.tsdk.query.Response;
import org.fly.tsdk.query.exceptions.InvalidJsonException;
import org.fly.tsdk.query.exceptions.ResponseException;
import org.fly.tsdk.text.Validator;
import org.fly.tsdk.io.DeviceHelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import okhttp3.HttpUrl;

public class BaseReport {

    private static final String TAG = "BaseReport";

    private TsdkApi sdkApi;

    public BaseReport(TsdkApi sdkApi)
    {
        this.sdkApi = sdkApi;
    }

    public Context getContext() {
        return sdkApi.getContext();
    }

    public Setting getSetting() {
        return sdkApi.getSetting();
    }

    public String getAlid() {
        return sdkApi.getAlid();
    }

    public Query getQuery() {
        return sdkApi.getQuery();
    }

    protected Device getDevice() {
        Device device = new Device();

        device.imei = DeviceHelper.getImei(getContext());

        device.android_id = Settings.System.getString(getContext().getContentResolver(), Settings.System.ANDROID_ID);
        device.serial = Build.SERIAL;
        device.brand = Build.BRAND;
        device.model = Build.MODEL;
        device.metrics = DeviceHelper.getMetrics(getContext());
        device.os = "Android";
        device.os_version = Build.VERSION.RELEASE;
        device.arch = Build.CPU_ABI;
        device.is_rooted = EasyProtectorLib.checkIsRoot();
        device.is_simulator = EasyProtectorLib.checkIsRunningInEmulator(getContext(), null);

        return device;
    }

    protected Property getProperty() {

        Property property = new Property();

        property.sdk_version = TsdkApi.VERSION;
        property.app_version = DeviceHelper.getAppVersion(getContext());
        property.app_version_code = DeviceHelper.getAppVersionCode(getContext());
        property.carrier = DeviceHelper.getCarrier(getContext());
        property.connection = DeviceHelper.getNetworkState(getContext()).toString();
        property.device_at = DeviceHelper.getW3cTime();
        Location location = DeviceHelper.getLastKnownLocation(getContext());
        property.geometry = location != null ? location.getLatitude() + "," + location.getLongitude() : null;

        return property;
    }

    private void validateUrl(String url)
    {
        if (!Validator.equalsUrl(url))
            throw new IllegalArgumentException("Invalid URL: " + url);
    }

    protected HttpUrl buildUrl(String urlPath, Map<String, String> pathParameters, List<Pair<String, String>> queryParameters)
    {
        String url = getSetting().getSdkUrl() + urlPath;

        if (pathParameters == null) {
            pathParameters = new HashMap<>();
            pathParameters.put("alid", getAlid());
            pathParameters.put("channel", getSetting().getChannel());
        }

        for (Map.Entry<String, String> entry: pathParameters.entrySet())
        {
            url = url.replace("{" + entry.getKey() + "}", entry.getValue() == null ? "" : entry.getValue());
        }


        validateUrl(url);

        return Query.buildUrl(url, queryParameters);
    }

    protected HttpUrl buildUrl(String urlPath, Map<String, String> pathParameters)
    {
        return buildUrl(urlPath, pathParameters, null);
    }

    protected HttpUrl buildUrl(String urlPath, List<Pair<String, String>> queryParameters)
    {
        return buildUrl(urlPath, null, queryParameters);
    }

    protected HttpUrl buildUrl(String urlPath)
    {
        return buildUrl(urlPath, null, null);
    }

    protected <T extends ReportResult> QueryListener queryToReportListener(final Class<T> resultClazz, final ReportListener<T> reportListener)
    {
        return new QueryListener()
        {
            @Override
            public void onDone(Response response, LinkedList<Object> objects) {
                T reportResult = null;
                try {
                    if (!objects.isEmpty() && objects.getLast() instanceof Result) {
                        //读取result
                        Result result = (Result) objects.getLast();
                        if (!StringUtils.isEmpty(result.data)) {
                            reportResult = ReportResult.fromJson(resultClazz, result.data);
                        }
                    }
                } catch (IOException e) {
                    this.onError(new InvalidJsonException("Invalid [" + resultClazz.getSimpleName() + "] JSON: " + e.getMessage(), 5001, e), objects);
                    return;
                }

                if (reportResult != null && reportListener != null)
                    reportListener.callback(reportResult, null);
                else if (objects.getLast() == null || !(objects.getLast() instanceof Result)) // response，result.data为空
                    this.onError(new InvalidJsonException("Invalid [" + resultClazz.getSimpleName() + "] Response or result.data: " + response.getContent(), 5002), objects);
                else if (reportListener == null)
                    Log.d(TAG, "Recv [" + resultClazz.getSimpleName() + "]: " + ((Result) objects.getLast()).toJson());
                else
                    this.onError(new InvalidJsonException("Invalid [" + resultClazz.getSimpleName() + "] Response or result.data: " + ((Result) objects.getLast()).toJson(), 5003), objects);

            }

            @Override
            public void onError(ResponseException e, LinkedList<Object> objects) {
                if (reportListener != null)
                    reportListener.callback(null, e);
                else if (e != null) // 如果没有回调，则打印出错误
                    Log.e(TAG, "[" + resultClazz.getSimpleName() + "] Report error:" + e.getMessage() + "; Code: " + e.getCode(), e);
            }

        };
    }

    protected <T extends ReportResult> void post(HttpUrl url, Jsonable data, Class<T> resultClazz, @Nullable ReportListener<T> reportListener)
    {
        getQuery().postWithEncrypted(url, data, queryToReportListener(resultClazz, reportListener));
    }

}
