package com.github.t1.rest;

import javax.annotation.concurrent.Immutable;
import javax.ws.rs.core.Response.StatusType;
import java.io.*;

@Immutable
public class EntityResponse<T> extends RestResponse {
    private final ResponseConverter<T> converter;
    private final byte[] body;

    public EntityResponse(RestContext context, StatusType status, Headers headers, ResponseConverter<T> converter,
            byte[] body) {
        super(context, status, headers);
        this.body = body;
        this.converter = converter;
    }

    @Override
    public EntityResponse<T> expecting(StatusType... expectedTypes) {
        super.expecting(expectedTypes);
        return this;
    }

    public T getBody() {
        return converter.convert(inputStream(), headers());
    }

    public <U> U getBody(Class<U> type) {
        ResponseConverter<U> otherConverter = context().converterFor(type);
        return otherConverter.convert(inputStream(), headers());
    }

    private InputStream inputStream() {
        return new ByteArrayInputStream(body);
    }
}
