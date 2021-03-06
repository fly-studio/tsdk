package org.fly.tsdk.query.middleware;

import org.fly.core.io.network.result.Result;
import org.fly.tsdk.query.ContentType;
import org.fly.tsdk.query.Request;
import org.fly.tsdk.query.Response;
import org.fly.tsdk.query.exceptions.InvalidFiledException;
import org.fly.tsdk.query.exceptions.ResponseException;

import java.util.LinkedList;

public class JsonFormatter implements Middleware {


    public JsonFormatter() {
    }

    @Override
    public void before(Request request, LinkedList<Object> object) throws Throwable {
        request.setHeader("Accept", ContentType.JSON.toString());

        if (request.getBody() != null && request.getBody().contentType() != null)
            request.setHeader("Content-Type", request.getBody().contentType().toString());
    }

    @Override
    public void after(Response response, LinkedList<Object> objects) throws Throwable {
        if (response.getContent() != null && "json".equalsIgnoreCase(response.getContentType().subtype()))
        {
            String json = response.getContent();
            if (json.startsWith("{") && json.endsWith("}") && json.contains("code")) {

                // to
                Result result = Result.fromJson(Result.class, json);

                if (result.code == 422)
                    throw new InvalidFiledException(result.message, result.code, response);
                else if (result.code != 0 || response.getCode() != 200)
                    throw new ResponseException(result.message, result.code, response);

                objects.add(result);
            }
        }
    }

}
