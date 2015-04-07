package com.github.t1.rest;

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

import lombok.*;

import org.jglue.cdiunit.*;
import org.junit.*;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(CdiRunner.class)
@AdditionalClasses({ RestTest.JsonMessageBodyReader.class, StringMessageBodyReader.class })
public class RestTest {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor(access = PRIVATE)
    public static class Pojo {
        private String string;
        private int i;
    }

    public static class JsonMessageBodyReader implements MessageBodyReader<Pojo> {
        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return true;
        }

        @Override
        public Pojo readFrom(Class<Pojo> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException {
            return mapper.readValue(entityStream, type);
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
        @Produces(APPLICATION_JSON)
        public Pojo pojo() {
            return new Pojo("s", 123);
        }
    }

    private final DropwizardClientRule service = new DropwizardClientRule(new MockService());

    // this rule must be on a method so this bean is valid application scoped, i.e. no public fields
    @Rule
    public DropwizardClientRule service() {
        return service;
    }

    @Inject
    RestConfig rest = new RestConfig();

    private Rest<Object> base() {
        return rest.uri(service.baseUri());
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
}
