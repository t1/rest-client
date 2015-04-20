package com.github.t1.rest;

import java.net.URI;
import java.util.*;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;

import lombok.*;

import com.github.t1.rest.fallback.*;

public class RestConfig {
    private final List<MessageBodyReader<?>> readers = new ArrayList<>();
    @Getter
    @Setter
    private RequestFactory requestFactory = new RequestFactory();

    public RestConfig() {
        add(new InputStreamMessageBodyReader());
        add(new JsonMessageBodyReader());
        add(new StringMessageBodyReader());
        add(new XmlMessageBodyReader());
        add(new YamlMessageBodyReader());
    }

    public RestConfig add(MessageBodyReader<?> reader) {
        readers.add(reader);
        return this;
    }

    public <T> ResponseConverter<T> converterFor(Class<T> type) {
        ResponseConverter<T> result = new ResponseConverter<>(type);
        addReadersFor(type, result);
        return result;
    }

    public ResponseConverter<?> converterFor(Class<?> first, Class<?>... more) {
        ResponseConverter<?> result = new ResponseConverter<>(Object.class);
        addReadersFor(first, result);
        for (Class<?> m : more)
            addReadersFor(m, result);
        return result;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void addReadersFor(Class<?> type, ResponseConverter<?> result) {
        ResponseConverter<?> converter = converterFor(type, (MediaType) null);
        result.readers().putAll((Map) converter.readers());
    }

    /**
     * Normally you wouldn't call this directly: the acceptable types are determined by the readers available for the
     * type. Call this method only, if you (must) know that the server would return some content type that is not
     * complete or otherwise not useful for this request, so you need a different one.
     */
    @Deprecated
    public <T> ResponseConverter<T> converterFor(Class<T> type, MediaType contentType) {
        ResponseConverter<T> out = new ResponseConverter<>(type);
        for (MessageBodyReader<T> bean : this.<T> readers()) {
            out.addIfReadable(bean, contentType);
        }
        if (out.mediaTypes().isEmpty())
            throw new IllegalArgumentException("no MessageBodyReader found for " + type);
        return out;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> Iterable<MessageBodyReader<T>> readers() {
        return (List) readers;
    }

    public GetRequest createGetRequest(URI uri, Headers headers) {
        return requestFactory.createGetRequest(uri, headers);
    }
}
