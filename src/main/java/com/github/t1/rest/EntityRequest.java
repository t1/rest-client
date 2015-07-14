package com.github.t1.rest;

import lombok.*;

@Value
@EqualsAndHashCode(callSuper = true)
public class EntityRequest<T> extends RestRequest {
    private final ResponseConverter<T> converter;

    public EntityRequest(RestResource resource, Headers headers, ResponseConverter<T> converter) {
        super(resource, headers);
        this.converter = converter;
    }

    @Override
    public EntityRequest<T> header(String name, Object value) {
        return new EntityRequest<>(resource, headers.header(name, value), converter);
    }

    @Override
    public EntityRequest<T> with(String name, String value) {
        return new EntityRequest<>(resource.with(name, value), headers.with(name, value), converter);
    }

    public T get() {
        return getResponse().get();
    }

    public EntityResponse<T> getResponse() {
        GetRequest<T> request = config().createGetRequest(uri(), headers, converter);
        return request.execute();
    }

    public Class<T> acceptedType() {
        return converter.acceptedType();
    }

    @Override
    public String toString() {
        return super.toString() + " with converters " + converter;
    }
}
