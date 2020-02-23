package org.fly.tsdk.query.middleware;

import org.fly.tsdk.query.Request;
import org.fly.tsdk.query.Response;

import java.util.LinkedList;


public interface Middleware {
    void before(Request request, LinkedList<Object> object) throws Throwable;
    void after(Response response, LinkedList<Object> object) throws Throwable;
}


