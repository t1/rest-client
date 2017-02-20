package com.github.t1.rest.fallback;

import lombok.SneakyThrows;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static com.github.t1.rest.fallback.YamlMessageBodyReader.*;
import static javax.ws.rs.core.MediaType.*;

@Produces(WILDCARD)
public class YamlMessageBodyWriter implements MessageBodyWriter<Object> {
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return ConverterTools.isConvertible(type) && ConverterTools.isApplicationType(mediaType, "yaml");
    }

    @Override
    public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    @SneakyThrows(IOException.class)
    public void writeTo(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) {
        YAML.writeValue(entityStream, t);
    }
}
