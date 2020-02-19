package org.fly.tsdk.sdk.query.middleware;

import org.fly.core.io.network.result.Result;
import org.fly.tsdk.sdk.query.ContentType;
import org.fly.tsdk.sdk.query.Request;
import org.fly.tsdk.sdk.query.Response;

import java.util.List;

public class JsonFormatter implements Middleware {


    public JsonFormatter() {
    }

    @Override
    public void before(Request request, List<Object> object) throws Throwable {
        request.setHeader("Content-Type", ContentType.JSON.toString());

    }

    @Override
    public void after(Response response, List<Object> objects) throws Throwable {
        if (response.getContent() != null && "json".equalsIgnoreCase(response.getContentType().subtype()))
        {
            String json = response.getContent();
            if (json.startsWith("{") && json.endsWith("}") && json.contains("code")) {
                Result result = Result.fromJson(Result.class, json);
                objects.add(result);
            }
        }
    }

}
