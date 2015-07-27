package com.github.t1.rest;

import static java.util.Arrays.*;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.Immutable;

@Immutable
public class CombinedRestResourceRegistry implements RestResourceRegistry {
    public static RestResourceRegistry combine(RestResourceRegistry... registries) {
        return combine(asList(registries));
    }

    public static RestResourceRegistry combine(Iterable<RestResourceRegistry> registries) {
        CombinedRestResourceRegistry result = new CombinedRestResourceRegistry();
        for (RestResourceRegistry registry : registries)
            result.registries.add(registry);
        return result;
    }

    private final List<RestResourceRegistry> registries = new ArrayList<>();

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
