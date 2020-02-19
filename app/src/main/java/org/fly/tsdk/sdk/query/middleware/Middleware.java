package org.fly.tsdk.sdk.query.middleware;

import org.fly.core.function.Throwing;
import org.fly.tsdk.sdk.query.Request;
import org.fly.tsdk.sdk.query.Response;

import java.util.List;


public interface Middleware {
    void before(Request request, List<Object> object) throws Throwable;
    void after(Response response, List<Object> object) throws Throwable;
}


