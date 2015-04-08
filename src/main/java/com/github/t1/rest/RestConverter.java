package com.github.t1.rest;

import java.io.*;
import java.util.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.MessageBodyReader;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class RestConverter<T> {
    private final Class<T> acceptedType;
    private final Map<MediaType, MessageBodyReader<T>> readers = new HashMap<>();

    public RestConverter(Class<T> acceptedType) {
        this.acceptedType = acceptedType;
    }

    public boolean isReadable() {
        return !readers.isEmpty();
    }

    public Set<MediaType> mediaTypes() {
        return readers.keySet();
    }

    public void addIfReadable(MessageBodyReader<T> reader) {
        for (MediaType mediaType : mediaTypes(reader)) {
            if (isReadable(mediaType, reader)) {
                log.debug("{} can convert {} from {}", reader.getClass(), mediaType, acceptedType);
                readers.put(mediaType, reader);
            }
        }
    }

    private List<MediaType> mediaTypes(MessageBodyReader<T> bean) {
        List<MediaType> list = new ArrayList<>();
        Consumes consumes = bean.getClass().getAnnotation(Consumes.class);
        if (consumes != null)
            for (String mediaType : consumes.value())
                list.add(MediaType.valueOf(mediaType));
        if (list.isEmpty())
            log.warn("no media type annotation (@Consumes) found on " + bean.getClass());
        return list;
    }

    public T convert(InputStream entityStream, MultivaluedMap<String, String> headers) {
        try {
            MediaType mediaType = mediaType(headers);
            MessageBodyReader<T> reader = converterFor(mediaType);
            return reader.readFrom(acceptedType, acceptedType, null, mediaType, headers, entityStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private MediaType mediaType(MultivaluedMap<String, String> headers) {
        String contentType = headers.getFirst("Content-Type");
        if (contentType == null)
            return null;
        if (contentType.startsWith("{") && contentType.endsWith(", q=1000}")) // Jersey/Dropwizard bug?
            contentType = contentType.substring(1, contentType.length() - 9);
        return MediaType.valueOf(contentType);
    }

    private MessageBodyReader<T> converterFor(MediaType expected) {
        for (MediaType actual : readers.keySet())
            if (expected.isCompatible(actual))
                if (readers.get(actual).isReadable(acceptedType, acceptedType, null, actual))
                    return readers.get(actual);
        throw new IllegalArgumentException("no converter for " + expected + " found in " + this.readers.keySet());
    }

    private boolean isReadable(MediaType actual, MessageBodyReader<T> reader) {
        if (reader != null)
            for (MediaType supported : mediaTypes(reader))
                if (supported.isCompatible(actual))
                    if (reader.isReadable(acceptedType, acceptedType, null, actual))
                        return true;
        return false;
    }

    public RestConverter<T> limitedTo(MediaType mediaType) {
        RestConverter<T> converter = new RestConverter<>(acceptedType);
        MessageBodyReader<T> reader = converterFor(mediaType);
        converter.readers.put(mediaType, reader);
        return converter;
    }
}
