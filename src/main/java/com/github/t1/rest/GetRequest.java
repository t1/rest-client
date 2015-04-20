package com.github.t1.rest;

import static javax.ws.rs.core.Response.Status.*;

import java.io.*;
import java.net.URI;

import javax.ws.rs.core.Response.Status;

import lombok.extern.slf4j.Slf4j;

import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;

@Slf4j
public class GetRequest {
    private static final CloseableHttpClient CLIENT = HttpClients.createDefault();

    private final Headers requestHeaders;
    private final HttpUriRequest request;

    public GetRequest(URI uri, Headers requestHeaders) {
        this.requestHeaders = requestHeaders;
        this.request = new HttpGet(uri);
        addRequestHeaders();
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

    protected void addRequestHeaders() {
        for (Headers.Header header : requestHeaders) {
            request.addHeader(header.name(), header.value());
        }
    }

    protected CloseableHttpResponse execute(HttpUriRequest request) throws IOException {
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

    protected Headers convert(org.apache.http.Header[] headers) {
        Headers out = new Headers();
        for (org.apache.http.Header header : headers)
            out = out.header(header.getName(), header.getValue());
        return out;
    }
}
