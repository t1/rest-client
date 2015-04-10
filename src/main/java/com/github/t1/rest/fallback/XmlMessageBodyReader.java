package com.github.t1.rest.fallback;

import static com.github.t1.rest.fallback.StringMessageBodyReader.*;
import static javax.ws.rs.core.MediaType.*;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.enterprise.inject.Alternative;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.MessageBodyReader;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlRootElement;

@Alternative
@Consumes(APPLICATION_XML)
public class XmlMessageBodyReader implements MessageBodyReader<Object> {
    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type != String.class && type.isAnnotationPresent(XmlRootElement.class);
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream) {
        return JAXB.unmarshal(new StringReader(readString(entityStream, mediaType)), type);
    }
}
