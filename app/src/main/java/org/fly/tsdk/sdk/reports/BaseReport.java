package org.fly.tsdk.sdk.reports;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.Nullable;

import com.lahm.library.EasyProtectorLib;

import org.apache.commons.lang3.StringUtils;
import org.fly.core.io.network.result.Result;
import org.fly.core.text.json.Jsonable;
import org.fly.tsdk.io.DeviceHelper;
import org.fly.tsdk.io.Logger;
import org.fly.tsdk.query.Query;
import org.fly.tsdk.query.QueryListener;
import org.fly.tsdk.query.Response;
import org.fly.tsdk.query.exceptions.InvalidJsonException;
import org.fly.tsdk.query.exceptions.ResponseException;
import org.fly.tsdk.sdk.TsdkApi;
import org.fly.tsdk.sdk.models.Device;
import org.fly.tsdk.sdk.models.Property;
import org.fly.tsdk.sdk.models.ReportResult;
import org.fly.tsdk.sdk.models.Setting;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

abstract public class BaseReport {

    private static final String TAG = "BaseReport";

    private TsdkApi tsdkApi;

    public BaseReport(TsdkApi tsdkApi) {
        this.tsdkApi = tsdkApi;
    }

    protected Context getContext() {
        return tsdkApi.getContext();
    }

    protected Setting getSetting() {
        return tsdkApi.getSetting();
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

    protected Map<String, String> getDefaultUrlParameter()
    {
        Map<String, String> urlParameters = new HashMap<>();
        urlParameters.put("alid", tsdkApi.getAlid());
        urlParameters.put("channel", getSetting().getChannel());

        return urlParameters;
    }

    protected <T extends ReportResult> Query.Builder buildQuery(final String urlPath, Jsonable data)
    {
        String url = getSetting().getSdkUrl() + urlPath;

        return Query.postWithJson(url, data)
                .urlParameters(getDefaultUrlParameter());
    }


    public <R extends ReportResult> AsyncTask call(Query.Builder builder,
                                                   Class<R> resultClazz,
                                                   @Nullable ReportListener<R> reportListener)
    {
        return builder
                .withJson()
                .withEncrypted()
                .withQueryListener(queryToReportListener(resultClazz, reportListener))
                .execute();
    }

    public <R extends ReportResult> R syncCall(Query.Builder builder, Class<R> resultClazz) throws ResponseException
    {
        Response response = builder
                .withJson()
                .withEncrypted()
                .syncExecute()
                ;

        return parseResult(response, resultClazz);
    }

    @Nullable
    protected <R extends ReportResult> R parseResult(Response response, final Class<R> resultClazz) throws ResponseException
    {
        LinkedList<Object> attachments = response.getAttachments();

        if (attachments == null || !(attachments.peekLast() instanceof Result))
            throw new InvalidJsonException("Invalid [" + resultClazz.getSimpleName() + "] Response: " + response.getContent(), 5001, response);

        try {
            //读取result
            Result result = (Result) attachments.getLast();
            if (!StringUtils.isEmpty(result.data)) {
                R reportResult = ReportResult.fromJson(resultClazz, result.data);

                Logger.d(TAG, "Recv [" + resultClazz.getSimpleName() + "]: " + reportResult.toJson());
                return reportResult;
            }

        } catch (IOException e) {
            throw new InvalidJsonException("Invalid [" + resultClazz.getSimpleName() + "] result.data: " + ((Result) attachments.getLast()).toJson(), 5003, response, e);
        }

        return null;
    }

    protected <R extends ReportResult> QueryListener queryToReportListener(final Class<R> resultClazz, final ReportListener<R> reportListener)
    {
        return new QueryListener()
        {
            @Override
            public void onDone(Response response) {

                try {
                    R reportResult = parseResult(response, resultClazz);

                    if (reportListener != null)
                        reportListener.dispatchResult(reportResult, null);

                } catch (ResponseException e)
                {
                    onError(e);
                }
            }

            @Override
            public void onError(ResponseException e) {
                Logger.e(TAG, "[" + resultClazz.getSimpleName() + "] Report error:" + e.getMessage() + "; Code: " + e.getCode(), e);

                if (reportListener != null)
                    reportListener.dispatchResult(null, e);
            }
        };
    }

}
