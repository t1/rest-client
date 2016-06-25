package com.github.t1.rest;

import lombok.Value;

import javax.annotation.concurrent.Immutable;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.List;

import static com.github.t1.rest.RestContext.*;
import static java.util.Arrays.*;

/**
 * Wraps the template for an http/https URI, where a resource is located, and a {@link RestContext context}. If you
 * create a {@link RestResource} without specifying the context, the {@link RestContext#REST default context} is used. A
 * {@link RestResource} is the factory for creating {@link RestRequest}s of various kinds.
 */
@Immutable
@Value
public class RestResource {
    private static final List<String> ALLOWED_SCHEMES = asList("http", "https");

    private final RestContext context;
    private final UriTemplate uri;

    /** resource for that uri, using the {@link RestContext#REST} */
    public RestResource(URI uri) {
        this(UriTemplate.fromString(uri.toString()));
    }

    /** resource for that uri, using the {@link RestContext#REST} */
    public RestResource(String uri) {
        this(UriTemplate.fromString(uri));
    }

    /** resource for that uri, using the {@link RestContext#REST} */
    public RestResource(UriTemplate uri) {
        this(REST, uri);
    }

    public RestResource(RestContext context, URI uri) {
        this(context, UriTemplate.fromString(uri.toString()));
    }

    public RestResource(RestContext context, String uri) {
        this(context, UriTemplate.fromString(uri));
    }

    public RestResource(RestContext context, UriTemplate uri) {
        this.uri = check(uri);
        this.context = context;
    }

    public String authority() {
        return uri.authority();
    }

    private static UriTemplate check(UriTemplate uri) {
        if (!ALLOWED_SCHEMES.contains(uri.scheme()))
            throw new RuntimeException("unsupported scheme for REST: " + uri.scheme());
        return uri;
    }

    public RestRequest<Void> request() {
        return new RestRequest<>(this, new Headers(), null);
    }

    public <T> RestRequest<T> accept(Class<T> acceptedType) {
        return request().accept(acceptedType);
    }

    public RestRequest<?> accept(Class<?> first, Class<?>... more) { return request().accept(first, more); }

    public <T> RestRequest<T> accept(GenericType<T> type) { return request().accept(type); }

    /**
     * Normally you wouldn't call this directly: the acceptable types are determined by the readers available for the
     * type you pass to {@link #accept(Class)}. Call this method only, if you (must) know that the server would return
     * some content type, that is not complete or otherwise not useful for this request, so you need a different one.
     */
    @Deprecated
    public <T> RestRequest<T> accept(Class<T> acceptedType, MediaType contentType) {
        return request().accept(acceptedType, contentType);
    }

    public RestRequest<Void> basicAuth(Credentials credentials) {
        return request().basicAuth(credentials);
    }

    public RestRequest<Void> header(String name, Object value) {
        return request().header(name, value);
    }

    public RestResource with(String name, Object value) {
        return new RestResource(context, uri.with(name, value));
    }

    /**
     * Execute a http GET and return the body as a String,
     * {@link RestResponse#expecting(javax.ws.rs.core.Response.StatusType...) expecting}
     * {@link javax.ws.rs.core.Response.Status#OK OK}
     */
    public String GET() {
        return GET(String.class);
    }

    /**
     * Execute a http GET and return the body of that type,
     * {@link RestResponse#expecting(javax.ws.rs.core.Response.StatusType...) expecting}
     * {@link javax.ws.rs.core.Response.Status#OK OK}
     */
    public <T> T GET(Class<T> acceptedType) {
        return accept(acceptedType).GET();
    }

    public EntityResponse<Object> GET_Response() {
        return accept(Object.class).GET_Response();
    }

    public EntityResponse<Object> POST_Response() {
        return accept(Object.class).POST_Response();
    }

    public <T> EntityResponse<T> GET_Response(Class<T> acceptedType) {
        return accept(acceptedType).GET_Response();
    }

    public RestResponse PUT(Object value) {
        return null;
    }

    public RestResponse PUT(MediaType type, Object value) {
        return null;
    }

    @Override
    public String toString() { return uri.toString(); }

    public EntityResponse POST() { return null; }
}
