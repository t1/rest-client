package com.github.t1.rest;

import com.github.t1.rest.UriAuthority.*;
import com.github.t1.rest.UriTemplate.*;
import org.junit.Test;

import java.net.URI;

import static com.github.t1.rest.UriTemplateAssert.assertThat;
import static org.junit.Assert.*;

public class UriTemplateParseTest {
    private UriTemplateAssert assertThatTemplateOf(String uri) {
        return assertThat(parse(uri)).matches(URI.create(uri));
    }

    private UriTemplate parse(String uri) {
        UriTemplate template = UriTemplate.fromString(uri);
        assertEquals("uri", uri, template.toString());
        UriTemplate templateFromUri = UriTemplate.from(URI.create(uri));
        assertEquals(template, templateFromUri);
        return template;
    }

    @Test
    public void shouldParseOpaqueUri() {
        assertThatTemplateOf("mailto:test@example.com")
                .isOpaque()
                .isNotHierarchical()
                .isNotRelativePath()
                .isNotAbsolutePath()
                .endsAs(RelativePath.class)
                .part("test@example.com");
    }

    @Test
    public void shouldParseOpaqueUrn() {
        assertThatTemplateOf("urn:isbn:096139210x")
                .isOpaque()
                .endsAs(RelativePath.class)
                .part("isbn:096139210x")
                .schemeSpecificPart("isbn:096139210x");
    }

    @Test
    public void shouldParseOpaqueUriWithFragment() {
        assertThatTemplateOf("news:comp.lang.java#1")
                .isOpaque()
                .endsAs(Fragment.class)
                .part("1");
    }

    @Test
    public void shouldParseAbsoluteOpaqueUriStartingWithTile() {
        assertThatTemplateOf("file:~/calendar")
                .isOpaque()
                .endsAs(PathElement.class)
                .part("~/calendar");
    }

    @Test
    public void shouldParseAbsoluteOpaqueUriStartingWithNonSlash() {
        assertThatTemplateOf("file:calendar")
                .isOpaque()
                .endsAs(RelativePath.class)
                .part("calendar");
    }

    @Test
    public void shouldParseSchemeOnly() {
        UriTemplate template = UriTemplate.fromString("http:");
        assertEquals("uri", "http:", template.toString());
        assertThat(template).isOpaque().endsAs(UriScheme.class).part("http").schemeSpecificPart("");
    }

    @Test
    public void shouldParseRelativeEmptyUri() {
        assertThatTemplateOf("")
                .isHierarchical()
                .isNotOpaque()
                .isRelative()
                .endsAs(UriScheme.class)
                .part(null);
    }

    @Test
    public void shouldParseRelativeUri() {
        assertThatTemplateOf("blub")
                .isRelative()
                .endsAs(RelativePath.class)
                .part("blub");
    }

    @Test
    public void shouldParseRelativeUriWithFragment() {
        assertThatTemplateOf("path#frag")
                .isRelative()
                .isRelativePath()
                .isNotAbsolutePath()
                .endsAs(Fragment.class)
                .part("frag")
                .is(UriPath.class, "path");
    }

    @Test
    public void shouldParseAbsoluteUriWithFragment() {
        assertThatTemplateOf("/path#frag")
                .isRelative()
                .isNotRelativePath()
                .isAbsolutePath()
                .endsAs(Fragment.class)
                .part("frag")
                .is(UriPath.class, "/path");
    }

    @Test
    public void shouldParseRelativeUriWithRelativePathAndFragment() {
        assertThatTemplateOf("docs/guide/collections/designfaq.html#28")
                .isRelative()
                .isRelativePath()
                .endsAs(Fragment.class)
                .part("28");
    }

    @Test
    public void shouldParseRelativeUriWithParentDirectoryRelativePath() {
        assertThatTemplateOf("../../../demo/jfc/SwingSet2/src/SwingSet2.java")
                .isRelative()
                .isRelativePath()
                .endsAs(PathElement.class)
                .part("../../../demo/jfc/SwingSet2/src/SwingSet2.java");
    }

    @Test
    public void shouldParseRelativeUriWithEmptyAbsolutePath() {
        assertThatTemplateOf("/")
                .isRelative()
                .isAbsolutePath()
                .endsAs(AbsolutePath.class)
                .part("/");
    }

    @Test
    public void shouldParseRelativeUriWithOneElementAbsolutePath() {
        assertThatTemplateOf("/path")
                .isRelative()
                .isAbsolutePath()
                .endsAs(AbsolutePath.class)
                .part("/path");
    }

    @Test
    public void shouldParseRelativeUriWithOneElementAbsolutePathEndingInSlash() {
        assertThatTemplateOf("/path/")
                .isRelative()
                .isAbsolutePath()
                .endsAs(PathElement.class)
                .part("/path/");
    }

    @Test
    public void shouldParseRelativeUriWithAbsolutePath() {
        assertThatTemplateOf("/path/of/files")
                .isRelative()
                .isAbsolutePath()
                .endsAs(PathElement.class)
                .part("/path/of/files");
    }

    @Test
    public void shouldParseRelativeUriWithAbsolutePathAndFragment() {
        assertThatTemplateOf("/path/of/files#12")
                .isRelative()
                .isAbsolutePath()
                .endsAs(Fragment.class)
                .part("12");
    }

    @Test
    public void shouldParseRelativeUriWithAbsoluteAuthorityAndPath() {
        assertThatTemplateOf("//host/path/of/files#12")
                .isRelative()
                .isAbsolutePath()
                .endsAs(Fragment.class)
                .part("12");
    }

    @Test
    public void shouldParseRelativeUriWithoutPathButFragment() {
        assertThatTemplateOf("#frag")
                .isRelative()
                .endsAs(Fragment.class)
                .part("frag");
    }

    @Test
    public void shouldParseAbsoluteHierachicalUriWithAbsolutePath() {
        assertThatTemplateOf("file:/path")
                .isHierarchical()
                .isAbsolutePath()
                .endsAs(AbsolutePath.class)
                .part("/path");
    }

    @Test
    public void shouldParseAbsoluteHierachicalUriWithEmptyAuthorityAndAbsolutePath() {
        assertThatTemplateOf("file:///calendar")
                .isHierarchical()
                .isAbsolutePath()
                .endsAs(AbsolutePath.class)
                .part("/calendar");
    }

    @Test
    public void shouldParseAbsoluteHierachicalUriWithAbsolutePathAndFragment() {
        assertThatTemplateOf("file:/calendar#2345")
                .isHierarchical()
                .isAbsolutePath()
                .endsAs(Fragment.class)
                .part("2345");
    }

    @Test
    public void shouldParseHierachicalUriWithHostBasedAuthority() {
        assertThatTemplateOf("http://java.sun.com/j2se/1.3")
                .isHierarchical()
                .isAbsolutePath()
                .endsAs(PathElement.class)
                .part("/j2se/1.3");
    }

    @Test
    public void shouldParseUriWithIp4Host() {
        assertThatTemplateOf("http://127.0.0.1/j2se/1.3")
                .isHierarchical()
                .isAbsolutePath()
                .endsAs(PathElement.class)
                .part("/j2se/1.3");
    }

    @Test
    public void shouldParseUriWithIp6Host() {
        assertThatTemplateOf("http://peter@::1/j2se/1.3")
                .isHierarchical()
                .isAbsolutePath()
                .endsAs(PathElement.class)
                .part("/j2se/1.3");
    }

    @Test
    public void shouldParseHierachicalUriWithRegistryBasedAuthority() {
        assertThatTemplateOf("http://häg")
                .isHierarchical()
                .endsAs(RegistryBasedAuthority.class)
                .part("häg");
    }

    @Test
    public void shouldParseHierachicalUriWithRegistryBasedAuthorityAndPath() {
        assertThatTemplateOf("http://häg/j2se/1.3")
                .isHierarchical()
                .isAbsolutePath()
                .endsAs(PathElement.class)
                .part("/j2se/1.3");
    }

    @Test
    public void shouldParseHierachicalUriWithRegistryBasedAuthorityWithFragment() {
        assertThatTemplateOf("http://häg#123")
                .isHierarchical()
                .endsAs(Fragment.class)
                .part("123");
    }

    @Test
    public void shouldParseHttpsUri() {
        assertThatTemplateOf("https://example.com")
                .endsAs(HostBasedAuthority.class)
                .part("example.com");
    }

    @Test
    public void shouldParseUriWithPathAndQuery() {
        assertThatTemplateOf("http://example.com/path?q=1")
                .isAbsolutePath()
                .endsAs(Query.class)
                .part("q=1");
    }

    @Test
    public void shouldParseUriWithPathAndTwoQueries() {
        assertThatTemplateOf("http://example.com/path?q=1&r=2")
                .isAbsolutePath()
                .endsAs(Query.class)
                .part("q=1&r=2");
    }

    @Test
    public void shouldParseUriWithPathAndMatrix() {
        assertThatTemplateOf("http://example.com/path;m=1")
                .isAbsolutePath()
                .endsAs(MatrixPath.class)
                .part("/path;m=1");
    }

    @Test
    public void shouldParseUriWithPathAndMatrixWithoutValue() {
        assertThatTemplateOf("http://example.com/path;m")
                .isAbsolutePath()
                .endsAs(MatrixPath.class)
                .part("/path;m");
    }

    @Test
    public void shouldParseUriWithSubPathAndMatrix() {
        assertThatTemplateOf("http://example.com/path/pith;m=1")
                .isAbsolutePath()
                .endsAs(MatrixPath.class)
                .part("/path/pith;m=1");
    }

    @Test
    public void shouldParseUriWithPathAndTwoMatrix() {
        assertThatTemplateOf("http://example.com/path;m=1&n=2")
                .isAbsolute()
                .isAbsolutePath()
                .endsAs(MatrixPath.class)
                .part("/path;m=1&n=2");
    }

    @Test
    public void shouldParseUriWithQuery() {
        assertThatTemplateOf("http://example.com?q=1")
                .isAbsolute()
                .isRelativePath()
                .endsAs(Query.class)
                .part("q=1");
    }
}
