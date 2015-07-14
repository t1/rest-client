package com.github.t1.rest;

import java.io.*;
import java.net.URI;

import javax.ws.rs.core.Response.StatusType;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;

import lombok.SneakyThrows;

class GetRequest<T> extends HttpRequest {
    private final ResponseConverter<T> converter;

    public GetRequest(RestConfig config, CloseableHttpClient apacheClient, URI uri, Headers requestHeaders,
            ResponseConverter<T> converter) {
        super(config, apacheClient, new HttpGet(uri), requestHeaders);
        this.converter = converter;
    }

    @Override
    @SuppressWarnings("unchecked")
    public EntityResponse<T> execute() {
        return (EntityResponse<T>) super.execute();
    }

    @Override
    @SneakyThrows(IOException.class)
    @SuppressWarnings("resource")
    protected EntityResponse<T> convert(CloseableHttpResponse apacheResponse) {
        Headers responseHeaders = convert(apacheResponse.getAllHeaders());
        StatusType status = status(apacheResponse);
        HttpEntity entity = apacheResponse.getEntity();
        InputStream responseStream = (entity == null) ? null : entity.getContent();
        return new EntityResponse<>(config(), status, responseHeaders, converter, responseStream);
    }
}
