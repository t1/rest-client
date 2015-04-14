package com.github.t1.rest;

import static com.github.t1.rest.VendorType.*;
import static javax.ws.rs.core.MediaType.*;

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
    private final VendorType vendorType;
    private final Map<MediaType, MessageBodyReader<T>> readers = new LinkedHashMap<>();

    public RestConverter(Class<T> acceptedType) {
        this.acceptedType = acceptedType;
        this.vendorType = acceptedType.getAnnotation(VendorType.class);
    }

    public List<MediaType> mediaTypes() {
        return new ArrayList<>(readers.keySet());
    }

    void addIfReadable(MessageBodyReader<T> reader, MediaType limitedType) {
        log.info("consider {} for {}:{}", reader.getClass(), acceptedType, vendorType);
        for (MediaType rawMediaType : mediaTypes(reader)) {
            MediaType mediaType = vendored(rawMediaType);
            if (isReadable(mediaType, reader, limitedType)) {
                log.debug("{} can convert {} to {}", reader.getClass(), mediaType, acceptedType);
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
        if (list.isEmpty()) {
            log.debug("no media type annotation (@Consumes) found on {}; fallback to wildcard", bean.getClass());
            list.add(WILDCARD_TYPE);
        }
        return list;
    }

    private MediaType vendored(MediaType mediaType) {
        if (vendorType == null)
            return mediaType;
        MediaType result = new MediaType("application", "vnd." + vendorTypeString() + "+" + mediaType.getSubtype());
        log.debug("use vendor type {} for {}", result, mediaType);
        return result;
    }

    private String vendorTypeString() {
        String vendorTypeValue = vendorType.value();
        if (USE_CLASS_NAME.equals(vendorTypeValue))
            vendorTypeValue = acceptedType.getName();
        return vendorTypeValue;
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

    private boolean isReadable(MediaType actual, MessageBodyReader<T> reader, MediaType limitedType) {
        if (reader != null)
            for (MediaType supported : mediaTypes(reader))
                if (vendored(supported).isCompatible(actual))
                    if (reader.isReadable(acceptedType, acceptedType, null, actual))
                        if (limitedType == null || limitedType.isCompatible(vendored(supported)))
                            return true;
        return false;
    }

    @Override
    public String toString() {
        return "Converter for " + acceptedType.getName() + ((vendorType == null) ? "" : "/" + vendorType) + " using "
                + readers.values();
    }
}
