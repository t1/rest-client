package com.github.t1.rest;

import com.github.t1.rest.UriTemplate.*;
import org.junit.Test;

import java.net.URI;

import static com.github.t1.rest.UriTemplate.CommonScheme.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;

public class UriTemplateBuildTest {
    @Test
    public void shouldBuildFull() {
        UriTemplate uri =
                UriScheme.of("s").userInfo("u").host("h").port("po").absolutePath("pa").query("q", "v").fragment("f");

        assertEquals("s://u@h:po/pa?q=v#f", uri.toString());
        assertEquals("f", uri.get());
    }

    @Test
    public void shouldBuildFullWithTemplates() {
        UriTemplate uri = UriScheme.of("{s}").userInfo("{u}").host("{h}").port("{po}").absolutePath("{pa}") //
                .query("{q}", "{v}").fragment("{f}");

        assertEquals("{s}://{u}@{h}:{po}/{pa}?{q}={v}#{f}", uri.toString());
        assertEquals("{f}", uri.get());
    }

    @Test
    public void shouldBuildScheme() {
        UriTemplate uri = UriScheme.of("s");

        assertEquals("s:", uri.toString());
        assertEquals("s", uri.get());
    }

    @Test
    public void shouldBuildSchemeFromUri() {
        UriTemplate uri = UriScheme.of(URI.create("http://example.org"));

        assertEquals("http:", uri.toString());
        assertEquals("http", uri.get());
    }

    @Test
    public void shouldBuildHttp() {
        UriTemplate uri = http.scheme();

        assertEquals("http:", uri.toString());
        assertEquals("http", uri.get());
    }

    @Test
    public void shouldBuildHttps() {
        UriTemplate uri = https.scheme();

        assertEquals("https:", uri.toString());
        assertEquals("https", uri.get());
    }

    @Test
    public void shouldBuildHostBasedAuthority() {
        UriTemplate uri = https.userInfo("u").host("h").port("po");

        assertEquals("https://u@h:po", uri.toString());
        assertEquals("u@h:po", uri.get());
    }

    @Test
    public void shouldBuildHostAndPort() {
        UriTemplate uri = http.host("example.org").port("8080");

        assertEquals("http://example.org:8080", uri.toString());
        assertEquals("example.org:8080", uri.get());
    }

    @Test
    public void shouldBuildHost() {
        UriTemplate uri = http.host("example.org");

        assertEquals("http://example.org", uri.toString());
        assertEquals("example.org", uri.get());
    }

    @Test
    public void shouldBuildAuthority() {
        UriTemplate uri = http.authority("u@h:1");

        assertEquals("http://u@h:1", uri.toString());
        assertEquals("u@h:1", uri.authority());
        assertEquals("u", uri.userInfo());
        assertEquals("h", uri.host());
        assertEquals("1", uri.port());
        assertEquals("u@h:1", uri.get());
    }

    @Test
    public void shouldBuildWithAuthorityUserVariable() {
        UriTemplate uri = http.authority("{u}@h:1");

        assertEquals("http://{u}@h:1", uri.toString());
        assertEquals("{u}@h:1", uri.authority());
        assertEquals("{u}", uri.userInfo());
        assertEquals("h", uri.host());
        assertEquals("1", uri.port());
        assertEquals("{u}@h:1", uri.get());
    }

    @Test
    public void shouldBuildWithAuthorityHostVariable() {
        UriTemplate uri = http.authority("u@{h}:1");

        assertEquals("http://u@{h}:1", uri.toString());
        assertEquals("u@{h}:1", uri.authority());
        assertEquals("u", uri.userInfo());
        assertEquals("{h}", uri.host());
        assertEquals("1", uri.port());
        assertEquals("u@{h}:1", uri.get());
    }

    @Test
    public void shouldBuildWithAuthorityPortVariable() {
        UriTemplate uri = http.authority("u@h:{po}");

        assertEquals("http://u@h:{po}", uri.toString());
        assertEquals("u@h:{po}", uri.authority());
        assertEquals("u", uri.userInfo());
        assertEquals("h", uri.host());
        assertEquals("{po}", uri.port());
        assertEquals("u@h:{po}", uri.get());
    }

    @Test
    public void shouldBuildWithRegistryAuthority() {
        UriTemplate uri = http.authority("räg");

        assertEquals("http://räg", uri.toString());
        assertEquals("räg", uri.get());
    }

    @Test
    public void shouldBuildFileWithEmptyAuthority() {
        UriTemplate uri = file.authority("").absolutePath("path");

        assertEquals("file:///path", uri.toString());
        assertEquals("/path", uri.get());
    }

    @Test
    public void shouldBuildFileWithoutAuthority() {
        UriTemplate uri = file.absolutePath("path");

        assertEquals("file:/path", uri.toString());
        assertEquals("/path", uri.get());
    }

    @Test
    public void shouldBuildFileWithEmptyHost() {
        UriTemplate uri = file.host("").absolutePath("path");

        assertEquals("file:///path", uri.toString());
        assertEquals("/path", uri.get());
    }

    @Test
    public void shouldBuildRelativeFilePath() {
        UriTemplate uri = file.relativePath("path");

        assertEquals("file:path", uri.toString());
        assertEquals("path", uri.get());
    }

    @Test
    public void shouldBuildRelativeFilePathWithTwoElements() {
        UriTemplate uri = file.relativePath("path").path("two");

        assertEquals("file:path/two", uri.toString());
        assertEquals("path/two", uri.get());
    }

    @Test
    public void shouldFailToBuildAbsolutePathNull() {
        Throwable thrown = catchThrowable(() -> http.absolutePath(null));

        assertThat(thrown).isInstanceOf(NullPointerException.class).hasMessage("path");
    }

    @Test
    public void shouldFailToBuildRelativePathNull() {
        Throwable thrown = catchThrowable(() -> file.relativePath(null));

        assertThat(thrown).isInstanceOf(NullPointerException.class).hasMessage("path");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToBuildRelativePathContainingSlashes() {
        file.relativePath("path/with/slashes");
    }

    @Test
    public void shouldBuildWithPath() {
        UriTemplate uri = http.absolutePath("path");

        assertEquals("http:/path", uri.toString());
        assertEquals("/path", uri.get());
    }

    @Test
    public void shouldBuildWithTwoPaths() {
        UriTemplate uri = http.absolutePath("path1").path("path2");

        assertEquals("http:/path1/path2", uri.toString());
        assertEquals("/path1/path2", uri.get());
    }

    @Test
    public void shouldBuildWithThreePaths() {
        UriTemplate uri = http.absolutePath("path1").path("path2").path("path3");

        assertEquals("http:/path1/path2/path3", uri.toString());
        assertEquals("/path1/path2/path3", uri.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToBuildAbsolutePathContainingSlashes() {
        file.absolutePath("path/with/slashes");
    }

    // TODO shouldn't this fail as well?
    @Test
    public void shouldBuildPathElementContainingSlashes() {
        UriPath path = file.absolutePath("path").path("with/slashes");

        assertEquals("file:/path/with/slashes", path.toString());
    }

    @Test
    public void shouldDeriveMultiplePathsFromOneRoot() {
        UriPath root = http.absolutePath("p");
        UriPath p1 = root.path("1");
        UriPath p2 = root.path("2");
        UriPath p3 = root.path("3").path("4");

        assertEquals("http:/p/1", p1.toString());
        assertEquals("http:/p/2", p2.toString());
        assertEquals("http:/p/3/4", p3.toString());

        assertEquals("/p", root.get());
        assertEquals("/p/1", p1.get());
        assertEquals("/p/2", p2.get());
        assertEquals("/p/3/4", p3.get());
    }

    @Test
    public void shouldBuildWithPathAndMatrix() {
        UriPath path = http.absolutePath("path").matrix("key", "value");

        assertEquals("http:/path;key=value", path.toString());
        assertEquals("/path;key=value", path.get());
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
        assertEquals("/path;key1=value1;key2=value2", path.get());
    }

    @Test
    public void shouldBuildWithPathMatrixPath() {
        UriPath path = http.absolutePath("path").matrix("key", "value").path("path2");

        assertEquals("http:/path;key=value/path2", path.toString());
        assertEquals("/path;key=value/path2", path.get());
    }

    @Test
    public void shouldBuildWithQuery() {
        Query query = http.absolutePath("path").query("key", "value");

        assertEquals("http:/path?key=value", query.toString());
        assertEquals("key=value", query.get());
    }

    @Test
    public void shouldBuildWithTwoQueries() {
        Query query = http.absolutePath("path").query("key1", "value1").query("key2", "value2");

        assertEquals("http:/path?key1=value1&key2=value2", query.toString());
        assertEquals("key1=value1&key2=value2", query.get());
    }

    @Test
    public void shouldBuildWithThreeQueries() {
        Query query = http.absolutePath("path").query("key1", "value1").query("key2", "value2").query("key3", "value3");

        assertEquals("http:/path?key1=value1&key2=value2&key3=value3", query.toString());
        assertEquals("key1=value1&key2=value2&key3=value3", query.get());
    }

    @Test
    public void shouldBuildWithoutQueryButFragment() {
        UriTemplate uri = http.absolutePath("path").fragment("frag");

        assertEquals("http:/path#frag", uri.toString());
        assertEquals("frag", uri.get());
    }

    @Test
    public void shouldNotCompileWithTwoFragments() {
        // http.absolutePath("path").fragment("frag1").fragment("frag2");
    }
}
