package com.github.t1.rest;

import static com.github.t1.rest.UriTemplate.*;
import static com.github.t1.rest.UriTemplate.Scheme.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class UriTemplateTest {
    @Test
    public void shouldBuildFull() {
        String uri = scheme("s").userInfo("u").host("h").port("po").path("pa").query("q", "v").fragment("f").toString();

        assertEquals("s://u@h:po/pa?q=v#f", uri);
    }

    @Test
    public void shouldBuildFullFromScheme() {
        String uri = https.userInfo("u").host("h").port("po").path("pa").query("q", "v").fragment("f").toString();

        assertEquals("https://u@h:po/pa?q=v#f", uri);
    }

    @Test
    public void shouldBuildHostFromScheme() {
        String uri = https.host("h").port("po").path("pa").toString();

        assertEquals("https://h:po/pa", uri);
    }

    @Test
    public void shouldBuildWithoutUserInfo() {
        String uri = scheme("http").host("example.org").port("8080").path("path").toString();

        assertEquals("http://example.org:8080/path", uri);
    }

    @Test
    public void shouldBuildWithoutPort() {
        String uri = http.host("example.org").path("path").toString();

        assertEquals("http://example.org/path", uri);
    }

    @Test
    public void shouldBuildWithAuthority() {
        String uri = scheme("s").authority("u@h:po").path("pa").query("q", "v").fragment("f").toString();

        assertEquals("s://u@h:po/pa?q=v#f", uri);
    }

    @Test
    public void shouldBuildFileWithEmptyAuthority() {
        String uri = file.authority("").path("path").toString();

        assertEquals("file:///path", uri);
    }

    @Test
    public void shouldBuildHttps() {
        String uri = https.host("example.org").toString();

        assertEquals("https://example.org", uri);
    }

    @Test
    public void shouldBuildFileWithoutAuthority() {
        String uri = file.path("path").toString();

        assertEquals("file:/path", uri);
    }

    @Test
    public void shouldBuildFileWithEmptyHost() {
        String uri = file.host("").path("path").toString();

        assertEquals("file:///path", uri);
    }
}
