package org.fly.tsdk.query;

import android.os.AsyncTask;

import org.apache.commons.lang3.StringUtils;
import org.fly.tsdk.query.exceptions.ResponseException;
import org.fly.tsdk.query.middleware.Middleware;
import org.fly.tsdk.structs.AsyncTaskResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;

public class QueryTask extends AsyncTask<Void, Long, AsyncTaskResult<Response>> {
    private static final String TAG = "HTTP_Task";
    public static final int DOWNLOAD_CHUNK_SIZE = 2048; //Same as Okio Segment.SIZE

    private Request request;
    private List<Middleware> middlewareList = new ArrayList<>();
    private QueryListener queryListener;
    private File file;
    final private LinkedList<Object> middlewareObjects  = new LinkedList<>();

    private QueryTask() {

    }

    public QueryTask setRequest(Request request) {
        this.request = request;
        return this;
    }

    public QueryTask setFile(File file) {
        this.file = file;
        return this;
    }

    public QueryTask setQueryListener(QueryListener queryListener) {
        this.queryListener = queryListener;
        return this;
    }

    public void addMiddleware(Middleware middleware)
    {
        middlewareList.add(middleware);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected void onPostExecute(AsyncTaskResult<Response> asyncTaskResult) {
        super.onPostExecute(asyncTaskResult);

        if (queryListener == null)
            return;

        if (isCancelled())
        {
            queryListener.onError(new ResponseException("Request be cancelled", 400), middlewareObjects);

        } else if (asyncTaskResult.getError() != null)
        {
            Throwable e = asyncTaskResult.getError();
            queryListener.onError(e instanceof ResponseException ? (ResponseException) e : new ResponseException(e.getMessage(), 500, e) , middlewareObjects);

        } else {
            queryListener.onDone(asyncTaskResult.getResult(), middlewareObjects);

            if (file != null)
                queryListener.onDownloaded(file, middlewareObjects);
        }
    }

    @Override
    protected void onProgressUpdate(Long... values) {
        super.onProgressUpdate(values);

        //trigger progress
        if (queryListener != null)
            queryListener.onProgress(values[0], values[1]);
    }


    @Override
    protected AsyncTaskResult<Response> doInBackground(Void... calls) {

        try {

            for (Middleware middleware: middlewareList) {
                middleware.before(request, middlewareObjects);
            }

            Response response = call(request);

            for (Middleware middleware: middlewareList) {
                middleware.after(response, middlewareObjects);
            }

            // server error
            if (response.getCode() != 200)
            {
                throw new ResponseException(response.getStatus() + ": " + response.getContent(), response.getCode());
            }

            return new AsyncTaskResult<>(response);

        } catch (Throwable e) {

            publishProgress(0L, 0L);
            return new AsyncTaskResult<>(e);
        }

    }


    protected Response call(Request request) throws Throwable {

        OkHttpClient okHttpClient = getHttpClient(request);

        final okhttp3.Request httpRequest = getHttpRequest(request);

        Call call = okHttpClient.newCall(httpRequest);

        okhttp3.Response httpResponse = call.execute();
        ResponseBody body = httpResponse.body();

        Response.Builder builder = new Response.Builder()
                .url(request.getUrl())
                .cookieJar(request.getCookieJar())
                .status(httpResponse.code(), httpResponse.message());

        if (body != null)
        {
            Sink sink;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            if (file != null)
            {
                if (!file.exists())
                {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }

                sink = Okio.sink(file);
            } else {
                sink = Okio.sink(byteArrayOutputStream);
            }

            long read, totalRead = 0;
            long contentLength = body.contentLength();

            publishProgress(totalRead, contentLength);

            if (contentLength > 0 || contentLength == -1) {
                BufferedSource source = body.source();
                BufferedSink bufferedSink = Okio.buffer(sink);

                while (!source.exhausted() && (read = source.read(bufferedSink.buffer(), DOWNLOAD_CHUNK_SIZE)) != -1) {
                    totalRead += read;
                    publishProgress(totalRead, contentLength);
                }

                bufferedSink.writeAll(source);
                bufferedSink.flush();
                bufferedSink.close();
            } else {
                publishProgress(0L, 0L);
            }

            if (file != null)
                builder.file(file, body.contentType());
            else
                builder.content(byteArrayOutputStream.toString(), body.contentType());

        }

        for (Map.Entry<String, List<String>> entry: httpResponse.headers().toMultimap().entrySet()
        ) {
            builder.header(entry.getKey(), StringUtils.joinWith(";", entry.getValue()));
        }

        if (body != null)
            body.close();

        httpResponse.close();

        return builder.build();
    }

    protected OkHttpClient getHttpClient(Request request)
    {
        return new OkHttpClient.Builder()
                .writeTimeout(5000, TimeUnit.MILLISECONDS)
                .readTimeout(5000, TimeUnit.MILLISECONDS)
                .connectTimeout(5000, TimeUnit.MILLISECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .cookieJar(request.getCookieJar())
                .build();
    }

    protected okhttp3.Request getHttpRequest(Request request)
    {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder()
                .url(request.getUrl())
                ;

        for (Map.Entry<String, String> entry: request.getHeaders().entrySet()
        ) {
            builder.header(entry.getKey(), entry.getValue());
        }

        builder.method(request.getMethod(), request.getBody());

        return builder.build();
    }

    public static class Builder {

        private QueryTask queryTask = new QueryTask();

        public Builder()
        {

        }

        public Builder request(Request request) {
            queryTask.setRequest(request);
            return this;
        }

        public Builder queryListener(QueryListener queryListener) {
            queryTask.setQueryListener(queryListener);
            return this;
        }

        public Builder middleware(Middleware middleware) {
            queryTask.addMiddleware(middleware);
            return this;
        }

        public Builder file(File file) {
            queryTask.setFile(file);
            return this;
        }

        public QueryTask build() {
            return queryTask;
        }
    }
}