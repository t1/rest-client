package com.github.t1.rest;

import java.net.URI;

import javax.ws.rs.core.MediaType;

import lombok.*;

/**
 * A {@link RestResource} plus headers to be sent. May be typed (see {@link EntityRequest}).
 */
@Getter
public class RestRequest {
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

    public String authority() {
        return resource.authority();
    }

    public URI uri() {
        return resource.uri().toUri();
    }

    public <T> T get(Class<T> type) {
        return accept(type).get();
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

    public RestRequest basicAuth(String userName, String password) {
        return new RestRequest(resource, headers.basicAuth(userName, password));
    }

    public RestRequest header(String name, Object value) {
        return new RestRequest(resource, headers.header(name, value));
    }

    public RestRequest with(String name, String value) {
        return new RestRequest(resource.with(name, value), headers.header(name, value));
    }

    @Override
    public String toString() {
        return resource + " ::: " + headers;
    }
}
