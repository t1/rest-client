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
public class RestBodyReader<T> {
    private final Class<T> acceptedType;
    private final MessageBodyReader<T> bean;
    private final List<MediaType> mediaTypes;

    public RestBodyReader(Class<T> acceptedType, MessageBodyReader<T> bean) {
        this.acceptedType = acceptedType;
        this.bean = bean;
        this.mediaTypes = mediaTypes(bean);
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
            return bean.readFrom(acceptedType, acceptedType, null, mediaType, headers, entityStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private MediaType mediaType(MultivaluedMap<String, String> headers) {
        // TODO read actual media type from header
        return null;
    }

    public boolean isReadable() {
        for (MediaType mediaType : mediaTypes)
            if (bean.isReadable(acceptedType, acceptedType, null, mediaType))
                return true;
        return false;
    }
}
