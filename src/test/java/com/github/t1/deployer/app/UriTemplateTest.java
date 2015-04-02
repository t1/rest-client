package com.github.t1.deployer.app;

import static org.junit.Assert.*;

import java.net.URI;

import org.junit.Test;

import com.github.t1.deployer.app.UriTemplate.HierarchicalUriTemplate;
import com.github.t1.deployer.app.UriTemplate.OpaqueUriTemplate;
import com.github.t1.deployer.app.UriTemplate.RelativeUriTemplate;

public class UriTemplateTest {
    private URI uri(String string) {
        return URI.create(string);
    }

    private UriTemplate parse(String uri) {
        UriTemplate template = UriTemplate.fromString(uri);
        assertEquals(uri, template.toString());
        return template;
    }

    private void convertAndCheck(Class<? extends UriTemplate> expectedType, String uri) {
        UriTemplate template = parse(uri);

        assertUri(uri(uri), template);
        assertTrue("expected a " + expectedType.getName() + " but got a " + template.getClass().getName(),
                expectedType.isInstance(template));
    }

    private void assertUri(URI uri, UriTemplate template) {
        assertEquals(uri.isOpaque(), template.isOpaque());
        assertEquals(uri.isAbsolute(), template.isAbsolute());
        assertEquals(!uri.isAbsolute(), template.isRelative());
        assertEquals(uri.getScheme(), template.scheme());
        assertEquals(uri.getSchemeSpecificPart(), template.schemeSpecificPart());
        assertEquals(uri.getFragment(), template.fragment());

        if (template instanceof HierarchicalUriTemplate) {
            HierarchicalUriTemplate hierarchical = (HierarchicalUriTemplate) template;
            assertEquals(uri.getAuthority(), toString(hierarchical.authority()));
        }
    }

    private String toString(UriAuthority authority) {
        if (authority == null)
            return null;
        String string = authority.toString();
        if (string.isEmpty())
            return null; // this is what URI does with an empty authority
        return string;
    }

    @Test
    public void shouldParseOpaqueUri() {
        convertAndCheck(OpaqueUriTemplate.class, "mailto:test@example.com");
    }

    @Test
    public void shouldParseOpaqueUrn() {
        convertAndCheck(OpaqueUriTemplate.class, "urn:isbn:096139210x");
    }

    @Test
    public void shouldParseOpaqueUriWithFragment() {
        convertAndCheck(OpaqueUriTemplate.class, "news:comp.lang.java#1");
    }

    @Test
    public void shouldParseAbsoluteOpaqueUriStartingWithTile() {
        convertAndCheck(OpaqueUriTemplate.class, "file:~/calendar");
    }

    @Test
    public void shouldParseAbsoluteOpaqueUriStartingWithNonSlash() {
        convertAndCheck(OpaqueUriTemplate.class, "file:calendar");
    }

    @Test
    public void shouldParseRelativeEmptyUri() {
        convertAndCheck(RelativeUriTemplate.class, "");
    }

    @Test
    public void shouldParseRelativeUri() {
        convertAndCheck(RelativeUriTemplate.class, "http");
    }

    @Test
    public void shouldParseRelativeUriWithFragment() {
        convertAndCheck(RelativeUriTemplate.class, "http#frag");
    }

    @Test
    public void shouldParseRelativeUriWithRelativePathAndFragment() {
        convertAndCheck(RelativeUriTemplate.class, "docs/guide/collections/designfaq.html#28");
    }

    @Test
    public void shouldParseRelativeUriWithParentDirectoryRelativePath() {
        convertAndCheck(RelativeUriTemplate.class, "../../../demo/jfc/SwingSet2/src/SwingSet2.java");
    }

    @Test
    public void shouldParseRelativeUriWithAbsolutePath() {
        convertAndCheck(RelativeUriTemplate.class, "/path/of/files");
    }

    @Test
    public void shouldParseRelativeUriWithAbsolutePathAndFragment() {
        convertAndCheck(RelativeUriTemplate.class, "/path/of/files#12");
    }

    @Test
    public void shouldParseRelativeUriWithoutPathButFragment() {
        convertAndCheck(RelativeUriTemplate.class, "#frag");
    }

    @Test
    public void shouldParseAbsoluteHierachicalUriWithAbsolutePath() {
        convertAndCheck(HierarchicalUriTemplate.class, "file:/path");
    }

    @Test
    public void shouldParseAbsoluteHierachicalUriWithEmptyAuthorityAndAbsolutePath() {
        convertAndCheck(HierarchicalUriTemplate.class, "file:///calendar");
    }

    @Test
    public void shouldParseAbsoluteHierachicalUriWithAbsolutePathAndFragment() {
        convertAndCheck(HierarchicalUriTemplate.class, "file:/calendar#2345");
    }

    @Test
    public void shouldParseHierachicalUriWithAuthority() {
        convertAndCheck(HierarchicalUriTemplate.class, "http://java.sun.com/j2se/1.3");
    }

    @Test
    public void shouldParseHttpsUri() {
        convertAndCheck(UriTemplate.class, "https://example.com");
    }
}
