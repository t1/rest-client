package com.github.t1.rest;

import static java.util.Arrays.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.Immutable;

@Immutable
public class CombinedCredentialsRegistry extends CredentialsRegistry {
    public static CredentialsRegistry combine(CredentialsRegistry... registries) {
        return combine(asList(registries));
    }

    public static CredentialsRegistry combine(Iterable<CredentialsRegistry> registries) {
        CombinedCredentialsRegistry result = new CombinedCredentialsRegistry();
        for (CredentialsRegistry registry : registries)
            result.registries.add(registry);
        return result;
    }

    private final List<CredentialsRegistry> registries = new ArrayList<>();

    @Override
    public Credentials get(URI uri) {
        for (CredentialsRegistry registry : registries) {
            Credentials resource = registry.get(uri);
            if (resource != null)
                return resource;
        }
        return null;
    }

    @Override
    public List<URI> uris() {
        List<URI> list = new ArrayList<>();
        for (CredentialsRegistry registry : registries)
            list.addAll(registry.uris());
        return list;
    }
}
