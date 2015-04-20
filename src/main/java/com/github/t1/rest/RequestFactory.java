package com.github.t1.rest;

import java.net.URI;

public class RequestFactory {
    public GetRequest createGetRequest(URI uri, Headers headers) {
        return new GetRequest(uri, headers);
    }
}
