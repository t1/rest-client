package com.github.t1.rest;

import static ch.qos.logback.classic.Level.*;
import static com.github.t1.rest.StringMessageBodyReader.*;
import static javax.ws.rs.core.MediaType.*;
import static lombok.AccessLevel.*;
import static org.junit.Assert.*;
import io.dropwizard.testing.junit.DropwizardClientRule;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.MessageBodyReader;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.*;

import org.jglue.cdiunit.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(CdiRunner.class)
@AdditionalClasses({ RestTest.JsonMessageBodyReader.class, RestTest.JsonPojoMessageBodyReader.class,
        RestTest.XmlMessageBodyReader.class, StringMessageBodyReader.class })
public class RestTest {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor(access = PRIVATE)
    @XmlRootElement
    public static class Pojo {
        private String string;
        private int i;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor(access = PRIVATE)
    public static class JsonPojo {
        private String string;
    }

    @Consumes(APPLICATION_JSON + ";" + CHARSET_PARAMETER + "=UTF-8")
    public static class JsonMessageBodyReader implements MessageBodyReader<Pojo> {
        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type == Pojo.class;
        }

        @Override
        public Pojo readFrom(Class<Pojo> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException {
            return mapper.readValue(entityStream, type);
        }
    }

    @Consumes(APPLICATION_JSON + ";" + CHARSET_PARAMETER + "=UTF-8")
    public static class JsonPojoMessageBodyReader implements MessageBodyReader<JsonPojo> {
        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type == JsonPojo.class;
        }

        @Override
        public JsonPojo readFrom(Class<JsonPojo> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException {
            return mapper.readValue(entityStream, type);
        }
    }

    @Consumes(APPLICATION_XML)
    public static class XmlMessageBodyReader implements MessageBodyReader<Pojo> {
        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type == Pojo.class;
        }

        @Override
        public Pojo readFrom(Class<Pojo> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                MultivaluedMap<String, String> httpHeaders, InputStream entityStream) {
            return JAXB.unmarshal(new StringReader(readString(entityStream, mediaType)), Pojo.class);
        }
    }

    @Path("/")
    public static class MockService {
        @GET
        @Path("/ping")
        @Produces(TEXT_PLAIN)
        public String ping() {
            return "pong";
        }

        @GET
        @Path("/pojo")
        public Pojo pojo() {
            return new Pojo("s", 123);
        }

        @GET
        @Path("/jsonpojo")
        public JsonPojo jsonpojo() {
            return new JsonPojo("json");
        }
    }

    private final DropwizardClientRule service = new DropwizardClientRule(new MockService());

    // this rule must be on a method so this bean is valid for CDI-Unit which makes it application scoped,
    // i.e. it must not have any public fields
    @Rule
    public DropwizardClientRule service() {
        return service;
    }

    @Inject
    RestConfig rest = new RestConfig();

    private Rest<Object> base() {
        return rest.uri(service.baseUri());
    }

    @Before
    public void before() {
        setLogLevel("org.apache.http.wire", DEBUG);
        setLogLevel("com.github.t1.rest", DEBUG);
    }

    private void setLogLevel(String loggerName, Level level) {
        ((Logger) LoggerFactory.getLogger(loggerName)).setLevel(level);
    }

    @Test(expected = RuntimeException.class)
    public void shouldFailParsingUnsupportedScheme() {
        rest.uri("mailto:test@example.com");
    }

    @Test
    public void shouldAcceptType() {
        Rest<Object> base = base();
        Rest<Pojo> rest = base.accept(Pojo.class);

        assertNull(base.converter());
        assertEquals(Pojo.class, rest.converter().acceptedType());

        try {
            rest.accept(String.class);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            // fine
        }
    }

    @Test
    public void shouldGetPing() {
        String pong = base().path("ping").accept(String.class).get();

        assertEquals("pong", pong);
    }

    @Test
    public void shouldGetPojo() {
        Pojo pojo = base().path("pojo").accept(Pojo.class).get();

        assertEquals("s", pojo.getString());
        assertEquals(123, pojo.getI());
    }

    @Test
    public void shouldGetJsonPojo() {
        JsonPojo pojo = base().path("jsonpojo").accept(JsonPojo.class).get();

        assertEquals("json", pojo.getString());
    }
}
