package com.github.t1.rest.fallback;

import static com.github.t1.rest.fallback.ByteArrayMessageBodyReader.*;
import static javax.ws.rs.core.MediaType.*;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.enterprise.inject.Alternative;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.MessageBodyReader;

@Alternative
@Consumes({ APPLICATION_OCTET_STREAM, APPLICATION_JAVA_ARCHIVE })
public class ByteArrayMessageBodyReader implements MessageBodyReader<byte[]> {
    private static final int BUFFER_SIZE = 4096;
    public static final String APPLICATION_JAVA_ARCHIVE = "application/java-archive";
    public static final MediaType APPLICATION_JAVA_ARCHIVE_TYPE = MediaType.valueOf(APPLICATION_JAVA_ARCHIVE);

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return byte[].class.isAssignableFrom(type);
    }

    @Override
    public byte[] readFrom(Class<byte[]> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException {
        return readAll(entityStream);
    }

    public static byte[] readAll(InputStream entityStream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
        byte[] buffer = new byte[BUFFER_SIZE];
        while (true) {
            int read = entityStream.read(buffer);
            if (read < 0)
                break;
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }
}
