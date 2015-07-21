package com.github.t1.rest;

import java.io.IOException;
import java.net.SocketTimeoutException;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import org.apache.http.client.methods.*;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;

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

    public RestCall(RestConfig config, CloseableHttpClient apacheClient, HttpRequestBase request, Headers requestHeaders) {
        this.config = config;
        this.apacheClient = apacheClient;
        this.request = request;
        this.requestHeaders = requestHeaders;
        addRequestHeaders(requestHeaders);
    }

    private void addRequestHeaders(Headers requestHeaders) {
        for (Headers.Header header : requestHeaders) {
            request.addHeader(header.name(), header.value());
        }
    }

    public RestResponse execute() {
        log.debug("execute {}", request);
        try (CloseableHttpResponse apacheResponse = apacheClient.execute(request)) {
            return convert(apacheResponse);
        } catch (ConnectTimeoutException | SocketTimeoutException e) {
            throw new HttpTimeoutException("can't execute " + request, e);
        } catch (IOException e) {
            throw new RuntimeException("can't execute " + request, e);
        } catch (RuntimeException e) {
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
}
