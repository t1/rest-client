package com.github.t1.rest;

import static com.github.t1.rest.RestContext.*;
import static javax.ws.rs.core.Response.Status.*;
import static org.junit.Assert.*;

import java.net.URI;

import org.junit.*;

import lombok.*;

public class RestClientMockerTest {
    private static final URI BASE = URI.create("http://example.mock");

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pojo {
        String key, value;
    }

    private static final Pojo POJO = new Pojo("k", "v");

    public final RestClientMocker mock = new RestClientMocker();

    @Before
    public void before() {
        mock.before();
    }

    @After
    public void after() {
        mock.after();
    }


    @Test
    public void shouldGetString() {
        mock.on(BASE + "/string").GET().respond("value");

        String value = new RestResource(BASE + "/string").GET();

        assertEquals("value", value);
    }

    @Test
    public void shouldGetInt() {
        mock.on(BASE + "/int").GET().respond(123);

        int value = new RestResource(BASE + "/int").GET(int.class);

        assertEquals(123, value);
    }

    @Test
    public void shouldGetPojo() {
        mock.on(BASE + "/pojo").GET().respond(POJO);

        Pojo value = new RestResource(BASE + "/pojo").GET(Pojo.class);

        assertEquals(POJO, value);
    }

    @Test
    public void shouldGetFromRegistry() {
        mock.on(BASE + "/pojo").GET().respond(POJO);
        RestContext rest = DEFAULT_CONFIG.register("example", BASE + "/pojo");

        Pojo value = rest.resource("example").GET(Pojo.class);

        assertEquals(POJO, value);
    }

    @Test
    public void shouldGetFromRegistryAddingPath() {
        mock.on(BASE + "/pojo").GET().respond(POJO);
        RestContext rest = DEFAULT_CONFIG.register("example", BASE);

        Pojo value = rest.resource("example", "/pojo").GET(Pojo.class);

        assertEquals(POJO, value);
    }

    @Test
    public void shouldFailBasicAuth() {
        mock.on(BASE + "/pojo").GET().requireBasicAuth("u", "p").respond(POJO);
        RestContext rest = DEFAULT_CONFIG.register("example", BASE + "/pojo");

        EntityResponse<Pojo> response = rest.resource("example").GET_Response(Pojo.class);

        assertEquals(UNAUTHORIZED, response.status());
    }

    @Test
    public void shouldFailBasicAuthWithWrongUsername() {
        mock.on(BASE + "/pojo").GET().requireBasicAuth("x", "p").respond(POJO);
        RestContext rest = DEFAULT_CONFIG.register("example", BASE + "/pojo");

        EntityResponse<Pojo> response = rest.resource("example").GET_Response(Pojo.class);

        assertEquals(UNAUTHORIZED, response.status());
    }

    @Test
    public void shouldFailBasicAuthWithWrongPassword() {
        mock.on(BASE + "/pojo").GET().requireBasicAuth("u", "x").respond(POJO);
        RestContext rest = DEFAULT_CONFIG.register("example", BASE + "/pojo");

        EntityResponse<Pojo> response = rest.resource("example").GET_Response(Pojo.class);

        assertEquals(UNAUTHORIZED, response.status());
    }

    @Test
    public void shouldAuthenticate() {
        mock.on(BASE + "/pojo").GET().requireBasicAuth("u", "p").respond(POJO);
        RestContext rest = DEFAULT_CONFIG.register("example", BASE + "/pojo").put(BASE, new Credentials("u", "p"));

        Pojo response = rest.resource("example").GET(Pojo.class);

        assertEquals(POJO, response);
    }
}
