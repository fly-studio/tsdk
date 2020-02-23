package org.fly.tsdk.sdk.wrapper;

import org.fly.tsdk.sdk.TsdkApi;

public interface InitSdkListener {

    void callback(TsdkApi.INIT_RESULT init_result, String message);

}
