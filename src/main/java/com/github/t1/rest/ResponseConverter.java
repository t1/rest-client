package com.github.t1.rest;

import static com.github.t1.rest.VendorType.*;
import static java.util.Locale.*;
import static javax.ws.rs.core.MediaType.*;

import java.io.*;
import java.util.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.MessageBodyReader;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Holds the java type that should be converted to and the converters to do the actual conversion for some content type
 * returned by the http request.
 * <p/>
 * Important: The {@link #convert(InputStream, Headers)} method <b>closes</b> the stream with one exception: Iff the
 * java type is {@link Closeable} (like an {@link InputStream}) and no exception occurs, the stream is left open.
 */
@Slf4j
@Getter
public class ResponseConverter<T> {
    private final Class<T> acceptedType;
    private final VendorType vendorType;
    private final Map<MediaType, MessageBodyReader<T>> readers = new LinkedHashMap<>();

    public ResponseConverter(Class<T> acceptedType) {
        this.acceptedType = acceptedType;
        this.vendorType = acceptedType.getAnnotation(VendorType.class);
    }

    public List<MediaType> mediaTypes() {
        return new ArrayList<>(readers.keySet());
    }

    void addIfReadable(MessageBodyReader<T> reader, MediaType limitedType) {
        log.debug("consider {} for {}:{}", reader.getClass(), acceptedType, vendorType);
        for (MediaType rawMediaType : mediaTypes(reader)) {
            MediaType mediaType = vendored(rawMediaType);
            if (isReadable(mediaType, reader, limitedType)) {
                log.debug("{} can convert {} to {}", reader.getClass(), mediaType, acceptedType);
                if (limitedType != null && readers.isEmpty())
                    mediaType = limitedType; // prefer actual limited type over the produced
                readers.put(mediaType, reader);
            }
        }
    }

    private List<MediaType> mediaTypes(MessageBodyReader<T> reader) {
        List<MediaType> list = new ArrayList<>();
        Consumes consumes = reader.getClass().getAnnotation(Consumes.class);
        if (consumes != null)
            for (String mediaType : consumes.value())
                list.add(MediaType.valueOf(mediaType));
        if (list.isEmpty()) {
            log.debug("no media type annotation (@Consumes) found on {}; fallback to wildcard", reader.getClass());
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
            vendorTypeValue = acceptedType.getName().toLowerCase(US);
        return vendorTypeValue;
    }

    public T convert(InputStream entityStream, Headers headers) {
        try {
            MediaType mediaType = headers.contentType();
            MessageBodyReader<T> reader = converterFor(mediaType);
            MultivaluedMap<String, String> headerMap = headers.toMultiValuedMap();
            T out = reader.readFrom(acceptedType, acceptedType, null, mediaType, headerMap, entityStream);
            if (shouldClose())
                entityStream.close();
            return out;
        } catch (Exception e) {
            try {
                entityStream.close();
            } catch (IOException f) {
                e.addSuppressed(f);
            }
            throw new RuntimeException("can't read: " + e.getMessage(), e);
        }
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
        return "Converter " + acceptedType.getName() + ((vendorType == null) ? "" : "/" + vendorType) //
                + ": " + readers.keySet();
    }

    /** iff the type to convert to is {@link Closeable}, the client has to do so in order to close the stream */
    public boolean shouldClose() {
        return !Closeable.class.isAssignableFrom(acceptedType);
    }
}
