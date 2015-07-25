package com.github.t1.rest;

import static com.github.t1.rest.fallback.ByteArrayMessageBodyReader.*;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.Response.StatusType;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;

import lombok.*;

class RestGetCall<T> extends RestCall {
    @Getter
    private final ResponseConverter<T> converter;

    public RestGetCall(RestContext config, URI uri, Headers requestHeaders, CloseableHttpClient apacheClient,
            ResponseConverter<T> converter) {
        super(config, uri, requestHeaders, apacheClient, new HttpGet(uri));
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
        StatusType status = status(apacheResponse);
        Headers responseHeaders = convert(apacheResponse.getAllHeaders());
        HttpEntity entity = apacheResponse.getEntity();
        byte[] body = (entity == null) ? null : readAll(entity.getContent());
        return new EntityResponse<>(config(), status, responseHeaders, converter, body);
    }
}
