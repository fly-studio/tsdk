package org.fly.tsdk.sdk.query.middleware;

import android.util.Log;

import org.fly.core.io.network.result.Result;
import org.fly.core.text.encrytor.Decryptor;
import org.fly.tsdk.sdk.query.Request;
import org.fly.tsdk.sdk.query.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;

public class EncryptBody implements Middleware {
    private static final String TAG = "HTTP_EncryptBody";

    private Decryptor decryptor;

    public EncryptBody() {
        this(new Decryptor());
    }

    public EncryptBody(Decryptor decryptor) {
        this.decryptor = decryptor;
    }

    public Decryptor getDecryptor() {
        return decryptor;
    }

    @Override
    public void before(Request request, List<Object> object) throws Throwable {
        if (null != decryptor)
        {
            //Rewrite body
            RequestBody body = request.getBody();
            if (body != null && body.contentType() != null && "json".equalsIgnoreCase(body.contentType().subtype()))
            {
                try {

                    ByteArrayOutputStream data = new ByteArrayOutputStream();
                    BufferedSink sink = Okio.buffer(Okio.sink(data));
                    body.writeTo(sink);
                    sink.flush();
                    sink.close();
                    Result result = decryptor.encodeData(data.toString(StandardCharsets.UTF_8.toString()));

                    request.setBody(RequestBody.create(MediaType.get("application/json; charset=utf-8"), result.toJson()));
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
            // add RSA Headers
            if (decryptor.getKeyMode() == Decryptor.KEY_MODE.Own) {
                String rsa = "";

                try {
                    rsa = URLEncoder.encode(decryptor.getPublicKey(), StandardCharsets.UTF_8.displayName());
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, e.getMessage(), e);
                }

                request.setHeader("X-RSA", rsa);
            }

        }
    }

    @Override
    public void after(Response response, List<Object> object) throws Throwable {

        Object obj = object.get(object.size() - 1);

        Result result = obj instanceof Result ? (Result) obj : null;
        // decode result
        if (result != null && null != decryptor) {

            if (result.encrypted != null && !result.encrypted.isEmpty())
            {
                result.data = decryptor.decodeData(result);
                result.encrypted = null;
            }
        }
    }

}
