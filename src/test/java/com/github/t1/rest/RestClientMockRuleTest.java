package com.github.t1.rest;

import static com.github.t1.rest.RestConfig.*;
import static javax.ws.rs.core.Response.Status.*;
import static org.junit.Assert.*;

import java.net.URI;

import org.junit.*;

import lombok.*;

public class RestClientMockRuleTest {
    private static final URI BASE = URI.create("http://example.mock");

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pojo {
        String key, value;
    }

    private static final Pojo POJO = new Pojo("k", "v");

    @Rule
    public final RestClientMockRule service = new RestClientMockRule();


    @Test
    public void shouldGetString() {
        service.on(BASE + "/string").GET("value");

        String value = new RestResource(BASE + "/string").GET();

        assertEquals("value", value);
    }

    @Test
    public void shouldGetInt() {
        service.on(BASE + "/int").GET(123);

        int value = new RestResource(BASE + "/int").GET(int.class);

        assertEquals(123, value);
    }

    @Test
    public void shouldGetPojo() {
        service.on(BASE + "/pojo").GET(POJO);

        Pojo value = new RestResource(BASE + "/pojo").GET(Pojo.class);

        assertEquals(POJO, value);
    }

    @Test
    public void shouldGetFromRegistry() {
        service.on(BASE + "/pojo").GET(POJO);
        RestConfig rest = DEFAULT_CONFIG.register("example", BASE + "/pojo");

        Pojo value = rest.resource("example").GET(Pojo.class);

        assertEquals(POJO, value);
    }

    @Test
    public void shouldGetFromRegistryAddingPath() {
        service.on(BASE + "/pojo").GET(POJO);
        RestConfig rest = DEFAULT_CONFIG.register("example", BASE);

        Pojo value = rest.resource("example", "/pojo").GET(Pojo.class);

        assertEquals(POJO, value);
    }

    @Test
    public void shouldFailBasicAuth() {
        service.on(BASE + "/pojo").requireBasicAuth("u", "p").GET(POJO);
        RestConfig rest = DEFAULT_CONFIG.register("example", BASE + "/pojo");

        EntityResponse<Pojo> response = rest.resource("example").GET_Response(Pojo.class);

        assertEquals(UNAUTHORIZED, response.status());
    }

    @Test
    public void shouldFailBasicAuthWithWrongUsername() {
        service.on(BASE + "/pojo").requireBasicAuth("x", "p").GET(POJO);
        RestConfig rest = DEFAULT_CONFIG.register("example", BASE + "/pojo");

        EntityResponse<Pojo> response = rest.resource("example").GET_Response(Pojo.class);

        assertEquals(UNAUTHORIZED, response.status());
    }

    @Test
    public void shouldFailBasicAuthWithWrongPassword() {
        service.on(BASE + "/pojo").requireBasicAuth("u", "x").GET(POJO);
        RestConfig rest = DEFAULT_CONFIG.register("example", BASE + "/pojo");

        EntityResponse<Pojo> response = rest.resource("example").GET_Response(Pojo.class);

        assertEquals(UNAUTHORIZED, response.status());
    }

    @Test
    public void shouldAuthenticate() {
        service.on(BASE + "/pojo").requireBasicAuth("u", "p").GET(POJO);
        RestConfig rest = DEFAULT_CONFIG.register("example", BASE + "/pojo").put(BASE, new Credentials("u", "p"));

        Pojo response = rest.resource("example").GET(Pojo.class);

        assertEquals(POJO, response);
    }
}
