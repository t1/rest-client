package com.github.t1.rest;

import static javax.ws.rs.core.Response.Status.*;

import java.io.InputStream;

import javax.ws.rs.core.Response.StatusType;

public class EntityResponse<T> extends RestResponse {
    private final ResponseConverter<T> converter;
    private final InputStream inputStream;

    public EntityResponse(ResponseConverter<T> converter, StatusType status, Headers headers, InputStream inputStream) {
        super(status, headers);
        this.converter = converter;
        this.inputStream = inputStream;
    }

    public T get() {
        expecting(OK);
        return converter.convert(inputStream, headers());
    }

    @Override
    public EntityResponse<T> expecting(StatusType... expectedTypes) {
        super.expecting(expectedTypes);
        return this;
    }

    public <U> U get(Class<U> type) {
        ResponseConverter<U> otherConverter = EntityRequest.CONFIG.converterFor(type);
        return otherConverter.convert(inputStream, headers());
    }
}
