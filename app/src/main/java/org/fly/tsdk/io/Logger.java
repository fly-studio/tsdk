package org.fly.tsdk.io;

import android.util.Log;

public class Logger {

    public static void v(String tag, String content)
    {
        Log.v(tag, content);
    }

    public void v(String tag, String content, Throwable e)
    {
        Log.v(tag, content, e);
    }

    public static void d(String tag, String content)
    {
        Log.d(tag, content);
    }

    public static void d(String tag, String content, Throwable e)
    {
        Log.d(tag, content, e);
    }

    public static void i(String tag, String content)
    {
        Log.i(tag, content);
    }

    public static void i(String tag, String content, Throwable e)
    {
        Log.i(tag, content, e);
    }

    public static void w(String tag, String content)
    {
        Log.w(tag, content);
    }

    public static void w(String tag, String content, Throwable e)
    {
        Log.w(tag, content, e);
    }

    public static void w(String tag, Throwable e)
    {
        Log.w(tag, e);
    }

    public static void e(String tag, String content)
    {
        Log.e(tag, content);
    }

    public static void e(String tag, String content, Throwable e)
    {
        Log.e(tag, content, e);
    }

}
