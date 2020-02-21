package org.fly.tsdk.sdk.query.middleware;

import org.fly.tsdk.sdk.query.Request;
import org.fly.tsdk.sdk.query.Response;

import java.util.LinkedList;


public interface Middleware {
    void before(Request request, LinkedList<Object> object) throws Throwable;
    void after(Response response, LinkedList<Object> object) throws Throwable;
}


