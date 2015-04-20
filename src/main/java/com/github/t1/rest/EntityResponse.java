package com.github.t1.rest;

import java.io.InputStream;

public class EntityResponse<T> extends RestResponse {
    private final ResponseConverter<T> converter;
    private final InputStream inputStream;

    public EntityResponse(ResponseConverter<T> converter, Headers headers, InputStream inputStream) {
        super(headers);
        this.converter = converter;
        this.inputStream = inputStream;
    }

    public T get() {
        return converter.convert(inputStream, headers());
    }

    public <U> U get(Class<U> type) {
        ResponseConverter<U> otherConverter = EntityRequest.CONFIG.converterFor(type);
        return otherConverter.convert(inputStream, headers());
    }
}
