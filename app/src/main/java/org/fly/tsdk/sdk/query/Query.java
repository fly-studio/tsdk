package org.fly.tsdk.sdk.query;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.fly.core.text.encrytor.Decryptor;
import org.fly.core.text.json.Jsonable;
import org.fly.tsdk.sdk.query.middleware.EncryptBody;
import org.fly.tsdk.sdk.query.middleware.JsonFormatter;

import java.io.File;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.MediaType;

public class Query {
    private static final String TAG = "HTTP";

    private Decryptor decryptor;

    public Query() {
        decryptor = new Decryptor();
    }

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

    public void setPublicKey(String key)
    {
        decryptor.setKey(key, null);
    }

    public AsyncTask postWithEncrypted(HttpUrl url, @NonNull Jsonable data, QueryListener queryListener) throws IllegalArgumentException
    {
        return new QueryTask.Builder()
                .request(new Request.Builder()
                        .method("POST")
                        .url(url)
                        .content(data.toJson(), MediaType.get("application/json; charset=utf-8"))
                        .build()
                )
                .queryListener(queryListener)
                .middleware(new JsonFormatter())
                .middleware(new EncryptBody(decryptor))
                .build()
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public AsyncTask post(HttpUrl url, @Nullable Jsonable data, QueryListener queryListener) throws IllegalArgumentException
    {
        return new QueryTask.Builder()
                .request(new Request.Builder()
                        .url(url)
                        .content(data != null ? data.toJson() : null, MediaType.get("application/json; charset=utf-8"))
                        .method("POST")
                        .build()
                )
                .queryListener(queryListener)
                .middleware(new JsonFormatter())
                .build()
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public AsyncTask get(HttpUrl url, QueryListener queryListener) throws IllegalArgumentException
    {
        return new QueryTask.Builder()
                .request(new Request.Builder()
                        .url(url)
                        .method("GET")
                        .build()
                )
                .queryListener(queryListener)
                .middleware(new JsonFormatter())
                .build()
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public AsyncTask download(HttpUrl url, File file, QueryListener queryListener) throws IllegalArgumentException
    {
        return new QueryTask.Builder()
                .request(new Request.Builder()
                        .url(url)
                        .build()
                )
                .file(file)
                .queryListener(queryListener)
                .build()
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

}
