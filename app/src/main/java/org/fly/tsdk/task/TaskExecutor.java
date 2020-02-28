package org.fly.tsdk.task;

import org.fly.tsdk.io.Logger;

import java.util.Map;

public interface TaskExecutor {
    void call(Map<String, Object> dependencyResults, TaskCallback completeCallback);

    default void onError(Throwable e) {
        Logger.e("TaskManager", e.getMessage(), e);
    }
}
