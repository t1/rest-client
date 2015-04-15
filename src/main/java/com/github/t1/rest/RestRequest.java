package com.github.t1.rest;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.MediaType;

import lombok.Getter;

import org.apache.http.HttpMessage;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;

import com.github.t1.rest.Headers.Header;

/**
 * A {@link RestResource} plus headers to be sent. May be typed (see {@link TypedRestRequest}).
 */
@Getter
public class RestRequest {
    private static final CloseableHttpClient CLIENT = HttpClients.createDefault();

    RestResource resource;
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

    public <T> TypedRestRequest<T> accept(Class<T> acceptedType) {
        return new TypedRestRequest<>(resource, headers, acceptedType);
    }

    /**
     * Normally you wouldn't call this: the acceptable types are determined by the readers available for the type you
     * passed to {@link #accept(Class)}. This method is only needed if you (must) know that the server would return some
     * content type, that is not complete or otherwise not useful for this request, so you need a different one.
     */
    @Deprecated
    public <T> TypedRestRequest<T> accept(Class<T> acceptedType, MediaType contentType) {
        return new TypedRestRequest<>(resource, headers, acceptedType, contentType);
    }

    protected void addHeaders(HttpMessage request) {
        for (Header header : headers.headers()) {
            request.addHeader(header.name(), header.value());
        }
    }

    protected CloseableHttpResponse execute(HttpUriRequest request) throws IOException {
        return CLIENT.execute(request);
    }

    @Override
    public String toString() {
        return resource + " ::: " + headers;
    }

    public RestRequest header(String name, Object value) {
        return new RestRequest(resource, headers.with(name, value));
    }
}
