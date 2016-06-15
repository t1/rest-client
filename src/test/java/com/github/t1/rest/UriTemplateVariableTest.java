package com.github.t1.rest;

import com.github.t1.rest.UriTemplate.Fragment;
import org.assertj.core.api.Assertions;
import org.junit.*;

import static com.github.t1.rest.UriTemplateAssert.assertThat;
import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;

public class UriTemplateVariableTest {
    @Test
    public void shouldFindTemplateVariables() {
        UriTemplate template = UriTemplate.fromString("{s}://{u}@{h}:{po}/{pa};{mk}={mv}?{qk}={qv}#{f}");

        assertThat(template)
                .endsAs(Fragment.class)
                .hasVariables("s", "u", "h", "po", "pa", "mk", "mv", "qk", "qv", "f");
    }

    @Test
    public void shouldReplaceSchemeVariable() {
        UriTemplate template = UriTemplate.fromString("{s}://example.org/path?q=1");

        assertThat(template).is("{s}://example.org/path?q=1");
        assertThat(template.with("s", "http")).is("http://example.org/path?q=1");
    }

    @Ignore("we should add tests like this for all parts and all invalid characters")
    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToReplaceSchemeWithSlash() {
        UriTemplate template = UriTemplate.fromString("{s}://example.org/path?q=1");

        System.out.println(template.with("s", "ht/tp"));
    }

    @Test
    public void shouldReplaceUserInfoVariable() {
        UriTemplate template = UriTemplate.fromString("http://{u}@example.org:8080/path?q=1");

        UriTemplate uri = template.with("u", "peter");

        assertEquals("http://peter@example.org:8080/path?q=1", uri.toString());
        assertEquals("peter@example.org:8080", uri.authority());
        assertEquals("peter", uri.userInfo());
        assertEquals("example.org", uri.host());
        assertEquals("8080", uri.port());
    }

    @Test
    public void shouldReplaceHostVariable() {
        UriTemplate template = UriTemplate.fromString("http://peter@{h}:8080/path?q=1");

        UriTemplate uri = template.with("h", "example.org");

        assertEquals("http://peter@example.org:8080/path?q=1", uri.toString());
        assertEquals("peter@example.org:8080", uri.authority());
        assertEquals("peter", uri.userInfo());
        assertEquals("example.org", uri.host());
        assertEquals("8080", uri.port());
    }

    @Test
    public void shouldReplacePortVariable() {
        UriTemplate template = UriTemplate.fromString("http://peter@example.org:{p}/path?q=1");

        UriTemplate uri = template.with("p", 8080);

        assertEquals("http://peter@example.org:8080/path?q=1", uri.toString());
        assertEquals("peter@example.org:8080", uri.authority());
        assertEquals("peter", uri.userInfo());
        assertEquals("example.org", uri.host());
        assertEquals("8080", uri.port());
    }

    @Test
    @Ignore("not sure, if this would be required")
    public void shouldReplaceAuthorityVariable() {
        UriTemplate template = UriTemplate.fromString("http://{a}/path?q=1");

        UriTemplate uri = template.with("a", "peter@example.org:8080");

        assertEquals("http://peter@example.org:8080/path?q=1", uri.toString());
        assertEquals("peter@example.org:8080", uri.authority());
        assertEquals("peter", uri.userInfo());
        assertEquals("example.org", uri.host());
        assertEquals("8080", uri.port());
    }

    @Ignore("should we add a test like this?")
    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToReplaceHostWithUserInfoOrPort() {
        UriTemplate template = UriTemplate.fromString("http://{a}");

        template.with("a", "peter@example.org:8080"); // can't add userInfo and port by template
    }

    @Test
    public void shouldReplaceRegistryBasedVariable() {
        UriTemplate template = UriTemplate.fromString("http://[{reg}]/path?q=1");

        UriTemplate uri = template.with("reg", "example.org");

        assertEquals("http://[example.org]/path?q=1", uri.toString());
        assertEquals("[example.org]", uri.authority());
        assertNull("userInfo", uri.userInfo());
        assertNull("host", uri.host());
        assertNull("port", uri.port());
    }

    @Test
    public void shouldReplaceRelativePathVariable() {
        UriTemplate template = UriTemplate.fromString("file:{p}?q=1");

        UriTemplate uri = template.with("p", 23);

        assertEquals("file:23?q=1", uri.toString());
    }

    @Test
    public void shouldFailToReplaceRelativePathVariableWithSlashes() {
        UriTemplate template = UriTemplate.fromString("file:{p}/d?q=1");

        Throwable thrown = catchThrowable(() -> template.with("p", "a/b/c"));

        Assertions.assertThat(thrown).hasMessage("path elements must not contain slashes: a/b/c");
    }

    @Test
    public void shouldReplaceRelativePathStarVariableWithSlashes() {
        UriTemplate template = UriTemplate.fromString("file:{*p}/d?q=1");

        UriTemplate uri = template.with("p", "a/b/c");

        assertEquals("file:a/b/c/d?q=1", uri.toString());
    }

    @Test
    public void shouldFailToReplacePathElementVariableWithSlashes() {
        UriTemplate template = UriTemplate.fromString("file:a/{p}/e?q=1");

        Throwable thrown = catchThrowable(() -> template.with("p", "b/c/d"));

        Assertions.assertThat(thrown).hasMessage("path elements must not contain slashes: b/c/d");
    }

    @Test
    public void shouldReplacePathElementStarVariableWithSlashes() {
        UriTemplate template = UriTemplate.fromString("file:a/{*p}/e?q=1");

        UriTemplate uri = template.with("p", "b/c/d");

        assertEquals("file:a/b/c/d/e?q=1", uri.toString());
    }

    @Test
    public void shouldReplaceAbsolutePathVariable() {
        UriTemplate template = UriTemplate.fromString("http://example.org/{p}?q=1");

        UriTemplate uri = template.with("p", 23);

        assertEquals("http://example.org/23?q=1", uri.toString());
    }

    @Test
    public void shouldReplaceTwoPathVariables() {
        UriTemplate template = UriTemplate.fromString("http://example.org/{p}/{q}?q=1");

        UriTemplate uri = template.with("p", 23).with("q", 45);

        assertEquals("http://example.org/23/45?q=1", uri.toString());
    }

    @Test
    public void shouldFailReplaceAbsolutePathVariableWithSlashes() {
        UriTemplate template = UriTemplate.fromString("http://example.org/{p}/d?q=1");

        Throwable thrown = catchThrowable(() -> template.with("p", "a/b/c"));

        Assertions.assertThat(thrown).hasMessage("path elements must not contain slashes: a/b/c");
    }

    @Test
    public void shouldReplaceAbsolutePathStarVariableWithSlashes() {
        UriTemplate template = UriTemplate.fromString("http://example.org/{*p}/d?q=1");

        UriTemplate uri = template.with("p", "a/b/c");

        assertEquals("http://example.org/a/b/c/d?q=1", uri.toString());
    }

    @Test
    public void shouldReplaceMatrixVariableName() {
        UriTemplate template = UriTemplate.fromString("http://example.org/path;{m}=x?q=1");

        UriTemplate uri = template.with("m", 1.2);

        assertEquals("http://example.org/path;1.2=x?q=1", uri.toString());
    }

    @Test
    public void shouldReplaceMatrixVariableValue() {
        UriTemplate template = UriTemplate.fromString("http://example.org/path;m={x}?q=1");

        UriTemplate uri = template.with("x", 12.34);

        assertEquals("http://example.org/path;m=12.34?q=1", uri.toString());
    }

    @Test
    public void shouldReplaceQueryNameVariable() {
        UriTemplate template = UriTemplate.fromString("http://example.org/path?{q}=1");

        UriTemplate uri = template.with("q", "query");

        assertEquals("http://example.org/path?query=1", uri.toString());
    }

    @Test
    public void shouldReplaceQueryValueVariable() {
        UriTemplate template = UriTemplate.fromString("http://example.org/path?q={v}");

        UriTemplate uri = template.with("v", true);

        assertEquals("http://example.org/path?q=true", uri.toString());
    }

    @Test
    public void shouldReplaceFragmentVariable() {
        UriTemplate template = UriTemplate.fromString("http://example.org/path?q=1#{f}");

        UriTemplate uri = template.with("f", 3.5);

        assertEquals("http://example.org/path?q=1#3.5", uri.toString());
    }
}
