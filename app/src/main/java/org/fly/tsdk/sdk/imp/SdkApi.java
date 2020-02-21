package org.fly.tsdk.sdk.imp;

import android.content.Context;

import org.fly.tsdk.sdk.models.Setting;
import org.fly.tsdk.sdk.query.Query;

abstract public class SdkApi {

    protected Query query;
    protected String alid;
    protected Context context;
    protected Setting setting;

    public Context getContext() {
        return context;
    }

    public Setting getSetting() {
        return setting;
    }

    public String getAlid() {
        return alid;
    }

    public Query getQuery() {
        return query;
    }

    protected void setContext(Context context) {
        this.context = context;
    }

    protected void setSetting(Setting setting) {
        this.setting = setting;
    }

    protected void setAlid(String alid) {
        this.alid = alid;
    }

    protected void setQuery(Query query) {
        this.query = query;
    }

}
