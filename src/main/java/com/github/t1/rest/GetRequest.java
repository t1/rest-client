package com.github.t1.rest;

import java.io.*;
import java.net.URI;

import javax.ws.rs.core.Response.StatusType;

import lombok.SneakyThrows;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;

public class GetRequest<T> extends HttpRequest {
    private final ResponseConverter<T> converter;

    public GetRequest(URI uri, Headers requestHeaders, ResponseConverter<T> converter) {
        super(new HttpGet(uri), requestHeaders);
        this.converter = converter;
    }

    @Override
    @SuppressWarnings("unchecked")
    public EntityResponse<T> execute() {
        return (EntityResponse<T>) super.execute();
    }

    @Override
    @SneakyThrows(IOException.class)
    protected EntityResponse<T> convert(CloseableHttpResponse apacheResponse) {
        Headers responseHeaders = convert(apacheResponse.getAllHeaders());
        StatusType status = status(apacheResponse);
        HttpEntity entity = apacheResponse.getEntity();
        InputStream responseStream = (entity == null) ? null : entity.getContent();
        return new EntityResponse<>(status, responseHeaders, converter, responseStream);
    }
}
