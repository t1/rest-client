package com.github.t1.deployer.app;

import static java.util.Arrays.*;

import java.net.URI;
import java.util.List;

import lombok.Getter;
import lombok.experimental.Accessors;

import com.github.t1.deployer.app.UriTemplate.HierarchicalUriTemplate;

@Getter
@Accessors(fluent = true)
public class Rest {
    private static final List<String> ALLOWED_SCHEMES = asList("http", "https");

    private final HierarchicalUriTemplate uri;

    // headers

    // config

    public Rest(URI uri) {
        this(uri.toString());
    }

    public Rest(String uri) {
        this.uri = (HierarchicalUriTemplate) UriTemplate.fromString(uri);
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
        return uri.scheme();
    }

    public String schemeSpecificPart() {
        return uri.schemeSpecificPart();
    }

    public String authority() {
        UriAuthority authority = uri.authority();
        return (authority == null) ? null : authority.toString();
    }

    public String path() {
        return uri.path();
    }

    public <T> T get(Class<T> type) {
        return null;
    }
}
