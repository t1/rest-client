package com.github.t1.rest;

import java.util.*;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;

import com.github.t1.rest.fallback.*;

/**
 * TODO it would be nice if we could reuse the MessageBodyReaders from the container, when available, but they don't
 * seem to be CDI beans, so how should we detect them?
 */
public class RestConfig {
    private final List<MessageBodyReader<?>> readers = new ArrayList<>();

    public RestConfig() {
        add(new InputStreamMessageBodyReader());
        add(new JsonMessageBodyReader());
        add(new StringMessageBodyReader());
        add(new XmlMessageBodyReader());
    }

    public RestConfig add(MessageBodyReader<?> reader) {
        readers.add(reader);
        return this;
    }

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
    public <T> Iterable<MessageBodyReader<T>> readers() {
        return (List) readers;
    }
}
