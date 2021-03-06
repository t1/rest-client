package com.github.t1.rest.fallback;

import com.fasterxml.jackson.databind.*;

import javax.enterprise.inject.Alternative;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.MessageBodyReader;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;
import static com.fasterxml.jackson.databind.DeserializationFeature.*;
import static javax.ws.rs.core.MediaType.*;

@Alternative
@Consumes(APPLICATION_JSON)
public class JsonMessageBodyReader implements MessageBodyReader<Object> {
    public static final ObjectMapper MAPPER = new ObjectMapper() //
            .setSerializationInclusion(NON_EMPTY) //
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false) //
            .findAndRegisterModules();

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return ConverterTools.isConvertible(type);
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException {
        JavaType javaType = MAPPER.constructType(genericType);
        return MAPPER.readValue(entityStream, javaType);
    }
}
