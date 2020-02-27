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

    public enum DefType{
        LAYOUT("layout"),
        ID("id"),
        DRAWABLE("drawable"),
        STRING("string"),
        ANIM("anim"),
        COLOR("color"),
        DIMEN("dimen"),
        STYLE("style"),
        ;

        private String value;

        DefType(String value) {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return value;
        }
    }

    public static int getId(Context context, String name, DefType defType)
    {
        return context.getResources().getIdentifier(name, defType.toString(), context.getPackageName());
    }

    public static String getString(Context context, String name)
    {
        return context.getResources().getString(getId(context, name, DefType.STRING));
    }

    public static int[] getIds(Context context, String name, DefType defType)
    {
        String packageName = context.getPackageName();

        Class clazz = null;
        int[] ids = null;
        try {
            clazz = Class.forName(packageName + ".R");

            Class[] classes = clazz.getClasses();
            Class desireClass = null;

            for (int i = 0; i < classes.length; ++i) {
                if (classes[i].getName().split("\\$")[1].equals(defType.toString())) {
                    desireClass = classes[i];
                    break;
                }
            }
            if (desireClass != null
                    && desireClass.getField(name).get(desireClass) != null
                    && desireClass.getField(name).get(desireClass).getClass().isArray()
            )
                ids = (int[]) desireClass.getField(name).get(desireClass);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return ids;
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
