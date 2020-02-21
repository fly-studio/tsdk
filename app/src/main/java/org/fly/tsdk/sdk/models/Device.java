package org.fly.tsdk.sdk.models;

import org.fly.core.annotation.NotProguard;
import org.fly.core.text.json.Jsonable;

@NotProguard
final public class Device extends Jsonable {
    public String imei = null;
    public String idfa = null;
    public String oaid = null;
    public String android_id = null;
    public String serial = null;
    public String brand = null;
    public String model = null;
    public String arch = null;
    public String os = null;
    public String os_version = null;
    public String mac = null;
    public String bluetooth = null;
    public String metrics = null;
    public boolean is_rooted = false;
    public boolean is_simulator = false;
}
