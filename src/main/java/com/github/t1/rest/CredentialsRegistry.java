package com.github.t1.rest;

import java.net.URI;
import java.nio.file.*;
import java.util.List;

import javax.annotation.concurrent.Immutable;

@Immutable
public abstract class CredentialsRegistry {
    /** Retrieve the resource for that URI */
    public abstract Credentials get(URI uri);

    public Credentials lookup(URI uri) {
        while (true) {
            Credentials result = get(uri);
            if (result != null) {
                return result;
            }
            if (uri.getFragment() != null) {
                uri = removeTrailing(uri, "#" + uri.getFragment());
            } else if (uri.getQuery() != null) {
                uri = removeTrailing(uri, "?" + uri.getQuery());
            } else if (uri.toString().endsWith("/")) {
                uri = removeTrailing(uri, "/");
            } else if (uri.getPath() != null && !uri.getPath().isEmpty()) {
                Path path = Paths.get(uri.getPath());
                path = path.getName(path.getNameCount() - 1);
                uri = removeTrailing(uri, "/" + path);
            } else {
                return null;
            }
        }
    }

    private URI removeTrailing(URI uri, String toBeRemoved) {
        String string = uri.toString();
        assert string.endsWith(toBeRemoved) : "uri should end with " + toBeRemoved + " but was " + uri;
        string = string.substring(0, string.length() - toBeRemoved.length());
        return URI.create(string);
    }

    public abstract List<URI> uris();
}
