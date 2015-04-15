package com.github.t1.rest;

import static org.junit.Assert.*;

import java.net.URI;

import lombok.Value;

import org.junit.Test;

public class UriTemplateParseTest {
    private URI uri(String string) {
        return URI.create(string);
    }

    private UriTemplate parse(String uri) {
        UriTemplate template = UriTemplate.fromString(uri);
        assertEquals("uri", uri, template.toString());
        return template;
    }

    @Value
    private static class Checker {
        UriTemplate template;

        public void isOpaque() {
            assertTrue(template.isOpaque());
        }

        public void isRelative() {
            assertTrue(template.isRelative());
        }

        public void isHierarchical() {
            assertTrue(template.isHierarchical());
        }
    }

    private Checker convert(String uri) {
        UriTemplate template = parse(uri);
        assertUri(uri(uri), template);
        return new Checker(template);
    }

    private void assertUri(URI uri, UriTemplate template) {
        assertEquals("opaque", uri.isOpaque(), template.isOpaque());
        assertEquals("absolute", uri.isAbsolute(), template.isAbsolute());
        assertEquals("relative", !uri.isAbsolute(), template.isRelative());
        assertEquals("scheme", uri.getScheme(), template.scheme());
        assertEquals("schemeSpecificPart", uri.getSchemeSpecificPart(), template.schemeSpecificPart());
        assertEquals("authority", toAuthority(uri), template.authority());
        assertEquals("userInfo", uri.getUserInfo(), template.userInfo());
        assertEquals("host", toHost(uri), template.host());
        assertEquals("port", toPortString(uri.getPort()), template.port());
        assertEquals("path", uri.getPath(), template.path());
        assertEquals("query", uri.getQuery(), template.query());
        assertEquals("fragment", uri.getFragment(), template.fragment());
    }

    /** we consider an empty authority as in 'file:///' to be empty, not null */
    private String toAuthority(URI uri) {
        String authority = uri.getAuthority();
        if (authority == null && uri.toString().contains("//"))
            return "";
        return authority;
    }

    /** we consider an empty host as in 'file:///' to be empty, not null */
    private String toHost(URI uri) {
        String host = uri.getHost();
        if (host == null && uri.toString().contains("//"))
            return "";
        return host;
    }

    /** port could be a template, so it must be a string, but it's an int in URI */
    private String toPortString(int port) {
        return (port < 0) ? null : Integer.toString(port);
    }

    @Test
    public void shouldParseOpaqueUri() {
        convert("mailto:test@example.com").isOpaque();
    }

    @Test
    public void shouldParseOpaqueUrn() {
        convert("urn:isbn:096139210x").isOpaque();
    }

    @Test
    public void shouldParseOpaqueUriWithFragment() {
        convert("news:comp.lang.java#1").isOpaque();
    }

    @Test
    public void shouldParseAbsoluteOpaqueUriStartingWithTile() {
        convert("file:~/calendar").isOpaque();
    }

    @Test
    public void shouldParseAbsoluteOpaqueUriStartingWithNonSlash() {
        convert("file:calendar").isOpaque();
    }

    @Test
    public void shouldParseRelativeEmptyUri() {
        convert("").isRelative();
    }

    @Test
    public void shouldParseRelativeUri() {
        convert("http").isRelative();
    }

    @Test
    public void shouldParseRelativeUriWithFragment() {
        convert("http#frag").isRelative();
    }

    @Test
    public void shouldParseRelativeUriWithRelativePathAndFragment() {
        convert("docs/guide/collections/designfaq.html#28").isRelative();
    }

    @Test
    public void shouldParseRelativeUriWithParentDirectoryRelativePath() {
        convert("../../../demo/jfc/SwingSet2/src/SwingSet2.java").isRelative();
    }

    @Test
    public void shouldParseRelativeUriWithEmptyAbsolutePath() {
        convert("/").isRelative();
    }

    @Test
    public void shouldParseRelativeUriWithOneElementAbsolutePath() {
        convert("/path").isRelative();
    }

    @Test
    public void shouldParseRelativeUriWithOneElementAbsolutePathEndingInSlash() {
        convert("/path/").isRelative();
    }

    @Test
    public void shouldParseRelativeUriWithAbsolutePath() {
        convert("/path/of/files").isRelative();
    }

    @Test
    public void shouldParseRelativeUriWithAbsolutePathAndFragment() {
        convert("/path/of/files#12").isRelative();
    }

    @Test
    public void shouldParseRelativeUriWithAbsoluteAuthorityAndPath() {
        convert("//host/path/of/files#12").isRelative();
    }

    @Test
    public void shouldParseRelativeUriWithoutPathButFragment() {
        convert("#frag").isRelative();
    }

    @Test
    public void shouldParseAbsoluteHierachicalUriWithAbsolutePath() {
        convert("file:/path").isHierarchical();
    }

    @Test
    public void shouldParseAbsoluteHierachicalUriWithEmptyAuthorityAndAbsolutePath() {
        convert("file:///calendar").isHierarchical();
    }

    @Test
    public void shouldParseAbsoluteHierachicalUriWithAbsolutePathAndFragment() {
        convert("file:/calendar#2345").isHierarchical();
    }

    @Test
    public void shouldParseHierachicalUriWithAuthority() {
        convert("http://java.sun.com/j2se/1.3").isHierarchical();
    }

    @Test
    public void shouldParseHttpsUri() {
        convert("https://example.com");
    }

    @Test
    public void shouldParseUriWithPathAndQuery() {
        convert("http://example.com/path?q=1");
    }

    @Test
    public void shouldParseUriWithOathAndTwoQueries() {
        convert("http://example.com/path?q=1&r=2");
    }

    @Test
    public void shouldParseUriWithQuery() {
        convert("http://example.com?q=1");
    }
}
