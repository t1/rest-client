package com.github.t1.rest;

import java.util.*;

import javax.annotation.concurrent.Immutable;

import lombok.*;

@Immutable
@Value
public class StaticRestResourceRegistry implements RestResourceRegistry {
    @NonNull
    String alias;
    @NonNull
    RestResource resource;
    RestResourceRegistry tail;

    public StaticRestResourceRegistry(String alias, RestResource resource) {
        this(alias, resource, null);
    }

    public StaticRestResourceRegistry(String alias, RestResource resource, RestResourceRegistry tail) {
        this.alias = alias;
        this.resource = resource;
        this.tail = tail;
    }

    @Override
    public RestResource get(String alias) {
        if (this.alias.equals(alias))
            return resource;
        return (tail == null) ? null : tail.get(alias);
    }

    @Override
    public List<String> names() {
        return Collections.singletonList(alias);
    }

    public StaticRestResourceRegistry and(String alias, String uri) {
        return and(alias, UriTemplate.fromString(uri));
    }

    public StaticRestResourceRegistry and(String alias, UriTemplate uri) {
        return new StaticRestResourceRegistry(alias, new RestResource(uri), this);
    }

    public StaticRestResourceRegistry and(String alias, RestResource resource) {
        return new StaticRestResourceRegistry(alias, resource, this);
    }
}
