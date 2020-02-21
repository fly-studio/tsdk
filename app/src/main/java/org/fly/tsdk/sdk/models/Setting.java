package org.fly.tsdk.sdk.models;

import android.content.Context;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.fly.core.annotation.NotProguard;
import org.fly.core.text.json.Jsonable;
import org.fly.tsdk.sdk.utils.ChannelHelper;

import java.util.HashMap;
import java.util.Map;

@NotProguard
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC)
final public class Setting extends Jsonable {

    protected long app_id = 0;
    protected String app_key = null;
    @JsonIgnore
    protected String sub_channel = null;
    @JsonIgnore
    protected String channel = null;
    protected Map<String, String> sdk_params = new HashMap<>();
    protected String sdk_url = null;

    public long getAppId() {
        return app_id;
    }

    public String getAppKey() {
        return app_key;
    }

    public String getSubChannel() {
        return sub_channel;
    }

    public String getChannel() {
        return channel;
    }

    public Map<String, String> getSdkParams() {
        return sdk_params;
    }

    public String getSdkUrl() {
        return sdk_url;
    }

    public void readSubChannel(Context context) {
        sub_channel = ChannelHelper.getSubChannel(context);
    }

    @JsonIgnore
    public void setChannel(String channel) {
        this.channel = channel;
    }
}
