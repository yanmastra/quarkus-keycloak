package com.acme.authorization.utils;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.runtime.util.StringUtil;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.handler.HttpException;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class SimpleHttpCallUtils {
    private SimpleHttpCallUtils() {
    }

    private static final Map<String, URL> urlMap = new HashMap<>();
    private static final Logger logger = Logger.getLogger(SimpleHttpCallUtils.class.getName());

    public static String call(HttpMethod method, String stringUrl, String content, Map<String, String> headers) throws IOException {
        return call(method, stringUrl, content, headers, false);
    }
    public static String call(HttpMethod method, String stringUrl, String content, Map<String, String> headers, boolean showError) throws IOException {

        URL url = null;
        if (urlMap.containsKey(stringUrl)) {
            url = urlMap.get(stringUrl);
        } else {
            url = new URL(stringUrl);
            urlMap.put(stringUrl, url);
        }

        int statusCode = 500;
        try (MyHttpConnection connection = new MyHttpConnection((HttpURLConnection) url.openConnection())){
            connection.setRequestMethod(method.name());
            connection.setUseCaches(false);

            for (String key : headers.keySet()) {
                connection.setRequestProperty(key, headers.get(key));
            }
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (!StringUtil.isNullOrEmpty(content)) {
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(content.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }

            statusCode = connection.getResponseCode();

            if (statusCode == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder respContent = new StringBuilder();

                    String line;
                    while ((line = in.readLine()) != null) {
                        respContent.append(line);
                    }
                    return respContent.toString();
                } catch (Exception e) {
                    if (showError) logger.error(e.getMessage(), e);
                }
            } else {
                if (showError) logger.error("status: " + statusCode);
            }
        } catch (Exception e) {
            if (showError) logger.error(e.getMessage(), e);
            else logger.error(e.getMessage());
            throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), e.getMessage(), e);
        }
        throw new HttpException(statusCode, HttpResponseStatus.valueOf(statusCode).toString());
    }

    public static GetBuilder get(String url) {
        return new GetBuilder(url);
    }

    public static PostBuilder post(String url) {
        return new PostBuilder(url);
    }

    static class MyHttpConnection implements AutoCloseable {
        private final HttpURLConnection connection;

        public MyHttpConnection(HttpURLConnection connection) {
            if (connection == null) throw new NullPointerException("Variable connection couldn't be null");
            this.connection = connection;
            this.connection.setDoOutput(true);
        }

        @Override
        public void close() throws Exception {
            connection.disconnect();
        }

        public void setRequestMethod(String name) throws ProtocolException {
            connection.setRequestMethod(name);
        }

        public void setUseCaches(boolean b) {
            connection.setUseCaches(b);
        }

        public void setRequestProperty(String key, String s) {
            connection.setRequestProperty(key, s);
        }

        public void setConnectTimeout(int i) {
            connection.setConnectTimeout(i);
        }

        public void setReadTimeout(int i) {
            connection.setReadTimeout(i);
        }

        public OutputStream getOutputStream() throws IOException {
            return connection.getOutputStream();
        }

        public int getResponseCode() throws IOException {
            return connection.getResponseCode();
        }

        public InputStream getInputStream() throws IOException {
            return connection.getInputStream();
        }
    }

    public static class GetBuilder extends Builder {
        private GetBuilder(String url) {
            super(url, HttpMethod.GET);
        }
    }

    public static class PostBuilder extends Builder {
        private PostBuilder(String url) {
            super(url, HttpMethod.POST);
        }
    }

    private static class Builder {
        private final HttpMethod method;
        private final String stringUrl;
        private String content;
        private Map<String, String> headers;
        private boolean showError;

        private Builder(String url, HttpMethod method) {
            this.stringUrl = url;
            this.showError = false;
            this.method = method;
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder addHeader(String headerName, String headerValue) {
            if (headers == null) headers = new HashMap<>();
            headers.put(headerName, headerValue);
            return this;
        }

        public Builder addHeaders(Map<String, String> headers) {
            if (this.headers == null) this.headers = new HashMap<>();
            this.headers.putAll(headers);
            return this;
        }

        public Builder showError(boolean b) {
            showError = b;
            return this;
        }

        public String exec() {
            try {
                return call(this.method, this.stringUrl, this.content, this.headers, this.showError);
            } catch (IOException ioe) {
                if (showError) {
                    logger.error(ioe.getMessage(), ioe);
                    return null;
                } else throw new RuntimeException(ioe.getMessage());
            }
        }

        public <T> T exec(Class<T> tClass) {
            String response = exec();
            if (StringUtils.isNotBlank(response)) {
                return JsonUtils.fromJson(response, tClass);
            } else {
                if (showError) return null;
                else throw new RuntimeException("Response is empty!");
            }
        }
    }
}
