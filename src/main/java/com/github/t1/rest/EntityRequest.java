package com.github.t1.rest;

import javax.ws.rs.core.MediaType;

import lombok.*;

@Value
@EqualsAndHashCode(callSuper = true)
public class EntityRequest<T> extends RestRequest {
    public static final RestConfig CONFIG = new RestConfig();

    public static <T> EntityRequest<T> of(RestResource resource, Class<T> acceptedType, Headers headers) {
        return of(resource, headers, CONFIG.converterFor(acceptedType));
    }

    public static EntityRequest<?> of(RestResource resource, Headers headers, Class<?> first, Class<?>... more) {
        return of(resource, headers, CONFIG.converterFor(first, more));
    }

    /**
     * Normally you wouldn't call this directly: the acceptable types are determined by the readers available for the
     * type passed in. Call this method only, if you (must) know that the server would return some content type, that is
     * not complete or otherwise not useful for this request, so you need a different one.
     */
    @Deprecated
    public static <T> EntityRequest<T> of(RestResource resource, Class<T> acceptedType, MediaType contentType,
            Headers headers) {
        return of(resource, headers, CONFIG.converterFor(acceptedType, contentType));
    }

    public static <T> EntityRequest<T> of(RestResource resource, Headers headers, ResponseConverter<T> converter) {
        return new EntityRequest<>(resource, converter, headers.accept(converter.mediaTypes()));
    }

    private final ResponseConverter<T> converter;

    private EntityRequest(RestResource resource, ResponseConverter<T> converter, Headers headers) {
        super(resource, headers);
        this.converter = converter;
    }

    @Override
    public EntityRequest<T> header(String name, Object value) {
        return new EntityRequest<>(resource, converter, headers.header(name, value));
    }

    @Override
    public EntityRequest<T> with(String name, String value) {
        return new EntityRequest<>(resource.with(name, value), converter, headers.header(name, value));
    }

    public T get() {
        return getResponse().get();
    }

    public EntityResponse<T> getResponse() {
        GetRequest<T> request = CONFIG.createGetRequest(uri(), headers, converter);
        return request.execute();
    }
}
