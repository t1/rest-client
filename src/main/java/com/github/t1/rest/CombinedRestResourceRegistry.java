package com.github.t1.rest;

import static java.util.Arrays.*;

import java.util.ArrayList;
import java.util.List;

public class CombinedRestResourceRegistry implements RestResourceRegistry {
    private final List<RestResourceRegistry> registries;

    public CombinedRestResourceRegistry(RestResourceRegistry... registries) {
        this.registries = asList(registries);
    }

    @Override
    public RestResource get(String alias) {
        for (RestResourceRegistry registry : registries) {
            RestResource resource = registry.get(alias);
            if (resource != null)
                return resource;
        }
        return null;
    }

    @Override
    public List<String> names() {
        List<String> list = new ArrayList<>();
        for (RestResourceRegistry registry : registries)
            list.addAll(registry.names());
        return list;
    }
}
