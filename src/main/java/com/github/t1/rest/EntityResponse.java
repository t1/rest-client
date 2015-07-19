package com.github.t1.rest;

import java.io.InputStream;

import javax.ws.rs.core.Response.StatusType;

/** Delegates the responsibility for closing the input stream to the converter */
public class EntityResponse<T> extends RestResponse {
    private final ResponseConverter<T> converter;
    private final InputStream inputStream;

    public EntityResponse(RestConfig config, StatusType status, Headers headers, ResponseConverter<T> converter,
            InputStream inputStream) {
        super(config, status, headers);
        this.converter = converter;
        this.inputStream = inputStream;
    }

    @Override
    public EntityResponse<T> expecting(StatusType... expectedTypes) {
        super.expecting(expectedTypes);
        return this;
    }

    public T get() {
        return converter.convert(inputStream, headers());
    }

    public <U> U get(Class<U> type) {
        ResponseConverter<U> otherConverter = config().converterFor(type);
        return otherConverter.convert(inputStream, headers());
    }
}
