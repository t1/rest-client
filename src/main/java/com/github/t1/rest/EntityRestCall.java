package com.github.t1.rest;

import lombok.*;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;

import javax.annotation.concurrent.Immutable;
import javax.ws.rs.*;
import javax.ws.rs.core.Response.StatusType;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;

import static com.github.t1.rest.fallback.ByteArrayMessageBodyReader.*;

@Immutable
class EntityRestCall<T> extends RestCall {
    @Getter
    private final ResponseConverter<T> converter;

    public <M extends Annotation> EntityRestCall(RestContext context, Class<M> method, URI uri, Headers requestHeaders,
            CloseableHttpClient apacheClient, ResponseConverter<T> converter) {
        super(context, method, uri, requestHeaders, apacheClient, apacheRequest(method, uri));
        this.converter = converter;
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    public static <M extends Annotation> HttpRequestBase apacheRequest(Class<M> method, URI uri) {
        assert method.isAnnotationPresent(HttpMethod.class);
        if (method == GET.class)
            return new HttpGet(uri);
        if (method == POST.class)
            return new HttpPost(uri);
        throw new UnsupportedOperationException("no handling defined for http method " + method.getName());
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
        return new EntityResponse<>(context(), status, responseHeaders, converter, body);
    }
}
