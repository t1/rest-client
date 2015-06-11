package com.github.t1.rest;

import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.StatusType;

import lombok.*;

import com.github.t1.rest.Headers.Header;

@Data
@RequiredArgsConstructor
public abstract class RestResponse {
    private final StatusType status;
    private final Headers headers;

    public RestResponse expecting(StatusType... expectedTypes) {
        for (StatusType expectedType : expectedTypes)
            if (status.getStatusCode() != expectedType.getStatusCode())
                throw new UnexpectedStatusException(status, expectedTypes);
        return this;
    }

    public Header header(String name) {
        return headers.header(name);
    }

    public MediaType contentType() {
        return headers.contentType();
    }

    public Integer contentLength() {
        return headers.contentLength();
    }
}
