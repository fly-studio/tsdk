package org.fly.tsdk.sdk.models;

import org.fly.core.annotation.NotProguard;
import org.fly.core.text.json.Jsonable;

@NotProguard
public class App {
    public static class Launch extends Jsonable {
        public Device device;
        public Property property = null;
        public long app_id = 0;
        public String uuid = null;
        public String sub_channel = null;
    }

    public static class Exception extends Jsonable {
        public Property property;
        public String exception = null;

    }
}
