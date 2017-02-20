package com.github.t1.rest.fallback;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.SneakyThrows;

import javax.enterprise.inject.Alternative;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.MessageBodyReader;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;
import static com.fasterxml.jackson.databind.DeserializationFeature.*;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.*;
import static com.github.t1.rest.fallback.YamlMessageBodyReader.*;

@Alternative
@Consumes(APPLICATION_YAML)
public class YamlMessageBodyReader implements MessageBodyReader<Object> {
    public final static String APPLICATION_YAML = "application/yaml";
    public final static MediaType APPLICATION_YAML_TYPE = MediaType.valueOf(APPLICATION_YAML);

    static final ObjectMapper YAML = new ObjectMapper(
            new YAMLFactory()
                    .enable(MINIMIZE_QUOTES)
                    .disable(WRITE_DOC_START_MARKER))
            .setSerializationInclusion(NON_EMPTY)
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            .findAndRegisterModules();

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return ConverterTools.isConvertible(type);
    }

    @Override
    @SneakyThrows(IOException.class)
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream) {
        return YAML.readValue(entityStream, type);
    }
}
