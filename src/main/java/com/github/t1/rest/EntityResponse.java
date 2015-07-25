package com.github.t1.rest;

import java.io.*;

import javax.ws.rs.core.Response.StatusType;

public class EntityResponse<T> extends RestResponse {
    private final ResponseConverter<T> converter;
    private final byte[] body;

    public EntityResponse(RestContext config, StatusType status, Headers headers, ResponseConverter<T> converter,
            byte[] body) {
        super(config, status, headers);
        this.body = body;
        this.converter = converter;
    }

    @Override
    public EntityResponse<T> expecting(StatusType... expectedTypes) {
        super.expecting(expectedTypes);
        return this;
    }

    public T get() {
        return converter.convert(inputStream(), headers());
    }

    public <U> U get(Class<U> type) {
        ResponseConverter<U> otherConverter = config().converterFor(type);
        return otherConverter.convert(inputStream(), headers());
    }

    private InputStream inputStream() {
        return new ByteArrayInputStream(body);
    }
}
