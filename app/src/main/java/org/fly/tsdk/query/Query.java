package org.fly.tsdk.query;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.fly.core.text.encrytor.Decryptor;
import org.fly.core.text.json.Jsonable;
import org.fly.tsdk.query.middleware.EncryptBody;
import org.fly.tsdk.query.middleware.JsonFormatter;

import java.io.File;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
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

    public static HttpUrl buildUrl(String baseUrl, List<Pair<String, String>> queryParameters)
    {
        HttpUrl.Builder builder = HttpUrl.parse(baseUrl).newBuilder();

        if (null != queryParameters)
        {
            for (Pair<String, String> query: queryParameters) {
                builder.addQueryParameter(query.getKey(), query.getValue());
            }
        }

        return builder.build();
    }

    public synchronized static void setPublicKey(String publicKey) {
        decryptor.setKey(publicKey, null);
    }

    public static class Builder {
        private QueryTask.Builder builder = new QueryTask.Builder();
        private Request.Builder requestBuilder = new Request.Builder();
        /**
         * any with json
         * @param url
         * @param data
         */
        public Builder(String method, HttpUrl url, @NonNull Jsonable data) {

            requestBuilder = new Request.Builder()
                    .method(method)
                    .url(url)
                    .content(data.toJson(), ContentType.JSON)
            ;
        }

        /**
         * Any with body
         * @param method
         * @param url
         * @param body
         */
        public Builder(String method, HttpUrl url, RequestBody body)
        {
            requestBuilder = new Request.Builder()
                    .method(method)
                    .url(url)
                    .body(body)
            ;
        }

        /**
         * Any
         * @param url
         */
        public Builder(String method, HttpUrl url)
        {
            requestBuilder = new Request.Builder()
                    .method(method)
                    .url(url)
            ;
        }

        public Builder head(String name, String value)
        {
            requestBuilder.header(name, value);
            return this;
        }

        public Builder cookie(String name, String value) {
            requestBuilder.cookie(name, value);
            return this;
        }

        public Builder retries(int retries)
        {
            builder.retries(retries);
            return this;
        }

        public Builder unlimitRetry()
        {
            return retries(UNLIMIT_RETRY);
        }

        public Builder writeToFile(File file)
        {
            builder.file(file);
            return this;
        }

        public Builder withJson() {
            builder.middleware(new JsonFormatter());
            return this;
        }

        public Builder withEncrypted() {
            builder.middleware(new EncryptBody(decryptor));
            return this;
        }

        public Builder withQueryListener(QueryListener queryListener) {
            builder.queryListener(queryListener);
            return this;
        }

        public AsyncTask execute()
        {
            return builder
                    .request(requestBuilder.build())
                    .build()
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }

    public static Builder option(HttpUrl url)
    {
        return new Builder(OPTION, url);
    }

    public static Builder HEAD(HttpUrl url)
    {
        return new Builder(GET, url);
    }

    public static Builder get(HttpUrl url)
    {
        return new Builder(GET, url);
    }

    public static Builder delete(HttpUrl url, RequestBody body)
    {
        return new Builder(DELETE, url, body);
    }

    public static Builder put(HttpUrl url, RequestBody body)
    {
        return new Builder(PUT, url, body);
    }

    public static Builder patch(HttpUrl url, RequestBody body)
    {
        return new Builder(PATCH, url, body);
    }

    public static Builder download(HttpUrl url, File file)
    {
        return new Builder(GET, url)
                .writeToFile(file);
    }

    public static Builder getWithJson(HttpUrl url)
    {
        return new Builder(GET, url)
                .withJson();
    }

    public static Builder postWithJson(HttpUrl url, @NonNull Jsonable data)
    {
        return new Builder(POST, url, data)
                .withJson();
    }

    public static Builder postWithEncrypted(HttpUrl url, @NonNull Jsonable data)
    {
        return new Builder(POST, url, data)
                .withJson()
                .withEncrypted();
    }
}
