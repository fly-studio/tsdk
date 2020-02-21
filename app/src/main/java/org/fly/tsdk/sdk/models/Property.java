package org.fly.tsdk.sdk.models;

import org.fly.core.annotation.NotProguard;
import org.fly.core.text.json.Jsonable;

@NotProguard
final public class Property extends Jsonable {
    public String carrier = null;
    public String connection = null;
    public long app_version_code = 0;
    public String app_version = null;
    public String sdk_version = null;
    public String geometry = null;
    public String device_at = null;
}
