package com.github.t1.rest;

import static java.util.Arrays.*;

import java.net.URI;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.ext.MessageBodyReader;

import lombok.extern.slf4j.Slf4j;

import com.github.t1.rest.fallback.*;

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

    public <T> Rest<T> uri(UriTemplate template) {
        return new Rest<>(this, template);
    }

    public <T> RestConverter<T> converterFor(Class<T> type) {
        RestConverter<T> out = new RestConverter<>(type);
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
        if (messageBodyReaders != null)
            return (Iterable) messageBodyReaders;
        log.debug("no messageBodyReaders set, probably CDI is not available; use fallback readers");
        return (Iterable) asList(new JsonMessageBodyReader(), new XmlMessageBodyReader(), new YamlMessageBodyReader());
    }
}
