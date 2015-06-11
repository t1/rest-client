package com.github.t1.rest;

import java.net.URI;

public class RequestFactory {
    public <T> GetRequest<T> createGetRequest(URI uri, Headers headers, ResponseConverter<T> converter) {
        return new GetRequest<>(uri, headers, converter);
    }
}
