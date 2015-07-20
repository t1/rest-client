package com.github.t1.rest;

import java.io.*;

import javax.ws.rs.core.Response.StatusType;

/** Delegates the responsibility for closing the input stream to the converter */
public class EntityResponse<T> extends RestResponse {
    private final ResponseConverter<T> converter;
    private final InputStream inputStream;
    private final Closeable closer;

    public EntityResponse(RestConfig config, StatusType status, Headers headers, ResponseConverter<T> converter,
            InputStream inputStream, Closeable closer) {
        super(config, status, headers);
        this.converter = converter;
        this.inputStream = inputStream;
        this.closer = closer;
    }

    @Override
    public EntityResponse<T> expecting(StatusType... expectedTypes) {
        try {
            super.expecting(expectedTypes);
            return this;
        } catch (RuntimeException e) {
            try {
                closer.close();
            } catch (IOException closingException) {
                e.addSuppressed(closingException);
            }
            throw e;
        }
    }

    public T get() {
        return converter.convert(inputStream, headers(), closer);
    }

    public <U> U get(Class<U> type) {
        ResponseConverter<U> otherConverter = config().converterFor(type);
        return otherConverter.convert(inputStream, headers(), closer);
    }
}
