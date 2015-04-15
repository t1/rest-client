package com.github.t1.rest;

import static java.util.Arrays.*;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MediaType;

import lombok.Getter;

import com.github.t1.rest.UriTemplate.NonFragment;
import com.github.t1.rest.UriTemplate.UriPath;

/**
 * Basically just the template for an URI, where a resource is located. The {@link URI#getScheme() scheme} must be
 * either <code>http</code> or <code>https</code>.
 */
@Getter
public class RestResource {
    private static final List<String> ALLOWED_SCHEMES = asList("http", "https");

    private final NonFragment uri;

    public RestResource(URI uri) {
        this(UriTemplate.fromString(uri.toString()));
    }

    public RestResource(String uri) {
        this(UriTemplate.fromString(uri));
    }

    public RestResource(UriTemplate uri) {
        this.uri = check(uri);
    }

    private static NonFragment check(UriTemplate uri) {
        if (!ALLOWED_SCHEMES.contains(uri.scheme()))
            throw new RuntimeException("unsupported scheme for REST: " + uri.scheme());
        return (NonFragment) uri;
    }

    public RestResource path(String path) {
        NonFragment subPath = ((UriPath) this.uri).path(path);
        return new RestResource(subPath);
    }

    public RestResource query(String key, String value) {
        NonFragment subPath = this.uri.query(key, value);
        return new RestResource(subPath);
    }

    public RestRequest request() {
        return new RestRequest(this);
    }

    public <T> TypedRestRequest<T> accept(Class<T> acceptedType) {
        return request().accept(acceptedType);
    }

    /**
     * Normally you wouldn't call this: the acceptable types are determined by the readers available for the type you
     * pass to {@link #accept(Class)}. This method is only needed if you (must) know that the server would return some
     * content type, that is not complete or otherwise not useful for this request, so you need a different one.
     */
    @Deprecated
    public <T> TypedRestRequest<T> accept(Class<T> acceptedType, MediaType contentType) {
        return request().accept(acceptedType, contentType);
    }

    public RestRequest header(String name, Object value) {
        return request().header(name, value);
    }

    public <T> T get(Class<T> acceptedType) {
        return accept(acceptedType).get();
    }

    public InputStream getStream() {
        // TODO Auto-generated method stub
        return null;
    }

    public RestResource with(String name, Object value) {
        return new RestResource(uri.with(name, value));
    }

    @Override
    public String toString() {
        return uri.toString();
    }
}
