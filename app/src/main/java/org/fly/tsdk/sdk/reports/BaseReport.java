package org.fly.tsdk.sdk.reports;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;

import com.lahm.library.EasyProtectorLib;

import org.fly.tsdk.sdk.TsdkApi;
import org.fly.tsdk.sdk.models.Device;
import org.fly.tsdk.sdk.models.Property;
import org.fly.tsdk.sdk.models.Setting;
import org.fly.tsdk.sdk.query.Query;
import org.fly.tsdk.sdk.utils.Constants;
import org.fly.tsdk.sdk.utils.DeviceHelper;

public class BaseReport {

    protected static final Query query = new Query();
    protected Context context;
    protected Setting setting;

    public BaseReport(Context context, Setting setting)
    {
        this.context = context;
        this.setting = setting;
    }

    protected Device getDevice() {
        Device device = new Device();

        device.imei = DeviceHelper.getImei(context);

        device.android_id = Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);
        device.serial = Build.SERIAL;
        device.brand = Build.BRAND;
        device.model = Build.MODEL;
        device.metrics = DeviceHelper.getMetrics(context);
        device.os = "Android";
        device.os_version = Build.VERSION.RELEASE;
        device.arch = Build.CPU_ABI;
        device.is_rooted = EasyProtectorLib.checkIsRoot();
        device.is_simulator = EasyProtectorLib.checkIsRunningInEmulator(context, null);

        return device;
    }

    protected Property getProperty() {

        Property property = new Property();

        property.sdk_version = TsdkApi.VERSION;
        property.app_version = DeviceHelper.getAppVersion(context);
        property.app_version_code = DeviceHelper.getAppVersionCode(context);
        property.carrier = DeviceHelper.getCarrier(context);
        property.connection = DeviceHelper.getNetworkState(context).toString();
        property.device_at = DeviceHelper.getW3cTime();
        Location location = DeviceHelper.getLastKnownLocation(context);
        property.geometry = location != null ? location.getLatitude() + "," + location.getLongitude() : null;


        return property;
    }



}
