package org.fly.tsdk.query.middleware;

import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.fly.core.io.network.result.EncryptedResult;
import org.fly.core.io.network.result.Result;
import org.fly.core.text.encrytor.Decryptor;
import org.fly.tsdk.query.Request;
import org.fly.tsdk.query.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

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
    public void before(Request request, LinkedList<Object> object) throws Throwable {
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
                    EncryptedResult result = decryptor.encodeData(data.toString(StandardCharsets.UTF_8.toString()));

                    if (result != null)
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
    public void after(Response response, LinkedList<Object> object) throws Throwable {

        Object obj = !object.isEmpty() ? object.getLast() : null;

        Result result = obj instanceof Result ? (Result) obj : null;
        // decode result
        if (result != null && null != decryptor) {

            if (!StringUtils.isEmpty(result.encrypted))
            {
                result.data = decryptor.decodeData(result);
                result.encrypted = null;
            }
        }
    }

}
