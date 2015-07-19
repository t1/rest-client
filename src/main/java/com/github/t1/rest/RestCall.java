package com.github.t1.rest;

import java.io.IOException;
import java.net.SocketTimeoutException;

import javax.ws.rs.core.Response.*;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.http.client.methods.*;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class RestCall {
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

    @Getter
    private final RestConfig config;
    private final CloseableHttpClient apacheClient;
    private final HttpRequestBase request;
    @Getter
    private final Headers requestHeaders;

    public RestCall(RestConfig config, CloseableHttpClient apacheClient, HttpRequestBase request,
            Headers requestHeaders) {
        this.config = config;
        this.apacheClient = apacheClient;
        this.request = request;
        this.requestHeaders = requestHeaders;
        addRequestHeaders(requestHeaders);
        // request.setConfig(config);
    }

    private void addRequestHeaders(Headers requestHeaders) {
        for (Headers.Header header : requestHeaders) {
            request.addHeader(header.name(), header.value());
        }
    }

    /** The {@link RestResponse}/{@link EntityResponse} is responsible to close the input stream */
    // if converting fails, the connection has to be closed, but not when it can be passed to the EntityResponse
    @SuppressWarnings("resource")
    public RestResponse execute() {
        log.debug("execute {}", request);
        CloseableHttpResponse apacheResponse = null;
        try {
            apacheResponse = apacheClient.execute(request);
            return convert(apacheResponse);
        } catch (ConnectTimeoutException | SocketTimeoutException e) {
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
