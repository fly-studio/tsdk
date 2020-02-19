package org.fly.tsdk.sdk.query;

import org.fly.tsdk.sdk.query.cookie.CookieJarImpl;
import org.fly.tsdk.sdk.query.cookie.MemoryCookieStore;

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

    public static class Builder {
        protected Request request = new Request();

        public Builder method(String method) {
            request.setMethod(method);
            return this;
        }

        public Builder url(HttpUrl url) {
            request.setUrl(url);
            return this;
        }

        public Builder header(String name, String value) {
            request.setHeader(name, value);
            return this;
        }

        public Builder cookie(String name, String value) {
            request.setCookie(name, value);
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
            return request;
        }
    }
}
