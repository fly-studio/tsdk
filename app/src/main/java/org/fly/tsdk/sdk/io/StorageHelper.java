package org.fly.tsdk.sdk.io;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.fly.core.io.IoUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import okio.Okio;
import okio.Source;

public class StorageHelper {

    private static final String TAG = "StorageHelper";

    public static String getStoragePath(@NonNull Context context)
    {
        return getStoragePath(context, false);
    }

    public static String getStoragePath(@NonNull Context mContext, boolean is_removale) {

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        if (mStorageManager == null)
            return null;

        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removale == removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return null;
    }


    public static File getCacheDir(Context context)
    {
        return getCacheDir(context, null);
    }

    public static File getCacheDir(@NonNull Context context, String suffix)
    {
        File file = context.getCacheDir();
        return new File(file, suffix);
    }

    public static File getFileDir(@NonNull Context context, String suffix)
    {
        File file = context.getFilesDir();
        return new File(file, suffix);
    }

    public static File getFileDir(@NonNull Context context)
    {
        return getFileDir(context, null);
    }

    public static String getNowFilename()
    {
        return getNowFilename("yyyyMMddHHmmss");
    }

    public static String getNowFilename(@NonNull String format)
    {
        DateFormat df = new SimpleDateFormat(format, Locale.CHINA);
        return df.format(new Date());
    }

    public static String getRandomFilename(int length)
    {
        StringBuilder sb = new StringBuilder();
        do {
            sb.append(UUID.randomUUID().toString().replace("{", "").replace("-", ""));
        } while (sb.length() < length);

        return sb.substring(0, length);
    }

    public static File getDataDir(@NonNull Context context)
    {
        return getDataDir(context, null);
    }

    @SuppressLint("NewApi")
    public static File getDataDir(@NonNull Context context, String suffix)
    {
        File file = context.getDataDir();
        return new File(file, suffix);
    }

    public static File getVideoDir()
    {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
    }

    public static File getPictureDir()
    {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    }

    public static File getDCIMDir()
    {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
    }

    public static void rescanGallery(@NonNull Context context, File ...files)
    {
        String[] paths = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            paths[i] = files[i].getAbsolutePath();
        }
        MediaScannerConnection.scanFile(context, paths,null, null);
    }

    public static Source readAsset(@NonNull Context context, String path) throws IOException {

        File assetPath = getCacheDir(context, "assets");
        if (!assetPath.exists())
            assetPath.mkdirs();

        File file = new File(assetPath, path);

        if (file.exists() && file.isFile())
        {
            return Okio.source(file);
        } else {
            AssetManager assetManager =  context.getAssets();

            InputStream inputStream = assetManager.open(path);
            return Okio.source(inputStream);
        }

    }

    public static void writeAsset(@NonNull Context context, String path, byte[] bytes) throws IOException
    {
        File assetPath = getCacheDir(context, "assets");

        File file = new File(assetPath, path);

        file.getParentFile().mkdirs();

        if (!file.exists())
            file.createNewFile();

        IoUtils.write(file, bytes);
    }

    public static void writeAsset(@NonNull Context context, String path, String content) throws IOException
    {
        writeAsset(context, path, content.getBytes());
    }

    @Nullable
    public static String getApkPath(@NonNull final Context context) {
        String apkPath = null;
        try {
            final ApplicationInfo applicationInfo = context.getApplicationInfo();
            if (applicationInfo == null) {
                return null;
            }
            apkPath = applicationInfo.sourceDir;
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return apkPath;
    }


}
