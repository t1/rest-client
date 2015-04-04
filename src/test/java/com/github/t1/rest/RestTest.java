package com.github.t1.rest;

import static javax.ws.rs.core.MediaType.*;
import static lombok.AccessLevel.*;
import static org.junit.Assert.*;
import io.dropwizard.testing.junit.DropwizardClientRule;

import javax.ws.rs.*;

import lombok.*;

import org.junit.*;

public class RestTest {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor(access = PRIVATE)
    public static class Pojo {
        private String string;
        private int i;
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
    }

    @Rule
    public DropwizardClientRule service = new DropwizardClientRule(new MockService());

    @Test(expected = RuntimeException.class)
    public void shouldFailParsingUnknownScheme() {
        @SuppressWarnings("unused")
        Rest<?> rest = new Rest<>("mailto:test@example.com");
    }

    @Test
    public void shouldGetPing() {
        String pong = new Rest<>(service.baseUri()).path("ping").accept(String.class).get();

        assertEquals("pong", pong);
    }

    @Test
    @Ignore
    public void shouldGetPojo() {
        Pojo pojo = new Rest<>(service.baseUri()).path("pojo").accept(Pojo.class).get();

        assertEquals("s", pojo.getString());
        assertEquals(123, pojo.getI());
    }
}
