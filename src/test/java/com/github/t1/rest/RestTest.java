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
import javax.ws.rs.ext.*;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.*;

import org.jglue.cdiunit.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;

@RunWith(CdiRunner.class)
@AdditionalClasses({ RestTest.JsonMessageBodyReader.class, RestTest.XmlMessageBodyReader.class,
        RestTest.YamlMessageBodyReader.class, StringMessageBodyReader.class })
public class RestTest {
    public final static String APPLICATION_YAML = "application/yaml";

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
    public static class JsonMessageBodyReader implements MessageBodyReader<Object> {
        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type != String.class;
        }

        @Override
        public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException {
            return mapper.readValue(entityStream, type);
        }
    }

    @Consumes(APPLICATION_XML)
    public static class XmlMessageBodyReader implements MessageBodyReader<Object> {
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

    @Consumes(APPLICATION_YAML)
    public static class YamlMessageBodyReader implements MessageBodyReader<Object> {
        private final Yaml yaml = new Yaml();

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type != String.class;
        }

        @Override
        public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                MultivaluedMap<String, String> httpHeaders, InputStream entityStream) {
            return yaml.load(entityStream);
        }
    }

    @Produces(APPLICATION_YAML)
    public static class YamlBodyWriter implements MessageBodyWriter<Object> {
        private final Yaml yaml = new Yaml();

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type != String.class;
        }

        @Override
        public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) {
            yaml.dump(t, new OutputStreamWriter(entityStream));
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

    private final DropwizardClientRule service = new DropwizardClientRule(new MockService(), new YamlBodyWriter());

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

    private void setLogLevel(String loggerName, Level level) {
        ((Logger) LoggerFactory.getLogger(loggerName)).setLevel(level);
    }

    private Rest<Pojo> pojo() {
        return base().path("pojo").accept(Pojo.class);
    }

    @Before
    public void before() {
        setLogLevel("org.apache.http.wire", DEBUG);
        setLogLevel("com.github.t1.rest", DEBUG);
    }

    @Test(expected = RuntimeException.class)
    public void shouldFailParsingUnsupportedScheme() {
        rest.uri("mailto:test@example.com");
    }

    @Test
    public void shouldGetPing() {
        String pong = base().path("ping").accept(String.class).get();

        assertEquals("pong", pong);
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
    public void shouldGetJsonPojo() {
        JsonPojo pojo = base().path("jsonpojo").accept(JsonPojo.class).get();

        assertEquals("json", pojo.getString());
    }

    @Test
    public void shouldLimitTypeToSomethingConvertibleTo() {
        base().path("jsonpojo").accept(JsonPojo.class).as(APPLICATION_JSON_TYPE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToLimitTypeToSomethingNotConvertibleTo() {
        base().path("jsonpojo").accept(JsonPojo.class).as(APPLICATION_XML_TYPE);
    }

    @Test
    public void shouldGetPojoAsAnything() {
        Pojo pojo = pojo().get();

        assertEquals("s", pojo.getString());
        assertEquals(123, pojo.getI());
    }

    @Test
    public void shouldGetPojoAsJson() {
        Pojo pojo = pojo().as(APPLICATION_JSON_TYPE).get();

        assertEquals("s", pojo.getString());
        assertEquals(123, pojo.getI());
    }

    @Test
    public void shouldGetPojoAsXml() {
        Pojo pojo = pojo().as(APPLICATION_XML).get();

        assertEquals("s", pojo.getString());
        assertEquals(123, pojo.getI());
    }

    @Test
    public void shouldGetPojoAsYaml() {
        Pojo pojo = pojo().as(APPLICATION_YAML).get();

        assertEquals("s", pojo.getString());
        assertEquals(123, pojo.getI());
    }
}
