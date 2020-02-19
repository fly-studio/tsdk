package org.fly.tsdk.sdk.query;

import org.fly.core.io.network.result.Result;

import java.io.File;
import java.util.List;

public interface QueryListener {
    void onDownloaded(File file, List<Object> objects);
    void onDone(Response response, List<Object> objects);
    void onError(Throwable e, List<Object> objects);
    void onProgress(long read, long total);
}
