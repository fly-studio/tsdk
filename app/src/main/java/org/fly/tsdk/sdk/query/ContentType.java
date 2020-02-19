package org.fly.tsdk.sdk.query;

import okhttp3.MediaType;

public class ContentType {
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    public static final MediaType JAVASCRIPT = MediaType.get("application/javascript; charset=utf-8");
    public static final MediaType XML = MediaType.get("application/xml; charset=utf-8");
    public static final MediaType TEXT = MediaType.get("text/plain; charset=utf-8");
    public static final MediaType HTML = MediaType.get("text/html; charset=utf-8");
    public static final MediaType FORM_DATA = MediaType.get("multipart/form-data");
    public static final MediaType X_WWW_FORM_URLENCODED = MediaType.get("application/x-www-form-urlencoded; charset=utf-8");
    public static final MediaType OCTET_STREAM = MediaType.get("application/octet-stream");

    public static final MediaType DEFAULT = TEXT;

}
