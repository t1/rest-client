package com.github.t1.rest;

import javax.ws.rs.core.MediaType;

import lombok.*;

import com.github.t1.rest.Headers.Header;

@Data
@RequiredArgsConstructor
public class RestResponse {
    private final Headers headers;

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
