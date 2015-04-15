package com.github.t1.rest;

import java.util.*;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;

import lombok.extern.slf4j.Slf4j;

import com.github.t1.rest.fallback.*;

/**
 * TODO it would be nice if we could reuse the MessageBodyReaders from the container, when available, but they don't
 * seem to be CDI beans, so how should we detect them?
 */
@Slf4j
public class RestConfig {
    public <T> RestConverter<T> converterFor(Class<T> type) {
        return converterFor(type, null);
    }

    /**
     * Normally you wouldn't call this: the acceptable types are determined by the readers available for the type. This
     * method is only needed if you (must) know that the server would return some content type that is not complete or
     * otherwise not useful for this request, so you need a different one.
     */
    @Deprecated
    public <T> RestConverter<T> converterFor(Class<T> type, MediaType conentType) {
        RestConverter<T> out = new RestConverter<>(type);
        for (MessageBodyReader<T> bean : this.<T> readers()) {
            out.addIfReadable(bean, conentType);
        }
        if (out.mediaTypes().isEmpty())
            throw new IllegalArgumentException("no MessageBodyReader found for " + type);
        return out;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <T> Iterable<MessageBodyReader<T>> readers() {
        List<MessageBodyReader<?>> out = new ArrayList<>();
        out.add(new StringMessageBodyReader());
        out.add(new JsonMessageBodyReader());
        out.add(new XmlMessageBodyReader());
        try {
            out.add(new YamlMessageBodyReader());
        } catch (NoClassDefFoundError e) {
            if (!"com/fasterxml/jackson/dataformat/yaml/snakeyaml/Yaml".equals(e.getMessage()))
                throw e;
            log.debug("Yaml not found; ignore");
        }
        return (List) out;
    }
}
