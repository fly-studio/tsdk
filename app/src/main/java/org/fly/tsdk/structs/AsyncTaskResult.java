package org.fly.tsdk.structs;

public class AsyncTaskResult<T, E extends Throwable> {
    private T result;
    private E error;

    public T getResult() {
        return result;
    }

    public E getError() {
        return error;
    }

    public AsyncTaskResult(T result) {
        this.result = result;
    }

    public AsyncTaskResult(E error) {
        this.error = error;
    }
}