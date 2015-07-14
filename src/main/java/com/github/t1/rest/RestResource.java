package com.github.t1.rest;

import static com.github.t1.rest.RestConfig.*;
import static java.util.Arrays.*;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MediaType;

import com.github.t1.rest.UriTemplate.*;

import lombok.Value;

/**
 * Basically just the template for an URI, where a resource is located. The {@link URI#getScheme() scheme} must be
 * either <code>http</code> or <code>https</code>.
 */
@Value
public class RestResource {
    private static final List<String> ALLOWED_SCHEMES = asList("http", "https");

    private final RestConfig config;
    private final NonFragment uri;

    /** resource for that uri, using the {@link RestConfig#DEFAULT_CONFIG} */
    public RestResource(URI uri) {
        this(UriTemplate.fromString(uri.toString()));
    }

    /** resource for that uri, using the {@link RestConfig#DEFAULT_CONFIG} */
    public RestResource(String uri) {
        this(UriTemplate.fromString(uri));
    }

    /** resource for that uri, using the {@link RestConfig#DEFAULT_CONFIG} */
    public RestResource(UriTemplate uri) {
        this(DEFAULT_CONFIG, uri);
    }

    public RestResource(RestConfig config, URI uri) {
        this(config, UriTemplate.fromString(uri.toString()));
    }

    public RestResource(RestConfig config, String uri) {
        this(config, UriTemplate.fromString(uri));
    }

    public RestResource(RestConfig config, UriTemplate uri) {
        this.uri = check(uri);
        this.config = config;
    }

    public String authority() {
        return uri.authority();
    }

    private static NonFragment check(UriTemplate uri) {
        if (!ALLOWED_SCHEMES.contains(uri.scheme()))
            throw new RuntimeException("unsupported scheme for REST: " + uri.scheme());
        return (NonFragment) uri;
    }

    public RestResource path(String path) {
        NonFragment subPath = ((UriPath) this.uri).parsePath(path);
        return new RestResource(subPath);
    }

    public RestResource matrix(String name, Object value) {
        NonFragment subPath = ((UriPath) this.uri).matrix(name, value.toString());
        return new RestResource(subPath);
    }

    public RestResource query(String key, String value) {
        NonFragment subPath = this.uri.query(key, value);
        return new RestResource(subPath);
    }

    public RestRequest request() {
        return new RestRequest(this);
    }

    public <T> EntityRequest<T> accept(Class<T> acceptedType) {
        return request().accept(acceptedType);
    }

    public EntityRequest<?> accept(Class<?> first, Class<?>... more) {
        return request().accept(first, more);
    }

    /**
     * Normally you wouldn't call this directly: the acceptable types are determined by the readers available for the
     * type you pass to {@link #accept(Class)}. Call this method only, if you (must) know that the server would return
     * some content type, that is not complete or otherwise not useful for this request, so you need a different one.
     */
    @Deprecated
    public <T> EntityRequest<T> accept(Class<T> acceptedType, MediaType contentType) {
        return request().accept(acceptedType, contentType);
    }

    public RestRequest basicAuth(String userName, String password) {
        return request().basicAuth(userName, password);
    }

    public RestRequest header(String name, Object value) {
        return request().header(name, value);
    }

    public RestResource with(String name, Object value) {
        return new RestResource(uri.with(name, value));
    }

    public <T> EntityResponse<T> getResponse(Class<T> acceptedType) {
        return accept(acceptedType).getResponse();
    }

    public EntityResponse<Object> getResponse() {
        return accept(Object.class).getResponse();
    }

    public <T> T get(Class<T> acceptedType) {
        return getResponse(acceptedType).get();
    }

    @Override
    public String toString() {
        return uri.toString();
    }

    public RestResponse put(Object value) {
        return null;
    }

    public RestResponse put(MediaType type, Object value) {
        return null;
    }
}
