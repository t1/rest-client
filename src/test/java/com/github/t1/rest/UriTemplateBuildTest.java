package com.github.t1.rest;

import static com.github.t1.rest.UriTemplate.CommonScheme.*;
import static com.github.t1.rest.UriTemplate.UriScheme.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.github.t1.rest.UriAuthorityTemplate.HostBasedAuthorityTemplate.HostBasedAuthorityTemplateBuilder;
import com.github.t1.rest.UriTemplate.Query;
import com.github.t1.rest.UriTemplate.UriPath;

public class UriTemplateBuildTest {
    @Test
    public void shouldBuildFull() {
        UriTemplate uri =
                scheme("s").userInfo("u").host("h").port("po").absolutePath("pa").query("q", "v").fragment("f");

        assertEquals("s://u@h:po/pa?q=v#f", uri.toString());
    }

    @Test
    public void shouldBuildFullWithTemplates() {
        UriTemplate uri = scheme("{s}").userInfo("{u}").host("{h}").port("{po}").absolutePath("{pa}") //
                .query("{q}", "{v}").fragment("{f}");

        assertEquals("{s}://{u}@{h}:{po}/{pa}?{q}={v}#{f}", uri.toString());
    }

    @Test
    public void shouldBuildFromScheme() {
        UriTemplate uri = https.userInfo("u").host("h").port("po").absolutePath("pa");

        assertEquals("https://u@h:po/pa", uri.toString());
    }

    @Test
    public void shouldBuildHostFromScheme() {
        UriTemplate uri = https.host("h").port("po").absolutePath("pa");

        assertEquals("https://h:po/pa", uri.toString());
    }

    @Test
    public void shouldBuildWithoutUserInfo() {
        UriTemplate uri = scheme("http").host("example.org").port("8080").absolutePath("path");

        assertEquals("http://example.org:8080/path", uri.toString());
    }

    @Test
    public void shouldBuildWithoutPort() {
        UriTemplate uri = http.host("example.org").absolutePath("path");

        assertEquals("http://example.org/path", uri.toString());
    }

    @Test
    public void shouldBuildWithAuthority() {
        UriTemplate uri = scheme("s").authority("u@h:po").absolutePath("pa");

        assertEquals("s://u@h:po/pa", uri.toString());
    }

    @Test
    public void shouldBuildWithRegistryAuthority() {
        UriTemplate uri = scheme("s").authority("räg").absolutePath("pa");

        assertEquals("s://räg/pa", uri.toString());
    }

    @Test
    public void shouldBuildFileWithEmptyAuthority() {
        UriTemplate uri = file.authority("").absolutePath("path");

        assertEquals("file:///path", uri.toString());
    }

    @Test
    public void shouldBuildHttps() {
        HostBasedAuthorityTemplateBuilder uri = https.host("example.org");

        assertEquals("https://example.org", uri.toString());
    }

    @Test
    public void shouldBuildFileWithoutAuthority() {
        UriTemplate uri = file.absolutePath("path");

        assertEquals("file:/path", uri.toString());
    }

    @Test
    public void shouldBuildFileWithEmptyHost() {
        UriTemplate uri = file.host("").absolutePath("path");

        assertEquals("file:///path", uri.toString());
    }

    @Test
    public void shouldBuildRelativeFilePath() {
        UriTemplate uri = file.relativePath("path");

        assertEquals("file:path", uri.toString());
    }

    @Test
    public void shouldBuildRelativeFilePathWithTwoElements() {
        UriTemplate uri = file.relativePath("path").path("two");

        assertEquals("file:path/two", uri.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToBuildRelativePathContainingSlashes() {
        file.relativePath("path/with/slashes");
    }

    @Test
    public void shouldBuildWithPath() {
        UriPath path = http.absolutePath("path");

        assertEquals("http:/path", path.toString());
    }

    @Test
    public void shouldBuildWithTwoPaths() {
        UriPath path = http.absolutePath("path1").path("path2");

        assertEquals("http:/path1/path2", path.toString());
    }

    @Test
    public void shouldBuildWithThreePaths() {
        UriPath path = http.absolutePath("path1").path("path2").path("path3");

        assertEquals("http:/path1/path2/path3", path.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToBuildAbsolutePathContainingSlashes() {
        file.absolutePath("path/with/slashes");
    }

    @Test
    public void shouldBuildPathElementContainingSlashes() {
        UriPath path = file.absolutePath("path").path("with/slashes");

        assertEquals("file:/path/with/slashes", path.toString());
    }

    @Test
    public void shouldDeriveThreePaths() {
        UriPath root = http.absolutePath("p");
        UriPath p1 = root.path("1");
        UriPath p2 = root.path("2");
        UriPath p3 = root.path("3").path("4");

        assertEquals("http:/p/1", p1.toString());
        assertEquals("http:/p/2", p2.toString());
        assertEquals("http:/p/3/4", p3.toString());
    }

    @Test
    public void shouldBuildWithPathAndMatrix() {
        UriPath path = http.absolutePath("path").matrix("key", "value");

        assertEquals("http:/path;key=value", path.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToBuildPathWithMatrixKeyContainingSlash() {
        http.absolutePath("path").matrix("key/slash", "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToBuildPathWithMatrixValueContainingSlash() {
        http.absolutePath("path").matrix("key", "value/slash");
    }

    @Test
    public void shouldBuildWithPathAndTwoMatrixParams() {
        UriPath path = http.absolutePath("path").matrix("key1", "value1").matrix("key2", "value2");

        assertEquals("http:/path;key1=value1;key2=value2", path.toString());
    }

    @Test
    public void shouldBuildWithPathMatrixPath() {
        UriPath path = http.absolutePath("path").matrix("key", "value").path("path2");

        assertEquals("http:/path;key=value/path2", path.toString());
    }

    @Test
    public void shouldBuildWithQuery() {
        Query query = http.absolutePath("path").query("key", "value");

        assertEquals("http:/path?key=value", query.toString());
    }

    @Test
    public void shouldBuildWithTwoQueries() {
        Query query = http.absolutePath("path").query("key1", "value1").query("key2", "value2");

        assertEquals("http:/path?key1=value1&key2=value2", query.toString());
    }

    @Test
    public void shouldBuildWithThreeQueries() {
        Query query = http.absolutePath("path").query("key1", "value1").query("key2", "value2").query("key3", "value3");

        assertEquals("http:/path?key1=value1&key2=value2&key3=value3", query.toString());
    }

    @Test
    public void shouldBuildWithoutQueryButFragment() {
        UriTemplate uri = http.absolutePath("path").fragment("frag");

        assertEquals("http:/path#frag", uri.toString());
    }

    @Test
    public void shouldNotCompileWithTwoFragments() {
        // http.path("path").fragment("frag1").fragment("frag2");
    }
}
