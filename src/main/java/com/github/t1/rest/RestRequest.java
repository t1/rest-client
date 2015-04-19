package com.github.t1.rest;

import static javax.ws.rs.core.Response.Status.*;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;

/**
 * A {@link RestResource} plus headers to be sent. May be typed (see {@link EntityRequest}).
 */
@Slf4j
@Getter
public class RestRequest {
    private static final CloseableHttpClient CLIENT = HttpClients.createDefault();

    @NonNull
    RestResource resource;
    @NonNull
    Headers headers;

    public RestRequest(RestResource resource) {
        this(resource, new Headers());
    }

    public RestRequest(RestResource resource, Headers headers) {
        this.resource = resource;
        this.headers = headers;
    }

    public URI uri() {
        return resource.uri().toUri();
    }

    public <T> EntityRequest<T> accept(Class<T> acceptedType) {
        return EntityRequest.of(resource, acceptedType, headers);
    }

    public EntityRequest<?> accept(Class<?> first, Class<?>... more) {
        return EntityRequest.of(resource, headers, first, more);
    }

    /**
     * Normally you wouldn't call this directly: the acceptable types are determined by the readers available for the
     * type you passed to {@link #accept(Class)}. Call this method only, if you (must) know that the server would return
     * some content type, that is not complete or otherwise not useful for this request, so you need a different one.
     */
    @Deprecated
    public <T> EntityRequest<T> accept(Class<T> acceptedType, MediaType contentType) {
        return EntityRequest.of(resource, acceptedType, contentType, headers);
    }

    public RestRequest header(String name, Object value) {
        return new RestRequest(resource, headers.with(name, value));
    }

    protected void addHeaders(HttpMessage request) {
        for (Headers.Header header : headers) {
            request.addHeader(header.name(), header.value());
        }
    }

    protected CloseableHttpResponse execute(HttpUriRequest request) throws IOException {
        log.debug("execute {}", request);
        addHeaders(request);
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

    protected Headers convert(Header[] headers) {
        Headers out = new Headers();
        for (Header header : headers)
            out = out.with(header.getName(), header.getValue());
        return out;
    }

    @Override
    public String toString() {
        return resource + " ::: " + headers;
    }
}
