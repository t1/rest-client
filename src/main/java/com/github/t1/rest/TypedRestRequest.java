package com.github.t1.rest;

import java.io.IOException;

import javax.ws.rs.core.MediaType;

import lombok.*;

import org.apache.http.client.methods.*;

@Value
@EqualsAndHashCode(callSuper = true)
public class TypedRestRequest<T> extends RestRequest {
    public static final RestConfig CONFIG = new RestConfig();

    private final RestConverter<T> converter;

    public TypedRestRequest(RestResource resource, Headers headers, Class<T> acceptedType) {
        this(resource, headers, CONFIG.converterFor(acceptedType));
    }

    /**
     * Normally you wouldn't call this directly: the acceptable types are determined by the readers available for the
     * type passed in. Call this method only, if you (must) know that the server would return some content type, that is
     * not complete or otherwise not useful for this request, so you need a different one.
     */
    @Deprecated
    public TypedRestRequest(RestResource resource, Headers headers, Class<T> acceptedType, MediaType contentType) {
        this(resource, headers, CONFIG.converterFor(acceptedType, contentType));
    }

    public TypedRestRequest(RestResource resource, Headers headers, RestConverter<T> converter) {
        super(resource, headers.accept(converter.mediaTypes()));
        this.converter = converter;
    }

    @SuppressWarnings("resource")
    public T get() {
        CloseableHttpResponse response = null;
        boolean shouldClose = true;
        try {
            response = execute(new HttpGet(uri()));
            Headers headers = convert(response.getAllHeaders());
            T converted = converter.convert(response.getEntity().getContent(), headers);
            shouldClose = converter.shouldClose();
            return converted;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (response != null && shouldClose) {
                try {
                    response.close();
                } catch (Throwable e) {
                    // ignore
                }
            }
        }
    }
}
