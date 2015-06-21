package com.github.t1.rest;

import java.io.IOException;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;

import lombok.Value;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.*;

@Slf4j
public abstract class HttpRequest {
    @Value
    @Accessors(fluent = false)
    private final class UnknownStatus implements StatusType {
        int statusCode;

        @Override
        public Family getFamily() {
            return Family.familyOf(statusCode);
        }

        @Override
        public String getReasonPhrase() {
            return "Unknown";
        }
    }

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

    /** The {@link RestResponse}/{@link EntityResponse} is responsible to close the input stream */
    @SuppressWarnings("resource")
    public RestResponse execute() {
        log.debug("execute {}", request);
        CloseableHttpResponse apacheResponse = null;
        try {
            apacheResponse = CLIENT.execute(request);
            return convert(apacheResponse);
        } catch (ConnectTimeoutException e) {
            close(apacheResponse, e);
            throw new HttpTimeoutException("can't execute " + request, e);
        } catch (IOException e) {
            close(apacheResponse, e);
            throw new RuntimeException("can't execute " + request, e);
        } catch (RuntimeException e) {
            close(apacheResponse, e);
            throw e;
        }
    }

    protected abstract RestResponse convert(CloseableHttpResponse apacheResponse);

    protected Headers convert(org.apache.http.Header[] headers) {
        Headers out = new Headers();
        for (org.apache.http.Header header : headers)
            out = out.header(header.getName(), header.getValue());
        return out;
    }

    protected StatusType status(CloseableHttpResponse apacheResponse) {
        final int code = apacheResponse.getStatusLine().getStatusCode();
        StatusType status = Status.fromStatusCode(code);
        if (status == null)
            status = new UnknownStatus(code);
        return status;
    }

    // we are very conservative with this: if the converting fails, the connection has to be closed
    // but not when it can be passed to the EntityResponse
    private void close(CloseableHttpResponse apacheResponse, Exception e) {
        if (apacheResponse != null) {
            try {
                apacheResponse.close();
            } catch (Exception e2) {
                e.addSuppressed(e2);
            }
        }
    }
}
