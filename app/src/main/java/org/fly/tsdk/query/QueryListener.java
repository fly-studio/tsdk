package org.fly.tsdk.query;

import org.fly.tsdk.query.exceptions.ResponseException;

import java.io.File;
import java.util.LinkedList;

public interface QueryListener {
    default void onDownloaded(File file, LinkedList<Object> objects){

    }

    void onDone(Response response, LinkedList<Object> objects);
    default void onError(ResponseException e, LinkedList<Object> objects){

    }
    default void onProgress(long read, long total) {

    }
}
