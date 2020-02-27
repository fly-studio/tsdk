package org.fly.tsdk.sdk.reports;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;


import androidx.annotation.Nullable;

import com.lahm.library.EasyProtectorLib;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.fly.core.annotation.NotProguard;
import org.fly.core.io.network.result.Result;
import org.fly.core.text.encrytor.Decryptor;
import org.fly.core.text.json.Jsonable;
import org.fly.tsdk.io.Logger;
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

@NotProguard
public class BaseReport {

    private static final String TAG = "BaseReport";

    public static Context getContext() {
        return TsdkApi.getInstance().getContext();
    }

    public static Setting getSetting() {
        return TsdkApi.getInstance().getSetting();
    }

    protected static Device getDevice() {
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

    protected static Property getProperty() {

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

    private static void validateUrl(String url)
    {
        if (!Validator.equalsUrl(url))
            throw new IllegalArgumentException("Invalid URL: " + url);
    }

    protected static HttpUrl buildUrl(String urlPath, Map<String, String> pathParameters, List<Pair<String, String>> queryParameters)
    {
        String url = getSetting().getSdkUrl() + urlPath;

        if (pathParameters == null) {
            pathParameters = new HashMap<>();
            pathParameters.put("alid", TsdkApi.getInstance().getAlid());
            pathParameters.put("channel", getSetting().getChannel());
        }

        for (Map.Entry<String, String> entry: pathParameters.entrySet())
        {
            url = url.replace("{" + entry.getKey() + "}", entry.getValue() == null ? "" : entry.getValue());
        }


        validateUrl(url);

        return Query.buildUrl(url, queryParameters);
    }

    protected static HttpUrl buildUrl(String urlPath, Map<String, String> pathParameters)
    {
        return buildUrl(urlPath, pathParameters, null);
    }

    protected static HttpUrl buildUrl(String urlPath, List<Pair<String, String>> queryParameters)
    {
        return buildUrl(urlPath, null, queryParameters);
    }

    protected static HttpUrl buildUrl(String urlPath)
    {
        return buildUrl(urlPath, null, null);
    }

    protected static <T extends ReportResult> QueryListener queryToReportListener(final Class<T> resultClazz, final ReportListener<T> reportListener)
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

                if (reportResult != null)
                {
                    if (reportListener != null)
                        reportListener.callback(reportResult, null);
                    else
                        Logger.d(TAG, "Recv [" + resultClazz.getSimpleName() + "]: " + ((Result) objects.getLast()).toJson());

                }
                else if (objects.getLast() == null || !(objects.getLast() instanceof Result)) // response，result.data为空
                    this.onError(new InvalidJsonException("Invalid [" + resultClazz.getSimpleName() + "] Response: " + response.getContent(), 5002), objects);
                else
                    this.onError(new InvalidJsonException("Invalid [" + resultClazz.getSimpleName() + "] result.data: " + ((Result) objects.getLast()).toJson(), 5003), objects);

            }

            @Override
            public void onError(ResponseException e, LinkedList<Object> objects) {
                if (reportListener != null)
                    reportListener.callback(null, e);
                else if (e != null) // 如果没有回调，则打印出错误
                    Logger.e(TAG, "[" + resultClazz.getSimpleName() + "] Report error:" + e.getMessage() + "; Code: " + e.getCode(), e);
            }

        };
    }

    /**
     * Post until Success
     * @param urlPath
     * @param data
     * @param resultClazz
     * @param reportListener
     * @param <T>
     */
    protected static <T extends ReportResult> Query.Builder post(String urlPath,
                                                          Jsonable data,
                                                          Class<T> resultClazz,
                                                          @Nullable ReportListener<T> reportListener)
    {
        HttpUrl url = buildUrl(urlPath);
        return Query.postWithEncrypted(url, data)
                .withQueryListener(queryToReportListener(resultClazz, reportListener))
                ;
    }
}
