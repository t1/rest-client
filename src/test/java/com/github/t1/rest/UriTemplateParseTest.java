package com.github.t1.rest;

import static org.junit.Assert.*;

import java.net.URI;

import lombok.Value;

import org.junit.*;

public class UriTemplateParseTest {
    private URI uri(String string) {
        return URI.create(string);
    }

    private UriTemplate parse(String uri) {
        UriTemplate template = UriTemplate.fromString(uri);
        assertEquals(uri, template.toString());
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
        assertEquals(uri.isOpaque(), template.isOpaque());
        assertEquals(uri.isAbsolute(), template.isAbsolute());
        assertEquals(!uri.isAbsolute(), template.isRelative());
        // TODO assertEquals(uri.getScheme(), template.scheme());
        assertEquals(uri.getSchemeSpecificPart(), template.schemeSpecificPart());
        assertEquals(uri.getFragment(), template.fragment());

        assertEquals(uri.getAuthority(), template.authority());
    }

    @Test
    @Ignore
    public void shouldParseOpaqueUri() {
        convert("mailto:test@example.com").isOpaque();
    }

    @Test
    @Ignore
    public void shouldParseOpaqueUrn() {
        convert("urn:isbn:096139210x").isOpaque();
    }

    @Test
    @Ignore
    public void shouldParseOpaqueUriWithFragment() {
        convert("news:comp.lang.java#1").isOpaque();
    }

    @Test
    @Ignore
    public void shouldParseAbsoluteOpaqueUriStartingWithTile() {
        convert("file:~/calendar").isOpaque();
    }

    @Test
    @Ignore
    public void shouldParseAbsoluteOpaqueUriStartingWithNonSlash() {
        convert("file:calendar").isOpaque();
    }

    @Test
    @Ignore
    public void shouldParseRelativeEmptyUri() {
        convert("").isRelative();
    }

    @Test
    @Ignore
    public void shouldParseRelativeUri() {
        convert("http").isRelative();
    }

    @Test
    @Ignore
    public void shouldParseRelativeUriWithFragment() {
        convert("http#frag").isRelative();
    }

    @Test
    @Ignore
    public void shouldParseRelativeUriWithRelativePathAndFragment() {
        convert("docs/guide/collections/designfaq.html#28").isRelative();
    }

    @Test
    @Ignore
    public void shouldParseRelativeUriWithParentDirectoryRelativePath() {
        convert("../../../demo/jfc/SwingSet2/src/SwingSet2.java").isRelative();
    }

    @Test
    @Ignore
    public void shouldParseRelativeUriWithAbsolutePath() {
        convert("/path/of/files").isRelative();
    }

    @Test
    @Ignore
    public void shouldParseRelativeUriWithAbsolutePathAndFragment() {
        convert("/path/of/files#12").isRelative();
    }

    @Test
    @Ignore
    public void shouldParseRelativeUriWithAbsoluteAuthorityAndPath() {
        convert("//host/path/of/files#12").isRelative();
    }

    @Test
    @Ignore
    public void shouldParseRelativeUriWithoutPathButFragment() {
        convert("#frag").isRelative();
    }

    @Test
    @Ignore
    public void shouldParseAbsoluteHierachicalUriWithAbsolutePath() {
        convert("file:/path").isHierarchical();
    }

    @Test
    @Ignore
    public void shouldParseAbsoluteHierachicalUriWithEmptyAuthorityAndAbsolutePath() {
        convert("file:///calendar").isHierarchical();
    }

    @Test
    @Ignore
    public void shouldParseAbsoluteHierachicalUriWithAbsolutePathAndFragment() {
        convert("file:/calendar#2345").isHierarchical();
    }

    @Test
    @Ignore
    public void shouldParseHierachicalUriWithAuthority() {
        convert("http://java.sun.com/j2se/1.3").isHierarchical();
    }

    @Test
    @Ignore
    public void shouldParseHttpsUri() {
        convert("https://example.com");
    }
}
