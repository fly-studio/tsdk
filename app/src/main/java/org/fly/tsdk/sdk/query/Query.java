package org.fly.tsdk.sdk.query;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.fly.core.text.encrytor.Decryptor;
import org.fly.core.text.json.Jsonable;
import org.fly.tsdk.sdk.query.middleware.EncryptBody;
import org.fly.tsdk.sdk.query.middleware.JsonFormatter;
import org.fly.tsdk.sdk.text.Validator;

import java.io.File;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.MediaType;

public class Query {
    private static final String TAG = "HTTP";

    private Decryptor decryptor;

    public Query() {
        decryptor = new Decryptor();
    }

    public static HttpUrl buildUrl(String baseUrl, Map<String, String> params)
    {
        HttpUrl.Builder builder = HttpUrl.parse(baseUrl).newBuilder();

        for (Map.Entry<String, String> entry: params.entrySet()
             ) {
            builder.addQueryParameter(entry.getKey(), entry.getValue());
        }

        return builder.build();
    }

    private void validateUrl(String url)
    {
        if (!Validator.equalsUrl(url))
            throw new IllegalArgumentException("Invalid URL");
    }

    public void setPublicKey(String key)
    {
        decryptor.setKey(key, null);
    }

    public AsyncTask postWithEncrypted(String url, @NonNull Jsonable data, QueryListener queryListener) throws IllegalArgumentException
    {
        validateUrl(url);

        return new QueryTask.Builder()
                .request(new Request.Builder()
                        .method("POST")
                        .url(HttpUrl.get(url))
                        .content(data.toJson(), MediaType.get("application/json; charset=utf-8"))
                        .build()
                )
                .queryListener(queryListener)
                .middleware(new JsonFormatter())
                .middleware(new EncryptBody(decryptor))
                .build()
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public AsyncTask post(String url, @Nullable Jsonable data, QueryListener queryListener) throws IllegalArgumentException
    {
        validateUrl(url);

        return new QueryTask.Builder()
                .request(new Request.Builder()
                        .url(HttpUrl.get(url))
                        .content(data != null ? data.toJson() : null, MediaType.get("application/json; charset=utf-8"))
                        .method("POST")
                        .build()
                )
                .queryListener(queryListener)
                .middleware(new JsonFormatter())
                .build()
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public AsyncTask get(String url, QueryListener queryListener) throws IllegalArgumentException
    {
        validateUrl(url);

        return new QueryTask.Builder()
                .request(new Request.Builder()
                        .url(HttpUrl.get(url))
                        .method("POST")
                        .build()
                )
                .queryListener(queryListener)
                .middleware(new JsonFormatter())
                .build()
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public AsyncTask download(String url, File file, QueryListener queryListener) throws IllegalArgumentException
    {
        validateUrl(url);
        return new QueryTask.Builder()
                .request(new Request.Builder()
                        .url(HttpUrl.get(url))
                        .build()
                )
                .file(file)
                .queryListener(queryListener)
                .build()
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

}
