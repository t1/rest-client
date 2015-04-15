package com.github.t1.rest;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.*;

public class UriTemplateVariableTest {
    @Test
    public void shouldFindTemplateVariables() {
        UriTemplate template = UriTemplate.fromString("{s}://{u}@{h}:{po}/{pa};{mk}={mv}?{qk}={qv}#{f}");

        List<String> variables = template.variables();

        assertEquals(asList("s", "u", "h", "po", "pa", "mk", "mv", "qk", "qv", "f"), variables);
    }

    @Test
    public void shouldReplaceSchemeVariable() {
        UriTemplate template = UriTemplate.fromString("{s}://example.org/path?q=1");

        UriTemplate uri = template.with("s", "http");

        assertEquals("http://example.org/path?q=1", uri.toString());
    }

    @Ignore("we should add tests like this for all parts and all invalid characters")
    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToReplaceSchemeWithSlash() {
        UriTemplate template = UriTemplate.fromString("{s}://example.org/path?q=1");

        System.out.println(template.with("s", "ht/tp"));
    }

    @Test
    public void shouldReplaceUserInfoVariable() {
        UriTemplate template = UriTemplate.fromString("http://peter@{h}:8080/path?q=1");

        UriTemplate uri = template.with("h", "example.org");

        assertEquals("http://peter@example.org:8080/path?q=1", uri.toString());
        assertEquals("peter", uri.userInfo());
        assertEquals("example.org", uri.host());
        assertEquals("8080", uri.port());
    }

    @Test
    public void shouldReplaceHostVariable() {
        UriTemplate template = UriTemplate.fromString("http://{u}@example.org:8080/path?q=1");

        UriTemplate uri = template.with("u", "peter");

        assertEquals("http://peter@example.org:8080/path?q=1", uri.toString());
        assertEquals("peter", uri.userInfo());
        assertEquals("example.org", uri.host());
        assertEquals("8080", uri.port());
    }

    @Test
    public void shouldReplacePortVariable() {
        UriTemplate template = UriTemplate.fromString("http://peter@example.org:{p}/path?q=1");

        UriTemplate uri = template.with("p", 8080);

        assertEquals("http://peter@example.org:8080/path?q=1", uri.toString());
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
        UriTemplate template = UriTemplate.fromString("http://peter@example.org:{p}/path?q=1");

        UriTemplate uri = template.with("p", 8080);

        assertEquals("http://peter@example.org:8080/path?q=1", uri.toString());
        assertEquals("peter", uri.userInfo());
        assertEquals("example.org", uri.host());
        assertEquals("8080", uri.port());
    }

    @Test
    public void shouldReplaceRelativePathVariable() {
        UriTemplate template = UriTemplate.fromString("file:{p}?q=1");

        UriTemplate uri = template.with("p", 23);

        assertEquals("file:23?q=1", uri.toString());
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
