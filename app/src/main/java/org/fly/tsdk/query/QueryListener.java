package org.fly.tsdk.query;

import org.fly.tsdk.io.Logger;
import org.fly.tsdk.query.exceptions.ResponseException;

import java.io.File;

public interface QueryListener {
    default void onDownloaded(File file){

    }

    void onDone(Response response);
    default void onError(ResponseException e){

        Logger.e("Query", e.getMessage(), e);

    }
    default void onProgress(long read, long total) {

    }
}
