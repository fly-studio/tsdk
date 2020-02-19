package org.fly.tsdk.sdk.models;

import android.content.Context;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.fly.core.annotation.NotProguard;
import org.fly.core.text.json.Jsonable;
import org.fly.tsdk.sdk.io.StorageHelper;
import org.fly.tsdk.sdk.utils.ChannelHelper;

@NotProguard
public class Setting extends Jsonable {
    private long app_id = 0;
    private String app_key = null;
    @JsonIgnore
    private String sub_channel = null;
    private String channel = null;



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

    public void readSubChannel(Context context) {
        sub_channel = ChannelHelper.getSubChannel(context);
    }
}
