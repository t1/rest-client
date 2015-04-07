package com.github.t1.rest;

import java.io.*;

import javax.ws.rs.ext.MessageBodyReader;

import lombok.*;

@Getter
@RequiredArgsConstructor
public class RestBodyReader<T> {
    private final Class<T> acceptedType;
    private final MessageBodyReader<T> bean;

    public T convert(InputStream entityStream) {
        try {
            return bean.readFrom(acceptedType, acceptedType, null, null, null, entityStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isReadable() {
        return bean.isReadable(acceptedType, acceptedType, null, null);
    }
}
