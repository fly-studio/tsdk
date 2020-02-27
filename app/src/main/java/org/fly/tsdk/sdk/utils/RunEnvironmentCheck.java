package org.fly.tsdk.sdk.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import org.fly.tsdk.io.Logger;
import org.fly.tsdk.io.ResourceHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class RunEnvironmentCheck {

    private static final String TAG = "RunEnvironmentCheck";

    public static final String[] PERMISSIONS = new String[]{
            "android.permission.READ_PHONE_STATE",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.INTERNET",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.ACCESS_WIFI_STATE"
    };
    private static final String[] CLASSES = new String[]{
            "android.arch.core.internal.FastSafeIterableMap", "androidx.arch.core.internal.FastSafeIterableMap",
            "android.arch.core.util.Function", "androidx.arch.core.util.Function",
            "android.arch.lifecycle.Lifecycle", "androidx.lifecycle.Lifecycle",
            "android.arch.lifecycle.Observer", "androidx.lifecycle.Observer",
            "android.arch.lifecycle.ReportFragment", "androidx.lifecycle.ReportFragment",
            "android.arch.lifecycle.ViewModel", "androidx.lifecycle.ViewModel",
/*            "android.support.v4.app.Fragment",
            "android.support.annotation.AnimatorRes",
            "android.support.v4.app.ActivityCompat",
            "android.support.design.widget.CoordinatorLayout",
            "android.support.v4.app.AppLaunchChecker",
            "android.support.v4.app.BackStackState"*/
    };
    private static final String[] DRAWABLES = new String[]{};
    private static final String[] IDS = new String[]{};
    private static final String[] LAYOUTS = new String[]{};

    public static void checkClasses() {
        for(int i = 0; i < CLASSES.length; i = i + 2) {
            int unfound = 0;

            try {
                Class.forName(CLASSES[i]);
            } catch (ClassNotFoundException e) {
                unfound++;
            }

            try {
                Class.forName(CLASSES[i + 1]);
            } catch (ClassNotFoundException e) {
                unfound++;
            }

            if (unfound > 1) {
                throw new IllegalStateException(CLASSES[i] + " or " + CLASSES[i + 1] + " required in APP.");
            }
        }
    }

    public static void checkPermissionsInManifest(Context context) {
        try {
            List<String> permissions  = new ArrayList<>(Arrays.asList(PERMISSIONS));
            String[] inManifest = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS).requestedPermissions;

            if (inManifest != null) {
                for (String perm : inManifest)
                    permissions.remove(perm);
            }

            if (permissions.size() > 0) {
                throw new IllegalStateException("请确保在AndroidManifest.xml声明了以下权限:" + permissions.toString());
            }
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e(TAG, e.getMessage(), e);
        }
    }

    public static String a(Context context, String clazz) {
        Intent intent = new Intent("android.intent.action.MAIN", null);
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Iterator iterator = context.getPackageManager()
                .queryIntentActivities(intent, PackageManager.GET_META_DATA)
                .iterator();

        ResolveInfo resolveInfo;
        do {
            if (!iterator.hasNext()) {
                return "no ".concat(String.valueOf(clazz));
            }
        } while(!(resolveInfo = (ResolveInfo)iterator.next()).activityInfo.packageName.equals(clazz));

        return resolveInfo.activityInfo.name;
    }

    public static void checkResoure(Context context) {
        StringBuilder str = new StringBuilder();

        for (String drawable: DRAWABLES)
        {
            if (ResourceHelper.getId(context, drawable, ResourceHelper.DefType.DRAWABLE) == 0)
                str.append("\ndrawable/").append(drawable);
        }

        for (String drawable: IDS)
        {
            if (ResourceHelper.getId(context, drawable, ResourceHelper.DefType.ID) == 0)
                str.append("\nid/").append(drawable);
        }

        for (String drawable: LAYOUTS)
        {
            if (ResourceHelper.getId(context, drawable, ResourceHelper.DefType.LAYOUT) == 0)
                str.append("\ndrawable/").append(drawable);
        }

        if (str.length() > 0)
            throw new IllegalStateException("Cannot find these in res: ".concat(str.toString()));
    }
}
