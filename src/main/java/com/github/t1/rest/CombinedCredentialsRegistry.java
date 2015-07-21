package com.github.t1.rest;

import static java.util.Arrays.*;

import java.net.URI;
import java.util.List;

public class CombinedCredentialsRegistry extends CredentialsRegistry {
    private final List<CredentialsRegistry> registries;

    public CombinedCredentialsRegistry(CredentialsRegistry... registries) {
        this.registries = asList(registries);
    }

    @Override
    public Credentials get(URI uri) {
        for (CredentialsRegistry registry : registries) {
            Credentials resource = registry.get(uri);
            if (resource != null)
                return resource;
        }
        return null;
    }
}
