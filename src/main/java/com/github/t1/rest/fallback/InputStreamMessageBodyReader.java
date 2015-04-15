package com.github.t1.rest.fallback;

import static javax.ws.rs.core.MediaType.*;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.enterprise.inject.Alternative;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.MessageBodyReader;

@Alternative
@Consumes(APPLICATION_OCTET_STREAM)
public class InputStreamMessageBodyReader implements MessageBodyReader<InputStream> {
    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return InputStream.class.isAssignableFrom(type);
    }

    @Override
    public InputStream readFrom(Class<InputStream> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) {
        return entityStream;
    }
}
