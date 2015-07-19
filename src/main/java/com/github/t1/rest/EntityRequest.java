package com.github.t1.rest;

import static javax.ws.rs.core.Response.Status.*;

import lombok.*;

@Value
@EqualsAndHashCode(callSuper = true)
public class EntityRequest<T> extends RestRequest {
    private final ResponseConverter<T> converter;

    public EntityRequest(RestResource resource, Headers headers, ResponseConverter<T> converter) {
        super(resource, headers);
        this.converter = converter;
    }

    @Override
    public EntityRequest<T> header(String name, Object value) {
        return new EntityRequest<>(resource, headers.header(name, value), converter);
    }

    @Override
    public EntityRequest<T> with(String name, String value) {
        return new EntityRequest<>(resource.with(name, value), headers.with(name, value), converter);
    }

    /**
     * Execute a http GET and return the body of that type,
     * {@link RestResponse#expecting(javax.ws.rs.core.Response.StatusType...) expecting}
     * {@link javax.ws.rs.core.Response.Status#OK OK}
     */
    public T GET() {
        return GET_Response().expecting(OK).get();
    }

    /**
     * Execute a GET and return the {@link EntityResponse response object}. This method name is better than getResponse
     * (as it indicates that a GET is executed), and anything else I could think of.
     */
    public EntityResponse<T> GET_Response() {
        RestGetCall<T> request = config().createRestGetCall(uri(), headers, converter);
        return request.execute();
    }

    public Class<T> acceptedType() {
        return converter.acceptedType();
    }

    @Override
    public String toString() {
        return super.toString() + " with converters " + converter;
    }
}
