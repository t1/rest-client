package com.github.t1.rest;

import static com.github.t1.rest.RestContext.*;
import static javax.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.net.URI;
import java.util.*;

import org.junit.*;

import lombok.Data;

@Ignore("requires internet connection")
public class TutorialTest {
    private static final URI BASE = URI.create("http://httpbin.org");


    @Test
    public void GET_as_String() {
        String body = new RestResource(BASE + "/get?foo=bar").GET();

        assertThat(body.replace('\"', '\'').replaceAll("\\s", "")) //
                .matches(".*'args':\\{'foo':'bar'\\}.*") //
                .matches(".*'url':'" + BASE + "/get\\?foo=bar'.*");
    }

    /** the body returned by http://httpbin.org/get */
    @Data
    public static class HttpBinGetResponse {
        Map<String, String> args;
        String url;

        public HttpBinGetResponse url(String url) {
            this.url = url;
            return this;
        }

        public HttpBinGetResponse arg(String key, String value) {
            if (args == null)
                args = new LinkedHashMap<>();
            args.put(key, value);
            return this;
        }
    }

    @Test
    public void GET_as_pojo() {
        HttpBinGetResponse pojo = new RestResource(BASE + "/get?foo=bar").GET(HttpBinGetResponse.class);

        assertEquals("bar", pojo.args.get("foo"));
        assertEquals(BASE + "/get?foo=bar", pojo.url);
    }

    @Test
    public void register_resource_and_GET() {
        RestContext rest = REST.register("httpbin", BASE + "/get");

        HttpBinGetResponse pojo = rest.resource("httpbin").GET(HttpBinGetResponse.class);

        assertEquals(BASE + "/get", pojo.url);
    }

    @Test
    public void build_registry_and_add_path_and_GET() {
        RestContext rest = REST //
                .register("google", "http://google.com") //
                .register("httpbin", BASE) //
                .register("apple", "http://apple.com") //
                ;

        HttpBinGetResponse pojo = rest.resource("httpbin", "/get").GET(HttpBinGetResponse.class);

        assertEquals(BASE + "/get", pojo.url);
    }

    @Test
    public void GET_response_object_with_status() {
        RestContext rest = REST.register("httpbin", BASE + "/basic-auth/foo/bar");

        EntityResponse<String> response = rest.resource("httpbin").GET_Response(String.class);

        assertEquals(UNAUTHORIZED, response.status());
        assertEquals("", response.get());
    }

    /** the body returned by a successful http://httpbin.org/basic-auth */
    @Data
    private static class AuthenticatedPage {
        boolean authenticated;
        String user;
    }

    @Test
    public void GET_with_basic_auth() {
        RestContext rest = REST //
                .register("httpbin", BASE + "/basic-auth/foo/bar") //
                .register(BASE, new Credentials("foo", "bar"));

        AuthenticatedPage page = rest.resource("httpbin").GET(AuthenticatedPage.class);

        assertTrue("authenticated", page.authenticated);
        assertEquals("foo", page.user);
    }
}
