package com.github.t1.rest;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import javax.ws.rs.core.*;
import javax.ws.rs.ext.MessageBodyReader;

import lombok.SneakyThrows;

public class StringMessageBodyReader implements MessageBodyReader<Object> {
    public static String readString(InputStream in, MediaType mediaType) {
        return readFromAsString(new InputStreamReader(in, getCharset(mediaType)));
    }

    public static Charset getCharset(MediaType mediaType) {
        String name = (mediaType == null) ? null : mediaType.getParameters().get(MediaType.CHARSET_PARAMETER);
        if (name == null)
            name = "UTF-8";
        return Charset.forName(name);
    }

    @SneakyThrows(IOException.class)
    public static String readFromAsString(Reader reader) {
        StringBuilder out = new StringBuilder();
        char[] buffer = new char[8 * 1024];
        while (true) {
            int length = reader.read(buffer);
            if (length < 0)
                break;
            out.append(buffer, 0, length);
        }
        return out.toString();
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return String.class.isAssignableFrom(type);
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream) {
        return readString(entityStream, mediaType);
    }
}
