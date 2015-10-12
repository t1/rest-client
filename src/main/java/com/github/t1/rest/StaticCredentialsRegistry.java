package com.github.t1.rest;

import java.net.URI;
import java.util.*;

import javax.annotation.concurrent.Immutable;

import lombok.*;

@Immutable
@Value
@EqualsAndHashCode(callSuper = false)
public class StaticCredentialsRegistry extends CredentialsRegistry {
    @NonNull
    URI uri;
    @NonNull
    Credentials resource;
    CredentialsRegistry tail;

    public StaticCredentialsRegistry(URI uri, Credentials resource) {
        this(uri, resource, null);
    }

    public StaticCredentialsRegistry(URI uri, Credentials resource, CredentialsRegistry tail) {
        this.uri = uri;
        this.resource = resource;
        this.tail = tail;
    }

    @Override
    public Credentials get(URI uri) {
        if (this.uri.equals(uri))
            return resource;
        return (tail == null) ? null : tail.get(uri);
    }

    public StaticCredentialsRegistry and(URI uri, Credentials credentials) {
        return new StaticCredentialsRegistry(uri, credentials, this);
    }

    @Override
    public List<URI> uris() {
        List<URI> list = new ArrayList<>();
        list.add(uri);
        if (tail != null)
            list.addAll(tail.uris());
        return list;
    }
}
