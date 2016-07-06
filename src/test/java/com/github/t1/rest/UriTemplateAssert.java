package com.github.t1.rest;

import java.net.URI;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

public class UriTemplateAssert {
    public static UriTemplateAssert assertThat(UriTemplate template) {
        return new UriTemplateAssert(template);
    }

    private final UriTemplate template;

    public UriTemplateAssert(UriTemplate template) {
        this.template = template;
    }

    public UriTemplateAssert matches(URI uri) {
        assertEquals("opaque", uri.isOpaque(), template.isOpaque());
        assertEquals("absolute", uri.isAbsolute(), template.isAbsolute());
        assertEquals("relative", !uri.isAbsolute(), template.isRelative());
        assertEquals("scheme", uri.getScheme(), template.scheme());
        assertEquals("schemeSpecificPart", uri.getSchemeSpecificPart(), template.schemeSpecificPart());
        assertEquals("authority", toAuthority(uri), template.authority());
        assertEquals("userInfo", toUserInfo(uri), template.userInfo());
        assertEquals("host", toHost(uri), template.host());
        assertEquals("port", toPortString(uri.getPort()), template.port());
        assertEquals("path", uri.getPath(), template.path());
        assertEquals("query", uri.getQuery(), template.query());
        assertEquals("fragment", uri.getFragment(), template.fragment());
        return this;
    }

    public UriTemplateAssert is(String string) {
        assertEquals(string, template.toString());
        return this;
    }

    private String toUserInfo(URI uri) {
        String userInfo = uri.getUserInfo();
        if (userInfo == null && uri.getAuthority() != null && uri.getAuthority().contains("peter@"))
            return "peter";
        return userInfo;
    }

    /** we consider an empty authority as in 'file:///' to be empty, not null */
    private String toAuthority(URI uri) {
        String authority = uri.getAuthority();
        if (authority == null && uri.toString().contains("///"))
            return "";
        return authority;
    }

    private String toHost(URI uri) {
        String host = uri.getHost();
        if (host == null && uri.toString().contains("::1"))
            return "::1";
        return host;
    }

    /** port could be a template, so it must be a string, but it's an int in URI */
    private String toPortString(int port) {
        return (port < 0) ? null : Integer.toString(port);
    }

    public UriTemplateAssert isOpaque() {
        assertTrue("is opaque", template.isOpaque());
        return this;
    }

    public UriTemplateAssert isNotOpaque() {
        assertFalse("is not opaque", template.isOpaque());
        return this;
    }

    public UriTemplateAssert isAbsolute() {
        assertTrue("is absolute", template.isAbsolute());
        return this;
    }

    public UriTemplateAssert isRelative() {
        assertTrue("is relative", template.isRelative());
        return this;
    }

    public UriTemplateAssert isHierarchical() {
        assertTrue("is hierarchical", template.isHierarchical());
        return this;
    }

    public UriTemplateAssert isNotHierarchical() {
        assertFalse("is not hierarchical", template.isHierarchical());
        return this;
    }

    public UriTemplateAssert isAbsolutePath() {
        assertTrue("is absolute path", template.isAbsolutePath());
        return this;
    }

    public UriTemplateAssert isNotAbsolutePath() {
        assertFalse("is not absolute path", template.isAbsolutePath());
        return this;
    }

    public UriTemplateAssert isRelativePath() {
        assertTrue("is relative path", template.isRelativePath());
        return this;
    }

    public UriTemplateAssert isNotRelativePath() {
        assertFalse("is not relative path", template.isRelativePath());
        return this;
    }

    public UriTemplateAssert endsAs(Class<? extends UriTemplate> type) {
        assertTrue("expected " + type.getSimpleName() + " but found a " + template.getClass().getSimpleName(),
                type.isInstance(template));
        return this;
    }

    public UriTemplateAssert hasVariables(String... names) {
        assertEquals(asList(names), template.variables());
        return this;
    }

    public UriTemplateAssert part(String string) {
        assertEquals(string, template.get());
        return this;
    }

    public UriTemplateAssert is(Class<? extends UriTemplate> type, String string) {
        assertEquals(type.getSimpleName(), string, template.findPart(type).toString());
        return this;
    }

    public UriTemplateAssert schemeSpecificPart(String string) {
        assertEquals("scheme specific part", string, template.schemeSpecificPart());
        return this;
    }
}
