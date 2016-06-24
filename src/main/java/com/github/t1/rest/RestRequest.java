package com.github.t1.rest;

import lombok.*;

import javax.annotation.concurrent.Immutable;
import javax.ws.rs.core.MediaType;
import java.net.URI;

import static javax.ws.rs.core.Response.Status.*;

/**
 * A {@link RestResource} plus the {@link Headers} to be sent.
 */
@Immutable
@Value
public class RestRequest<T> {
    @NonNull
    RestResource resource;
    @NonNull
    Headers headers;
    ResponseConverter<T> converter;

    @Override
    public String toString() {
        return resource + " ::: " + headers;
    }

    public String authority() {
        return resource.authority();
    }

    public RestContext context() {
        return resource.context();
    }

    public URI uri() {
        return resource.uri().toUri();
    }


    public RestRequest<T> basicAuth(Credentials credentials) {
        return new RestRequest<>(resource, headers.basicAuth(credentials), converter);
    }

    public RestRequest<T> header(String name, Object value) {
        return new RestRequest<>(resource, headers.header(name, value), converter);
    }

    public RestRequest<T> with(String name, String value) {
        return new RestRequest<>(resource.with(name, value), headers.with(name, value), converter);
    }


    public T GET() {
        return GET_Response().expecting(OK).get();
    }

    public <U> U GET(Class<U> type) {
        return accept(type).GET();
    }

    /**
     * Execute a GET and return the {@link EntityResponse response object}. This method name is better than getResponse
     * (as it indicates that a GET is executed), and anything else I could think of.
     */
    public EntityResponse<T> GET_Response() {
        RestGetCall<T> request = context().createRestGetCall(uri(), headers, acceptedType());
        return request.execute();
    }

    public EntityResponse<T> POST() {
        return null;
    }

    public Class<T> acceptedType() {
        return (converter == null) ? null : converter.acceptedType();
    }

    public <U> RestRequest<U> accept(Class<U> acceptedType) {
        return entityRequest(resource.context().converterFor(acceptedType));
    }

    public <U> RestRequest<U> accept(Class<?> first, Class<?>... more) {
        ResponseConverter<U> otherConverter = resource.context().converterFor(first, more);
        return entityRequest(otherConverter);
    }

    /**
     * Normally you wouldn't call this directly: the acceptable types are determined by the readers available for the
     * type you passed to {@link #accept(Class)}. Call this method only, if you (must) know that the server would return
     * some content type, that is not complete or otherwise not useful for this request, so you need a different one.
     */
    @Deprecated
    public <U> RestRequest<U> accept(Class<U> acceptedType, MediaType contentType) {
        return entityRequest(resource.context().converterFor(acceptedType, contentType));
    }

    private <U> RestRequest<U> entityRequest(ResponseConverter<U> converter) {
        return new RestRequest<>(resource, headers.accept(converter.mediaTypes()), converter);
    }
}
