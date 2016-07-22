package com.github.t1.rest;

import com.github.t1.rest.Headers.Header;
import lombok.*;

import javax.annotation.concurrent.Immutable;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.StatusType;

@Immutable
@Data
@RequiredArgsConstructor
public abstract class RestResponse {
    private final RestContext context;
    private final StatusType status;
    private final Headers headers;

    /** Throw a {@link UnexpectedStatusException} if the {@link #status() response status} is none of those. */
    public RestResponse expecting(StatusType... expectedTypes) {
        for (StatusType expectedType : expectedTypes)
            if (status.getStatusCode() == expectedType.getStatusCode())
                return this;
        throw unexpectedStatusException(expectedTypes);
    }

    protected UnexpectedStatusException unexpectedStatusException(StatusType[] expectedTypes) {
        return new UnexpectedStatusException(status, headers, expectedTypes);
    }

    public Header header(String name) {
        return headers.firstHeader(name);
    }

    public MediaType contentType() {
        return headers.contentType();
    }

    public Integer contentLength() {
        return headers.contentLength();
    }
}
