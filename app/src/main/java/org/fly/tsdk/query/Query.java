package org.fly.tsdk.query;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import org.fly.core.text.encrytor.Decryptor;
import org.fly.core.text.json.Jsonable;
import org.fly.tsdk.io.Logger;
import org.fly.tsdk.query.exceptions.ResponseException;
import org.fly.tsdk.query.middleware.EncryptBody;
import org.fly.tsdk.query.middleware.JsonFormatter;

import java.io.File;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import okhttp3.RequestBody;

public class Query {
    private static final String TAG = "HTTP";
    private static Decryptor decryptor = new Decryptor();

    public final static String GET = "GET";
    public final static String POST = "POST";
    public final static String DELETE = "DELETE";
    public final static String PUT = "PUT";
    public final static String PATCH = "PATCH";
    public final static String HEAD = "HEAD";
    public final static String OPTION = "OPTION";

    public final static int UNLIMIT_RETRY = -1;

    public synchronized static void setPublicKey(String publicKey) {
        decryptor.setKey(publicKey, null);
    }

    public static class Builder {
        private QueryTask.Builder builder = new QueryTask.Builder();
        private Request.Builder requestBuilder;
        /**
         * any http call with json
         * @param url
         * @param data
         */
        public Builder(String method, String url, @NonNull Jsonable data) {

            requestBuilder = new Request.Builder()
                    .method(method)
                    .url(url)
                    .content(data.toJson(), ContentType.JSON)
            ;
        }

        /**
         * Any http call with body
         * @param method
         * @param url
         * @param body
         */
        public Builder(String method, String url, RequestBody body)
        {
            requestBuilder = new Request.Builder()
                    .method(method)
                    .url(url)
                    .body(body)
            ;
        }

        /**
         * Any http call
         * @param url
         */
        public Builder(String method, String url)
        {
            requestBuilder = new Request.Builder()
                    .method(method)
                    .url(url)
            ;
        }

        public Builder urlParameter(String name, String value)
        {
            requestBuilder.urlParameter(name, value);
            return this;
        }

        public Builder urlParameters(Map<String, String> urlParameters)
        {
            requestBuilder.urlParameters(urlParameters);
            return this;
        }

        public Builder queryParameter(String name, String value)
        {
            requestBuilder.queryParameter(name, value);
            return this;
        }

        public Builder queryParameter(Map<String, String> queryParameter)
        {
            requestBuilder.urlParameters(queryParameter);
            return this;
        }

        public Builder header(String name, String value)
        {
            requestBuilder.header(name, value);
            return this;
        }

        public Builder headers(Map<String, String> headers)
        {
            requestBuilder.headers(headers);
            return this;
        }

        public Builder cookie(String name, String value) {
            requestBuilder.cookie(name, value);
            return this;
        }

        public Builder cookies(Map<String, String> cookies)
        {
            requestBuilder.cookies(cookies);
            return this;
        }

        /**
         * retry N times
         * -1: always retry until success
         * 0: no retry
         * @param retries
         * @return
         */
        public Builder retries(int retries)
        {
            builder.retries(retries);
            return this;
        }

        /**
         * always retry when 500 or timeout
         * @return
         */
        public Builder unlimitRetry()
        {
            return retries(UNLIMIT_RETRY);
        }

        /**
         * download a file
         * @param file
         * @return
         */
        public Builder writeToFile(File file)
        {
            builder.file(file);
            return this;
        }

        /**
         * with a middleware of {@link JsonFormatter}
         * @return
         */
        public Builder withJson() {
            builder.middleware(new JsonFormatter());
            return this;
        }

        /**
         * with a middleware of {@link EncryptBody}
         * @return
         */
        public Builder withEncrypted() {
            builder.middleware(new EncryptBody(decryptor));
            return this;
        }

        /**
         * set a {@link QueryListener}
         * conflict with {@link #syncExecute}
         *
         * @param queryListener
         * @return
         */
        public Builder withQueryListener(QueryListener queryListener) {
            builder.queryListener(queryListener);
            return this;
        }

        /**
         * async execute
         * If you wanna a callback, you must {@link #withQueryListener}
         * @return
         */
        public AsyncTask execute()
        {
            return builder
                    .request(requestBuilder.build())
                    .build()
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        /**
         * sync execute
         *
         * It'll disable the QueryListener that you defined;
         *
         * @return
         * @throws Throwable
         */
        public Response syncExecute() throws ResponseException
        {
            CountDownLatch countDownLatch = new CountDownLatch(1);

            final LinkedList<Object> result = new LinkedList<>();

            builder.queryListener(new QueryListener() {
                @Override
                public void onDone(Response response) {
                    result.add(response);
                    countDownLatch.countDown();
                }

                @Override
                public void onError(ResponseException e) {
                    result.add(e);
                    countDownLatch.countDown();
                }
            });

            try {
                // wait for complete
                countDownLatch.await();
            } catch (InterruptedException e)
            {
                Logger.e(TAG, e.getMessage(), e);
            }


            Object first = result.poll();
            if (first instanceof ResponseException)
                throw (ResponseException) first;
            else
                return (Response) first;
        }

    }

    public static Builder option(String url)
    {
        return new Builder(OPTION, url);
    }

    public static Builder HEAD(String url)
    {
        return new Builder(HEAD, url);
    }

    public static Builder get(String url)
    {
        return new Builder(GET, url);
    }

    public static Builder delete(String url, RequestBody body)
    {
        return new Builder(DELETE, url, body);
    }

    public static Builder put(String url, RequestBody body)
    {
        return new Builder(PUT, url, body);
    }

    public static Builder patch(String url, RequestBody body)
    {
        return new Builder(PATCH, url, body);
    }

    public static Builder download(String url, File file)
    {
        return new Builder(GET, url)
                .writeToFile(file);
    }

    public static Builder getWithJson(String url)
    {
        return new Builder(GET, url)
                .withJson();
    }

    public static Builder postWithJson(String url, @NonNull Jsonable data)
    {
        return new Builder(POST, url, data)
                .withJson();
    }

    public static Builder postWithEncrypted(String url, @NonNull Jsonable data)
    {
        return new Builder(POST, url, data)
                .withJson()
                .withEncrypted();
    }
}
