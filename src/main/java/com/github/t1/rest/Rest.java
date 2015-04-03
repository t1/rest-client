package com.github.t1.rest;

import static java.util.Arrays.*;

import java.net.URI;
import java.util.List;

import lombok.Getter;

/** Immutable, typesafe, fluent, strictly appendable builder for clients of RESTful web services. */
@Getter
public class Rest {
    private static final List<String> ALLOWED_SCHEMES = asList("http", "https");

    private final UriTemplate uri;

    // headers

    // config

    public Rest(URI uri) {
        this(uri.toString());
    }

    public Rest(String uri) {
        this.uri = null;
        check();
    }

    private void check() {
        if (!ALLOWED_SCHEMES.contains(scheme()))
            throw new RuntimeException("unsupported scheme for REST: " + scheme());
    }

    public String uri() {
        return uri.toString();
    }

    public String scheme() {
        return null;
    }

    public String schemeSpecificPart() {
        return null;
    }

    public String authority() {
        return null;
    }

    public String path() {
        return null;
    }

    public <T> T get(Class<T> type) {
        return null;
    }
}
