package com.github.t1.rest;

import java.io.*;

import javax.ws.rs.core.MediaType;

import lombok.*;

import org.apache.http.client.methods.*;

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

    public static <T> EntityRequest<T> of(RestResource resource, Headers headers, RestConverter<T> converter) {
        return new EntityRequest<>(resource, converter, headers.accept(converter.mediaTypes()));
    }

    private final RestConverter<T> converter;

    private EntityRequest(RestResource resource, RestConverter<T> converter, Headers headers) {
        super(resource, headers);
        this.converter = converter;
    }

    public T get() {
        return getResponse().get();
    }

    /** the converter will close the stream */
    @SuppressWarnings("resource")
    public EntityResponse<T> getResponse() {
        try {
            CloseableHttpResponse apacheResponse = execute(new HttpGet(uri()));
            Headers headers = convert(apacheResponse.getAllHeaders());
            InputStream inputStream = apacheResponse.getEntity().getContent();
            EntityResponse<T> entityResponse = new EntityResponse<>(converter, headers, inputStream);
            return entityResponse;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public EntityRequest<T> header(String name, Object value) {
        return new EntityRequest<>(resource, converter, headers.header(name, value));
    }

    @Override
    public EntityRequest<T> with(String name, String value) {
        return new EntityRequest<>(resource.with(name, value), converter, headers.header(name, value));
    }
}
