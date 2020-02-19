package org.fly.tsdk.sdk.query;

import org.fly.tsdk.sdk.query.cookie.CookieJarImpl;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;
import okhttp3.HttpUrl;
import okhttp3.MediaType;

public class Response {
    private static final String TAG = "HTTP_Response";
    private HttpUrl url;
    private int code = 200;
    private String status = "OK";
    private Map<String, String> headers = new HashMap<>();
    private CookieJarImpl cookies;
    private MediaType contentType;
    private String content;
    private File file;

    private Response() {}

    public HttpUrl getUrl() {
        return url;
    }

    public Response setUrl(HttpUrl url) {
        this.url = url;
        return this;
    }

    public int getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public Response setStatus(int code, String status) {
        this.code = code;
        this.status = status;
        return this;
    }

    public Response setContent(String content, MediaType mediaType) {
        this.content = content;
        this.contentType = mediaType;

        return this;
    }

    public String getContent() {
        return content;
    }

    public Response setFile(File file, MediaType mediaType) {
        this.file = file;
        this.contentType = mediaType;

        return this;
    }

    public File getFile() {
        return file;
    }

    public Response setHeader(String name, String value)
    {
        headers.put(name.toUpperCase(), value);
        return this;
    }

    public Response setHeaders(Map<String, String> headers)
    {
        this.headers = headers;
        return this;
    }

    public String getHeader(String name)
    {
        return headers.get(name.toUpperCase());
    }

    public Map<String, String> getHeaders() {
        return headers;
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

    public Response setCookieJar(CookieJarImpl cookieJar) {
        this.cookies = cookieJar;
        return this;
    }

    public CookieJarImpl getCookieJar() {
        return cookies;
    }

    public long getContentLength() {
        return file != null ? file.length() : content.length();
    }

    public MediaType getContentType() {
        return contentType;
    }

    public static class Builder {
        private Response response = new Response();

        public Builder url(HttpUrl url) {
            response.setUrl(url);
            return this;
        }

        public Builder headers(Map<String, String> headers)
        {
            response.setHeaders(headers);
            return this;
        }

        public Builder header(String name, String value)
        {
            response.setHeader(name, value);
            return this;
        }

        public Builder cookieJar(CookieJarImpl cookieJar)
        {
            response.setCookieJar(cookieJar);
            return this;
        }

        public Builder content(String content, MediaType mediaType)
        {
            response.setContent(content, mediaType);
            return this;
        }

        public Builder file(File file, MediaType mediaType)
        {
            response.setFile(file, mediaType);
            return this;
        }

        public Builder status(int code, String status)
        {
            response.setStatus(code, status);
            return this;
        }

        public Response build() {
            return response;
        }
    }
}
