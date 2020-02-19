package org.fly.tsdk.sdk.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.RequiresPermission;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.DataOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.content.Context.LOCATION_SERVICE;

public class DeviceHelper {
    private static final String TAG = "DeviceHelper";
    public static String getMetrics(Context context)
    {
        DisplayMetrics display = context.getResources().getDisplayMetrics();

        if (display == null && context.getSystemService(Context.WINDOW_SERVICE) != null)
        {
            ((WindowManager) (context.getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay().getMetrics(display);
        }

        return display == null ? "" : display.widthPixels + "*" + display.heightPixels + " " + display.densityDpi;
    }

    public static String getImei(Context context) {

        if (!hasPermission(context, Manifest.permission_group.PHONE))
            return null;

        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                String imei;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    imei = telephonyManager.getImei();
                }
                else {
                    imei = telephonyManager.getDeviceId();
                }
                return imei;
            }
        } catch (SecurityException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return null;
    }

    public static boolean isRoot() {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();
            int exitValue = process.waitFor();
            if (exitValue == 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage(), e);
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isSimulator(Context context)
    {
        String url = "tel:" + "123456";
        Intent intent = new Intent();
        intent.setData(Uri.parse(url));
        intent.setAction(Intent.ACTION_DIAL);
        // 是否可以处理跳转到拨号的 Intent
        boolean canCallPhone = intent.resolveActivity(context.getPackageManager()) != null;
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.toLowerCase().contains("vbox")
                || Build.FINGERPRINT.toLowerCase().contains("test-keys")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("MuMu")
                || Build.MODEL.contains("virtual")
                || Build.SERIAL.equalsIgnoreCase("android")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT)
                || ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getNetworkOperatorName().toLowerCase().equals("android")
                || !canCallPhone;
    }

    //获取当前版本号
    public static String getAppVersion(Context context) {
        String versionName = "";
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
            if (TextUtils.isEmpty(versionName)) {
                return "";
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return versionName;
    }

    public static long getAppVersionCode(Context context) {
        long versionCode = 0;
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionCode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? packageInfo.getLongVersionCode() : packageInfo.versionCode;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return versionCode;
    }

    public static String getUUID(Context context)
    {
        SharedPreferences uuidSharedPreferences = context.getSharedPreferences(context.getPackageName() + ".device_info", Context.MODE_PRIVATE);

        String uniqueID = uuidSharedPreferences.getString("uuid", null);

        if (null != uniqueID && uniqueID.length() == 36)
            return uniqueID;

        uniqueID = UUID.randomUUID().toString();

        SharedPreferences.Editor editor= uuidSharedPreferences.edit();
        editor.putString("uuid", uniqueID);
        editor.apply();

        return uniqueID;
    }

    /**
     * 获取运营商名字
     *
     * @param context context
     * @return int
     */
    public static String getCarrier(Context context) {
        /*
         * getSimOperatorName()就可以直接获取到运营商的名字
         * 也可以使用IMSI获取，getSimOperator()，然后根据返回值判断，例如"46000"为移动
         * IMSI相关链接：http://baike.baidu.com/item/imsi
         */
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        // getSimOperatorName就可以直接获取到运营商的名字
        return telephonyManager != null ? telephonyManager.getSimOperatorName() : null;
    }

    /**
     * 判断是否有Sim卡
     *
     * @return
     */
    public static boolean hasSim(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
            return true;
        } else {
            return false;
        }
    }

    public static Location getLastKnownLocation(Context context) {

        if (!hasPermission(context, Manifest.permission_group.PHONE)) {
            return null;
        }

        LocationManager mLocationManager = (LocationManager)context.getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            try {

                Location l = mLocationManager.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                    // Found best last known location: %s", l);
                    bestLocation = l;
                }

            } catch (SecurityException e) {
                continue;
            }
        }

        return bestLocation;
    }


    public enum NETWORK {
        NONE("NONE"), // 没有网络连接
        WIFI("WIFI"), // wifi连接
        SECOND_G ("2G"), // 2G
        THIRD_G("3G"), // 3G
        FOURTH_G("4G"), // 4G
        FIFTH_G("5G"), // 5G
        OTHER("OTHER"); // 其他

        private final String value;

        NETWORK(String value) {
            this.value = value;
        }

        // 重写方法
        @Override
        public String toString() {
            return value;
        }
    }

    public static NETWORK getNetworkState(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); // 获取网络服务
        if (null == connManager) { // 为空则认为无网络
            return NETWORK.NONE;
        }
        // 获取网络类型，如果为空，返回无网络
        NetworkInfo activeNetInfo = connManager.getActiveNetworkInfo();
        if (activeNetInfo == null || !activeNetInfo.isAvailable()) {
            return NETWORK.NONE;
        }
        // 判断是否为WIFI
        NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (null != wifiInfo) {
            NetworkInfo.State state = wifiInfo.getState();
            if (null != state) {
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                    return NETWORK.WIFI;
                }
            }
        }
        // 若不是WIFI，则去判断是2G、3G、4G网
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = telephonyManager != null ? telephonyManager.getNetworkType() : 0;
        switch (networkType) {
            /*
             GPRS : 2G(2.5) General Packet Radia Service 114kbps
             EDGE : 2G(2.75G) Enhanced Data Rate for GSM Evolution 384kbps
             UMTS : 3G WCDMA 联通3G Universal Mobile Telecommunication System 完整的3G移动通信技术标准
             CDMA : 2G 电信 Code Division Multiple Access 码分多址
             EVDO_0 : 3G (EVDO 全程 CDMA2000 1xEV-DO) Evolution - Data Only (Data Optimized) 153.6kps - 2.4mbps 属于3G
             EVDO_A : 3G 1.8mbps - 3.1mbps 属于3G过渡，3.5G
             1xRTT : 2G CDMA2000 1xRTT (RTT - 无线电传输技术) 144kbps 2G的过渡,
             HSDPA : 3.5G 高速下行分组接入 3.5G WCDMA High Speed Downlink Packet Access 14.4mbps
             HSUPA : 3.5G High Speed Uplink Packet Access 高速上行链路分组接入 1.4 - 5.8 mbps
             HSPA : 3G (分HSDPA,HSUPA) High Speed Packet Access
             IDEN : 2G Integrated Dispatch Enhanced Networks 集成数字增强型网络 （属于2G，来自维基百科）
             EVDO_B : 3G EV-DO Rev.B 14.7Mbps 下行 3.5G
             LTE : 4G Long Term Evolution FDD-LTE 和 TDD-LTE , 3G过渡，升级版 LTE Advanced 才是4G
             EHRPD : 3G CDMA2000向LTE 4G的中间产物 Evolved High Rate Packet Data HRPD的升级
             HSPAP : 3G HSPAP 比 HSDPA 快些
             */
            // 2G网络
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return NETWORK.SECOND_G;
            // 3G网络
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return NETWORK.THIRD_G;
            // 4G网络
            case TelephonyManager.NETWORK_TYPE_LTE:
                return NETWORK.FOURTH_G;
            case TelephonyManager.NETWORK_TYPE_NR:
                return NETWORK.FIFTH_G;
            default:
                return NETWORK.OTHER;
        }
    }

    public static String getW3cTime() {
        return DateFormatUtils.format(new Date(),"yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
    }

    public static boolean hasPermission(Context mContexts, String permission) {
        return Build.VERSION.SDK_INT < 23 || mContexts.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED;
    }
}
