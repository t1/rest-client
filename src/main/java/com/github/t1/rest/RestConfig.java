package com.github.t1.rest;

import java.net.URI;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.ext.MessageBodyReader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RestConfig {
    @Inject
    Instance<MessageBodyReader<?>> messageBodyReaders;

    public <T> Rest<T> uri(URI baseUri) {
        return uri(baseUri.toString());
    }

    public <T> Rest<T> uri(String baseUri) {
        return new Rest<>(this, baseUri);
    }

    public <T> BodyConverter<T> converterFor(Class<T> type) {
        BodyConverter<T> out = new BodyConverter<>(type);
        for (MessageBodyReader<T> bean : this.<T> readers()) {
            log.info("consider {} for {}", bean.getClass(), type);
            out.addIfReadable(bean);
        }
        if (!out.isReadable())
            throw new IllegalArgumentException("no MessageBodyReader found for " + type);
        return out;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <T> Iterable<MessageBodyReader<T>> readers() {
        return (Iterable) messageBodyReaders;
    }
}
