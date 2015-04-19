package com.github.t1.rest.fallback;

import static com.github.t1.rest.fallback.YamlMessageBodyReader.*;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.enterprise.inject.Alternative;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.MessageBodyReader;

import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;

@Alternative
@Consumes(APPLICATION_YAML)
public class YamlMessageBodyReader implements MessageBodyReader<Object> {
    public final static String APPLICATION_YAML = "application/yaml";
    public final static MediaType APPLICATION_YAML_TYPE = MediaType.valueOf(APPLICATION_YAML);

    private final Yaml yaml = new Yaml();

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return ConverterTools.isConvertible(type);
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream) {
        return yaml.load(entityStream);
    }
}
