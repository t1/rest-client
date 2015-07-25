package com.github.t1.rest.fallback;

import static com.github.t1.rest.fallback.InputStreamMessageBodyReader.*;
import static javax.ws.rs.core.MediaType.*;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.enterprise.inject.Alternative;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.MessageBodyReader;

@Alternative
@Consumes({ APPLICATION_OCTET_STREAM, APPLICATION_JAVA_ARCHIVE, WILDCARD })
public class InputStreamMessageBodyReader implements MessageBodyReader<InputStream> {
    public static final String APPLICATION_JAVA_ARCHIVE = "application/java-archive";
    public static final MediaType APPLICATION_JAVA_ARCHIVE_TYPE = MediaType.valueOf(APPLICATION_JAVA_ARCHIVE);

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
