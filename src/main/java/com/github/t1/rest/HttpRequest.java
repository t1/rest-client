package com.github.t1.rest;

import static javax.ws.rs.core.Response.Status.*;

import java.io.*;

import javax.ws.rs.core.Response.Status;

import lombok.extern.slf4j.Slf4j;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;

@Slf4j
public abstract class HttpRequest {
    private static final CloseableHttpClient CLIENT = HttpClients.createDefault();

    public static final int DEFAULT_CONNECTION_REQUEST_TIMEOUT = 1;
    public static final int DEFAULT_CONNECT_TIMEOUT = 1_000;
    public static final int DEFAULT_SOCKET_TIMEOUT = 5_000;

    private final RequestConfig config = RequestConfig.custom() //
            .setConnectionRequestTimeout(DEFAULT_CONNECTION_REQUEST_TIMEOUT) //
            .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT) //
            .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT) //
            .build();

    private final Headers requestHeaders;
    private final HttpRequestBase request;

    public HttpRequest(HttpRequestBase request, Headers requestHeaders) {
        this.requestHeaders = requestHeaders;
        this.request = request;
        addRequestHeaders();
        setConfig();
    }

    private void addRequestHeaders() {
        for (Headers.Header header : requestHeaders) {
            request.addHeader(header.name(), header.value());
        }
    }

    private void setConfig() {
        request.setConfig(config);
    }

    /** The {@link EntityResponse} is responsible to close the input stream */
    @SuppressWarnings("resource")
    public <T> EntityResponse<T> execute(ResponseConverter<T> converter) {
        try {
            CloseableHttpResponse apacheResponse = execute(request);
            Headers responseHeaders = convert(apacheResponse.getAllHeaders());
            InputStream responseStream = apacheResponse.getEntity().getContent();
            return new EntityResponse<>(converter, responseHeaders, responseStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CloseableHttpResponse execute(HttpUriRequest request) throws IOException {
        log.debug("execute {}", request);
        CloseableHttpResponse response = CLIENT.execute(request);
        expecting(response, OK);
        return response;
    }

    private void expecting(CloseableHttpResponse response, Status expectedStatus) {
        if (!isStatus(response, expectedStatus))
            throw new RuntimeException("expected status " + expectedStatus.getStatusCode() + " "
                    + expectedStatus.getReasonPhrase() + " but got " + response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase());
    }

    private boolean isStatus(CloseableHttpResponse response, Status expected) {
        return response.getStatusLine().getStatusCode() == expected.getStatusCode();
    }

    private Headers convert(org.apache.http.Header[] headers) {
        Headers out = new Headers();
        for (org.apache.http.Header header : headers)
            out = out.header(header.getName(), header.getValue());
        return out;
    }
}
