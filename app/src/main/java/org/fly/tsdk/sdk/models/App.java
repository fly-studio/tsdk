package org.fly.tsdk.sdk.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.fly.core.annotation.NotProguard;
import org.fly.core.text.json.Jsonable;

@NotProguard
final public class App {
    final public static class Launch extends Jsonable {
        public Device device;
        public Property property = null;
        public long app_id = 0;
        public String uuid = null;
        public String sub_channel = null;
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC)
    final public static class LaunchResult extends ReportResult {
        protected boolean need_device_id;
        protected String alid;
        protected String channel;
        protected long expired_at;
        protected String public_key;

        public boolean isNeedDeviceId() {
            return need_device_id;
        }

        public String getAlid() {
            return alid;
        }

        public String getChannel() {
            return channel;
        }

        public long getExpiredAt() {
            return expired_at;
        }

        public String getPublicKey() {
            return public_key;
        }
    }

    final public static class Exception extends Jsonable {
        public Property property;
        public String exception = null;

    }
}
