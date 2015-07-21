package com.github.t1.rest;

import static com.github.t1.rest.fallback.ByteArrayMessageBodyReader.*;

import java.io.*;
import java.net.URI;

import javax.ws.rs.core.Response.StatusType;

import lombok.SneakyThrows;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;

class RestGetCall<T> extends RestCall {
    private final ResponseConverter<T> converter;

    public RestGetCall(RestConfig config, CloseableHttpClient apacheClient, URI uri, Headers requestHeaders,
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
    protected EntityResponse<T> convert(CloseableHttpResponse apacheResponse) {
        Headers responseHeaders = convert(apacheResponse.getAllHeaders());
        StatusType status = status(apacheResponse);
        HttpEntity entity = apacheResponse.getEntity();
        InputStream responseStream = bufferedStream(entity);
        return new EntityResponse<>(config(), status, responseHeaders, converter, responseStream);
    }

    private InputStream bufferedStream(HttpEntity entity) throws IOException {
        if (entity == null)
            return null;
        byte[] buffer = readAll(entity.getContent());
        return new ByteArrayInputStream(buffer);
    }
}
