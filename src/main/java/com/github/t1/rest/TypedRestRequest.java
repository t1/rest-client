package com.github.t1.rest;

import static javax.ws.rs.core.Response.Status.*;

import java.io.IOException;

import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import org.apache.http.Header;
import org.apache.http.client.methods.*;

@Slf4j
@Value
@EqualsAndHashCode(callSuper = true)
public class TypedRestRequest<T> extends RestRequest {
    public static final RestConfig CONFIG = new RestConfig();

    private final RestConverter<T> converter;

    public TypedRestRequest(RestResource resource, Headers headers, Class<T> acceptedType) {
        this(resource, headers, CONFIG.converterFor(acceptedType));
    }

    /**
     * Normally you wouldn't call this: the acceptable types are determined by the readers available for the type passed
     * in. This method is only needed if you (must) know that the server would return some content type, that is not
     * complete or otherwise not useful for this request, so you need a different one.
     */
    @Deprecated
    public TypedRestRequest(RestResource resource, Headers headers, Class<T> acceptedType, MediaType conentType) {
        this(resource, headers, CONFIG.converterFor(acceptedType, conentType));
    }

    public TypedRestRequest(RestResource resource, Headers headers, RestConverter<T> converter) {
        super(resource, headers.accept(converter.mediaTypes()));
        this.converter = converter;
    }

    public T get() {
        HttpGet get = new HttpGet(uri());
        addHeaders(get);
        log.debug("execute {} on {}", get, get.getURI());
        try (CloseableHttpResponse response = execute(get)) {
            expecting(response, OK);
            MultivaluedMap<String, String> headers = convert(response.getAllHeaders());
            return converter.convert(response.getEntity().getContent(), headers);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void expecting(CloseableHttpResponse response, Status expectedStatus) {
        if (!isStatus(response, expectedStatus))
            throw new RuntimeException("expected status " + expectedStatus.getStatusCode() + " "
                    + expectedStatus.getReasonPhrase() + " but got " + response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase());
    }

    private boolean isStatus(CloseableHttpResponse response, Status expected) {
        return response.getStatusLine().getStatusCode() == expected.getStatusCode();
    }

    private MultivaluedMap<String, String> convert(Header[] headers) {
        MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
        for (Header header : headers)
            map.add(header.getName(), header.getValue());
        return map;
    }
}
