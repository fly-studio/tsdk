package org.fly.tsdk.query;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.fly.tsdk.io.Logger;
import org.fly.tsdk.query.cookie.CookieJarImpl;
import org.fly.tsdk.query.cookie.MemoryCookieStore;
import org.fly.tsdk.text.Validator;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class Request {
    private static final String TAG = "HTTP_Request";

    private String method = "GET";
    private HttpUrl url;
    private Map<String, String> headers = new HashMap<>();
    private static final CookieJarImpl cookies = new CookieJarImpl(new MemoryCookieStore());
    private RequestBody body;

    private Request() {
    }

    public HttpUrl getUrl() {
        return url;
    }

    public Request setMethod(String method) {
        this.method = method;
        return this;
    }

    public String getMethod() {
        return method.toUpperCase();
    }

    public Request setUrl(HttpUrl url) {

        validateUrl(url.toString());

        this.url = url;
        return this;
    }

    public Request setContent(String content, MediaType mediaType) {
        this.body = content == null ? null : RequestBody.create(mediaType, content);

        return this;
    }

    public Request setContent(String content) {
        this.body = RequestBody.create(ContentType.DEFAULT, content);

        return this;
    }

    public RequestBody getBody() {
        return body;
    }

    public Request setBody(RequestBody body) {
        this.body = body;
        return this;
    }

    public Request setHeader(String name, String value)
    {
        headers.put(name.toUpperCase(), value);
        return this;
    }

    public String getHeader(String name)
    {
        return headers.get(name.toUpperCase());
    }

    public Request removeHeader(String name)
    {
        headers.remove(name);
        return this;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Request setCookie(String name, String value)
    {
        cookies.getCookieStore().add(url, new Cookie.Builder()
                .name(name)
                .value(value)
                .domain(url.host())
                .path("/")
                .build());
        return this;
    }

    public String getCookie(String name)
    {
        List<Cookie> cookieList = cookies.getCookieStore().get(url);
        for (Cookie cookie: cookieList
             ) {
            if (cookie.name().equalsIgnoreCase(name))
                return cookie.value();
        }

        return null;
    }

    public List<Cookie> getCookies()
    {
        return cookies.getCookieStore().get(url);
    }

    public CookieJarImpl getCookieJar() {
        return cookies;
    }

    public MediaType getContentType() {
        return getBody() != null ? getBody().contentType() : MediaType.get(getHeader("content-type"));
    }

    private void validateUrl(String url)
    {
        if (!Validator.equalsUrl(url))
            throw new IllegalArgumentException("Invalid URL: " + url);
    }

    public static class Builder {
        protected Request request = new Request();
        private String url;
        private Map<String, String> urlParameters = new HashMap<>();
        private List<Pair<String, String>> queryParameters = new ArrayList<>();

        public Builder(String url) {
            this.url = url;
        }

        public Builder()
        {

        }

        public Builder method(String method) {
            request.setMethod(method);
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder urlParameter(final String name, String value)
        {
            urlParameters.put(name, value);
            return this;
        }

        public Builder urlParameters(final Map<String, String> urlParameters) {
            this.urlParameters = urlParameters;
            return this;
        }

        public Builder queryParameter(final String name, String value)
        {
            queryParameters.add(Pair.of(name, value));
            return this;
        }

        public Builder queryParameters(final List<Pair<String, String>> queryParameters)
        {
            this.queryParameters = queryParameters;
            return this;
        }

        public Builder header(String name, String value) {
            request.setHeader(name, value);
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            for (Map.Entry<String, String> entry : headers.entrySet()
                    ) {
                header(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public Builder cookie(String name, String value) {
            request.setCookie(name, value);
            return this;
        }

        public Builder cookies(Map<String, String> cookies) {
            for (Map.Entry<String, String> entry : cookies.entrySet()
                    ) {
                cookie(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public Builder body(RequestBody body) {
            request.setBody(body);
            return this;
        }

        public Builder content(String content) {
            request.setContent(content);
            return this;
        }

        public Builder content(String content, MediaType mediaType) {
            request.setContent(content, mediaType);
            return this;
        }

        public Request build() {

            for (Map.Entry<String, String> entry: urlParameters.entrySet()) {
                try {
                    String value = StringUtils.isEmpty(entry.getValue()) ? "" : URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString());
                    url = url.replace("{" + entry.getKey() + "}",  value);
                } catch (UnsupportedEncodingException e) {
                    Logger.e(TAG, e.getMessage(), e);
                }
            }

            HttpUrl.Builder httpUrlBuilder = HttpUrl.get(url).newBuilder();

            for (Pair<String, String> param: queryParameters
                 ) {
                httpUrlBuilder.addQueryParameter(param.getKey(), param.getValue());
            }

            request.setUrl(httpUrlBuilder.build());

            return request;
        }


    }
}
