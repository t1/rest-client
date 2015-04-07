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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> RestBodyReader<T> converterFor(Class<T> type) {
        for (MessageBodyReader<T> bean : ((Iterable<MessageBodyReader<T>>) (Iterable) messageBodyReaders)) {
            log.info("consider {} for {}", bean, type);
            RestBodyReader<T> restBodyReader = new RestBodyReader<>(type, bean);
            if (restBodyReader.isReadable()) {
                return restBodyReader;
            } else {
                messageBodyReaders.destroy(bean);
            }
        }
        throw new IllegalArgumentException("no MessageBodyReader found for " + type);
    }
}
