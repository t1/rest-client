package com.github.t1.rest;

import java.util.regex.*;

import lombok.RequiredArgsConstructor;

import com.github.t1.rest.UriAuthorityTemplate.HostBasedAuthorityTemplate;
import com.github.t1.rest.UriAuthorityTemplate.HostBasedAuthorityTemplate.HostBasedAuthorityTemplateBuilder;

/** Immutable, typesafe, fluent, strictly appendable builder for URI templates. */
@RequiredArgsConstructor
public class UriTemplate {
    private static final Pattern URI_PATTERN = Pattern
            .compile("((?<scheme>[a-zA-Z{][a-zA-Z0-9{}.+-]*):)?(?<schemeSpecificPart>.*?)(\\#(?<fragment>.*))?");

    public static UriTemplate fromString(String uri) {
        Matcher matcher = URI_PATTERN.matcher(uri);
        if (!matcher.matches())
            throw new IllegalArgumentException("unparseable uri: " + uri);
        String schemeSpecificPart = matcher.group("schemeSpecificPart");
        String fragment = matcher.group("fragment");

        UriTemplate uriTemplate = UriScheme.scheme(matcher.group("scheme")) //
        // .schemeSpecificPart(schemeSpecificPart)
        ;
        // if (fragment != null)
        // uriTemplate.fragment(fragment);
        return uriTemplate;
    }

    public static class UriScheme extends UriTemplate {
        public static UriScheme scheme(String scheme) {
            return new UriScheme(scheme);
        }

        public UriScheme(String scheme) {
            super(null); // has no previous
            this.scheme = scheme;
        }

        private final String scheme;

        public HostBasedAuthorityTemplateBuilder userInfo(String userInfo) {
            return HostBasedAuthorityTemplate.builder().scheme(this).userInfo(userInfo);
        }

        public HostBasedAuthorityTemplateBuilder host(String host) {
            return userInfo(null).host(host);
        }

        public Path path(String path) {
            return authority(null).path(path);
        }

        public Path relativePath(String path) {
            return new RelativePath(this, path);
        }

        public UriAuthorityTemplate authority(String authority) {
            return UriAuthorityTemplate.authority(this, authority);
        }

        @Override
        public String toString() {
            return scheme + ":";
        }
    }

    public enum CommonScheme {
        file,
        http,
        https;

        public UriScheme scheme() {
            return UriScheme.scheme(name());
        }

        public UriAuthorityTemplate authority(String authority) {
            return scheme().authority(authority);
        }

        public HostBasedAuthorityTemplateBuilder userInfo(String userInfo) {
            return scheme().userInfo(userInfo);
        }

        public HostBasedAuthorityTemplateBuilder host(String host) {
            return scheme().host(host);
        }

        public Path path(String path) {
            return scheme().path(path);
        }

        public Path relativePath(String path) {
            return scheme().relativePath(path);
        }
    }

    public static abstract class Path extends UriTemplate {
        public Path(UriTemplate previous) {
            super(previous);
        }

        public Path path(String path) {
            return new PathElement(this, path);
        }

        public Query query(String key, String value) {
            return new Query(this, true, key, value);
        }

        public Fragment fragment(String fragment) {
            return query(null, null).fragment(fragment);
        }

        public Path matrix(String key, String value) {
            return new MatrixPath(this, key, value);
        }
    }

    public static class AbsolutePath extends Path {
        private final String path;

        public AbsolutePath(UriAuthorityTemplate previous, String path) {
            super(previous);
            this.path = path;
        }

        @Override
        public String toString() {
            return previous + "/" + path;
        }
    }

    public static class RelativePath extends Path {
        public RelativePath(UriScheme previous, String path) {
            super(previous);
            this.path = path;
        }

        private final String path;

        @Override
        public String toString() {
            return previous + path;
        }
    }

    public static class PathElement extends Path {
        private final String path;

        public PathElement(Path previous, String path) {
            super(previous);
            this.path = path;
        }

        @Override
        public String toString() {
            return previous + "/" + path;
        }
    }

    public static class MatrixPath extends Path {
        private final String key;
        private final String value;

        public MatrixPath(Path previous, String key, String value) {
            super(previous);
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return previous + ";" + key + "=" + value;
        }
    }

    public static class Query extends UriTemplate {
        public Query(UriTemplate before, boolean first, String key, String value) {
            super(before);
            this.first = first;
            this.key = key;
            this.value = value;
        }

        private final boolean first;
        private final String key;
        private final String value;

        public Fragment fragment(String fragment) {
            return new Fragment(this, fragment);
        }

        public Query query(String key, String value) {
            return new Query(this, false, key, value);
        }

        @Override
        public String toString() {
            return previous + ((key == null) ? "" : (first ? "?" : "&") + key + "=" + value);
        }
    }

    public static class Fragment extends UriTemplate {
        private final String fragment;

        public Fragment(UriTemplate previous, String fragment) {
            super(previous);
            this.fragment = fragment;
        }

        @Override
        public String toString() {
            return previous + "#" + fragment;
        }
    }

    protected final UriTemplate previous;

    public boolean isOpaque() {
        return false;
    }

    public boolean isAbsolute() {
        return false;
    }

    public boolean isRelative() {
        return false;
    }

    public boolean isHierarchical() {
        return false;
    }

    public String schemeSpecificPart() {
        return null;
    }

    public String fragment() {
        return null;
    }

    public String authority() {
        return null;
    }
}
