package com.github.t1.rest;

import java.io.IOException;
import java.net.*;

import javax.annotation.concurrent.Immutable;
import javax.ws.rs.core.Response.*;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.http.client.methods.*;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Immutable
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
    private final RestContext config;
    @Getter
    private final URI uri;
    @Getter
    private final Headers requestHeaders;
    private final CloseableHttpClient apacheClient;
    private final HttpRequestBase request;

    public RestCall(RestContext config, URI uri, Headers requestHeaders, CloseableHttpClient apacheClient,
            HttpRequestBase request) {
        this.config = config;
        this.uri = uri;
        this.requestHeaders = requestHeaders;
        this.apacheClient = apacheClient;
        this.request = request;
        initRequest();
    }

    private void initRequest() {
        for (Headers.Header header : requestHeaders) {
            request.addHeader(header.name(), header.value());
        }
    }

    public RestResponse execute() {
        log.debug("execute {}", request);
        try (CloseableHttpResponse apacheResponse = apacheClient.execute(request)) {
            return convert(apacheResponse);
        } catch (ConnectTimeoutException | SocketTimeoutException e) {
            throw new HttpTimeoutException("timeout on " + request + ": " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("can't execute " + request + ": " + e.getMessage(), e);
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
}
