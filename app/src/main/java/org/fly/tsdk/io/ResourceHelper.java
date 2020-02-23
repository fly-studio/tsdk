package org.fly.tsdk.io;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.annotation.NonNull;

import org.fly.core.io.IoUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import okio.Okio;
import okio.Source;

public class ResourceHelper {

    public static int getId(Context context, String name, String defType)
    {
        return context.getResources().getIdentifier(name, defType, context.getPackageName());
    }

    public static Source readAsset(@NonNull Context context, String path) throws IOException {

        File assetPath = StorageHelper.getCacheDir(context, "assets");
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
        File assetPath = StorageHelper.getCacheDir(context, "assets");

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
}
