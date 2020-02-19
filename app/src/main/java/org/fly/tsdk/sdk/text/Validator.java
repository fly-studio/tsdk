package org.fly.tsdk.sdk.text;

import android.util.Patterns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {

    public static boolean equals(String str, Pattern pattern)
    {
        Matcher matcher = pattern.matcher(str);
        return matcher.find() && str.equals(matcher.group(0));
    }

    public static boolean equalsUrl(String str)
    {
        return equals(str, Patterns.WEB_URL);
    }

    public static boolean contains(String str, Pattern pattern)
    {
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }

    public static boolean containsUrl(String str)
    {
        return contains(str, Patterns.WEB_URL);
    }
}