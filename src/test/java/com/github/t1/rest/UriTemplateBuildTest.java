package com.github.t1.rest;

import static com.github.t1.rest.UriTemplate.*;
import static com.github.t1.rest.UriTemplate.Scheme.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.github.t1.rest.UriTemplate.Path;
import com.github.t1.rest.UriTemplate.Query;

public class UriTemplateBuildTest {
    @Test
    public void shouldBuildFull() {
        String uri = scheme("s").userInfo("u").host("h").port("po").path("pa").query("q", "v").fragment("f").toString();

        assertEquals("s://u@h:po/pa?q=v#f", uri);
    }

    @Test
    public void shouldBuildFullWithTemplates() {
        String uri = scheme("{s}").userInfo("{u}").host("{h}").port("{po}").path("{pa}") //
                .query("{q}", "{v}").fragment("{f}").toString();

        assertEquals("{s}://{u}@{h}:{po}/{pa}?{q}={v}#{f}", uri);
    }

    @Test
    public void shouldBuildFromScheme() {
        String uri = https.userInfo("u").host("h").port("po").path("pa").toString();

        assertEquals("https://u@h:po/pa", uri);
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
        String uri = scheme("s").authority("u@h:po").path("pa").toString();

        assertEquals("s://u@h:po/pa", uri);
    }

    @Test
    public void shouldBuildWithRegistryAuthority() {
        String uri = scheme("s").authority("räg").path("pa").toString();

        assertEquals("s://räg/pa", uri);
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

    @Test
    public void shouldBuildRelativeFilePath() {
        String uri = file.relativePath("path").toString();

        assertEquals("file:path", uri);
    }

    @Test
    public void shouldBuildRelativeFilePathWithTwoElements() {
        String uri = file.relativePath("path").path("two").toString();

        assertEquals("file:path/two", uri);
    }

    @Test
    public void shouldBuildWithPath() {
        Path path = http.path("path");

        assertEquals("http:/path", path.toString());
    }

    @Test
    public void shouldBuildWithTwoPaths() {
        Path path = http.path("path1").path("path2");

        assertEquals("http:/path1/path2", path.toString());
    }

    @Test
    public void shouldBuildWithThreePaths() {
        Path path = http.path("path1").path("path2").path("path3");

        assertEquals("http:/path1/path2/path3", path.toString());
    }

    @Test
    public void shouldDeriveThreePaths() {
        Path root = http.path("p");
        Path p1 = root.path("1");
        Path p2 = root.path("2");
        Path p3 = root.path("3").path("4");

        assertEquals("http:/p/1", p1.toString());
        assertEquals("http:/p/2", p2.toString());
        assertEquals("http:/p/3/4", p3.toString());
    }

    @Test
    public void shouldBuildWithPathAndMatrix() {
        Path path = http.path("path").matrix("key", "value");

        assertEquals("http:/path;key=value", path.toString());
    }

    @Test
    public void shouldBuildWithPathAndTwoMatrixParams() {
        Path path = http.path("path").matrix("key1", "value1").matrix("key2", "value2");

        assertEquals("http:/path;key1=value1;key2=value2", path.toString());
    }

    @Test
    public void shouldBuildWithPathMatrixPath() {
        Path path = http.path("path").matrix("key", "value").path("path2");

        assertEquals("http:/path;key=value/path2", path.toString());
    }

    @Test
    public void shouldBuildWithQuery() {
        Path path = http.path("path");
        Query query = path.query("key", "value");

        assertEquals(path, query.path());
        assertEquals("http:/path?key=value", query.toString());
    }

    @Test
    public void shouldBuildWithTwoQueries() {
        Path path = http.path("path");
        Query query = path.query("key1", "value1").query("key2", "value2");

        assertEquals(path, query.path());
        assertEquals("http:/path?key1=value1&key2=value2", query.toString());
    }

    @Test
    public void shouldBuildWithThreeQueries() {
        Path path = http.path("path");
        Query query = path.query("key1", "value1").query("key2", "value2").query("key3", "value3");

        assertEquals(path, query.path());
        assertEquals("http:/path?key1=value1&key2=value2&key3=value3", query.toString());
    }

    @Test
    public void shouldBuildWithoutQueryButFragment() {
        String uri = http.path("path").fragment("frag").toString();

        assertEquals("http:/path#frag", uri);
    }
}
