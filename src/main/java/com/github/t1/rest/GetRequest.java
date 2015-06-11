package com.github.t1.rest;

import java.net.URI;

import org.apache.http.client.methods.HttpGet;

public class GetRequest extends HttpRequest {
    public GetRequest(URI uri, Headers requestHeaders) {
        super(new HttpGet(uri), requestHeaders);
    }
}
