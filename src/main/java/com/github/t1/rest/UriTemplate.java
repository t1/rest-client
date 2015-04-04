package com.github.t1.rest;

import static java.util.Arrays.*;
import static lombok.AccessLevel.*;

import java.util.List;
import java.util.regex.*;

import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

import com.github.t1.rest.UriAuthorityTemplate.HostBasedAuthorityTemplate;
import com.github.t1.rest.UriAuthorityTemplate.HostBasedAuthorityTemplate.HostBasedAuthorityTemplateBuilder;

/** Immutable, fluent, strictly appendable builder for URI templates. */
@RequiredArgsConstructor(access = PRIVATE)
@ExtensionMethod(MethodExtensions.class)
public abstract class UriTemplate {
    private static final Pattern URI_PATTERN = Pattern
            .compile("((?<scheme>[a-zA-Z{][a-zA-Z0-9{}.+-]*):)?(?<schemeSpecificPart>.*?)(\\#(?<fragment>.*))?");

    public static UriTemplate fromString(String uri) {
        Matcher matcher = URI_PATTERN.matcher(uri);
        if (!matcher.matches())
            throw new IllegalArgumentException("unparseable uri: " + uri);

        NonFragment uriTemplate = UriScheme.scheme(matcher.group("scheme")) //
                .schemeSpecificPart(matcher.group("schemeSpecificPart"));
        String fragment = matcher.group("fragment");
        if (fragment == null)
            return uriTemplate;
        return uriTemplate.fragment(fragment);
    }

    private static List<String> split(String path) {
        List<String> list = asList(path.split("/"));
        if (path.endsWith("/"))
            list = list.with("");
        return list;
    }

    public static class UriScheme extends NonAuthority {
        public static UriScheme scheme(String scheme) {
            return new UriScheme(scheme);
        }

        public NonFragment schemeSpecificPart(String schemeSpecificPart) {
            if (schemeSpecificPart == null || schemeSpecificPart.isEmpty())
                return this;
            if (schemeSpecificPart.startsWith("//"))
                return authorityAndMore(schemeSpecificPart.substring(2));
            return pathAndMore(schemeSpecificPart);
        }

        private UriScheme(String scheme) {
            super(null); // has no previous
            this.scheme = scheme;
        }

        private final String scheme;

        @Override
        public String toString() {
            return (scheme == null) ? "" : scheme + ":";
        }

        @Override
        public String get() {
            return scheme;
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

        public UriTemplate authorityAndMore(String authority) {
            return scheme().authorityAndMore(authority);
        }

        public HostBasedAuthorityTemplateBuilder userInfo(String userInfo) {
            return scheme().userInfo(userInfo);
        }

        public HostBasedAuthorityTemplateBuilder host(String host) {
            return scheme().host(host);
        }

        public NonFragment pathAndMore(String path) {
            return scheme().pathAndMore(path);
        }

        public UriPath absolutePath(String path) {
            return scheme().absolutePath(path);
        }

        public UriPath relativePath(String path) {
            return scheme().relativePath(path);
        }
    }

    public static abstract class NonAuthority extends NonPath {
        private NonAuthority(UriTemplate previous) {
            super(previous);
        }

        public UriAuthorityTemplate authority(String authority) {
            return UriAuthorityTemplate.authority(this, authority);
        }

        public NonFragment authorityAndMore(String authorityAndMore) {
            return UriAuthorityTemplate.authorityAndMore(this, authorityAndMore);
        }

        public HostBasedAuthorityTemplateBuilder userInfo(String userInfo) {
            return HostBasedAuthorityTemplate.builder().scheme(this).userInfo(userInfo);
        }

        public HostBasedAuthorityTemplateBuilder host(String host) {
            return userInfo(null).host(host);
        }
    }

    public static abstract class NonPath extends NonQuery {
        NonPath(UriTemplate previous) {
            super(previous);
        }

        public NonFragment pathAndMore(String path) {
            if (path == null)
                return this;
            boolean isAbsolute = path.startsWith("/");
            if (isAbsolute)
                path = path.substring(1);
            List<String> split = split(path);
            UriPath result;
            if (isAbsolute)
                result = new AbsolutePath(this, split.head());
            else
                result = new RelativePath(this, split.head());
            return result.path(split.tail());
        }

        public UriPath absolutePath(String path) {
            return new AbsolutePath(this, path);
        }

        public UriPath relativePath(String path) {
            return new RelativePath(this, path);
        }
    }

    public static abstract class UriPath extends NonQuery {
        private UriPath(UriTemplate previous) {
            super(previous);
        }

        protected String check(String path) {
            if (path == null)
                throw new IllegalArgumentException("path elements must not be null");
            if (path.contains("/"))
                throw new IllegalArgumentException("path elements must not contain slashes: " + path);
            return path;
        }

        public UriPath path(String path) {
            return path(split(path));
        }

        public UriPath path(List<String> path) {
            if (path.isEmpty())
                return this;
            return new PathElement(this, path.head()).path(path.tail());
        }

        public UriPath matrix(String key, String value) {
            return new MatrixPath(this, key, value);
        }
    }

    public static class RelativePath extends UriPath {
        private RelativePath(NonPath previous, String path) {
            super(previous);
            this.path = check(path);
        }

        private final String path;

        @Override
        public String toString() {
            return previous + path;
        }

        @Override
        public String get() {
            return path;
        }
    }

    public static class AbsolutePath extends UriPath {
        private final String path;

        AbsolutePath(NonPath previous, String path) {
            super(previous);
            this.path = check(path);
        }

        @Override
        public String toString() {
            return previous + "/" + path;
        }

        @Override
        public String get() {
            return "/" + path;
        }
    }

    public static class PathElement extends UriPath {
        private final String path;

        private PathElement(UriPath previous, String path) {
            super(previous);
            this.path = check(path);
        }

        @Override
        public String toString() {
            return previous + "/" + path;
        }

        @Override
        public String get() {
            return previous.get() + "/" + path;
        }
    }

    public static class MatrixPath extends UriPath {
        private final String key;
        private final String value;

        private MatrixPath(UriPath previous, String key, String value) {
            super(previous);
            this.key = check(key);
            this.value = check(value);
        }

        @Override
        public String toString() {
            return previous + ";" + key + "=" + value;
        }

        @Override
        public String get() {
            return previous + ";" + key + "=" + value;
        }
    }

    public static abstract class NonQuery extends NonFragment {
        private NonQuery(UriTemplate previous) {
            super(previous);
        }

        public Query query(String key, String value) {
            return new Query(this, key, value);
        }
    }

    public static class Query extends NonFragment {
        private Query(UriTemplate before, String key, String value) {
            super(before);
            this.key = key;
            this.value = value;
        }

        private final String key;
        private final String value;

        public Query query(String key, String value) {
            return new Query(this, key, value);
        }

        private boolean first() {
            return !(previous instanceof Query);
        }

        @Override
        public String toString() {
            return previous + ((key == null) ? "" : (first() ? "?" : "&") + key + "=" + value);
        }

        @Override
        public String get() {
            return (first() ? "" : previous.get() + "&") + key + "=" + value;
        }
    }

    public static abstract class NonFragment extends UriTemplate {
        private NonFragment(UriTemplate previous) {
            super(previous);
        }

        public Fragment fragment(String fragment) {
            return new Fragment(this, fragment);
        }
    }

    public static class Fragment extends UriTemplate {
        private final String fragment;

        private Fragment(UriTemplate previous, String fragment) {
            super(previous);
            this.fragment = fragment;
        }

        @Override
        public String toString() {
            return previous + "#" + fragment;
        }

        @Override
        public String get() {
            return fragment;
        }

        @Override
        public String schemeSpecificPart() {
            return previous.schemeSpecificPart();
        }
    }

    protected final UriTemplate previous;

    public boolean isOpaque() {
        return isAbsolute() && (schemeSpecificPart() == null || !schemeSpecificPart().startsWith("/"));
    }

    public boolean isHierarchical() {
        return !isOpaque();
    }

    public boolean isRelativePath() {
        return path() != null && !path().startsWith("/");
    }

    public boolean isAbsolutePath() {
        return path() != null && path().startsWith("/");
    }

    public boolean isAbsolute() {
        return scheme() != null;
    }

    public boolean isRelative() {
        return scheme() == null;
    }

    private String findPartString(Class<? extends UriTemplate> type) {
        UriTemplate part = findPart(type);
        return (part == null) ? null : part.get();
    }

    private <T extends UriTemplate> T findPart(Class<T> type) {
        for (UriTemplate part = this; part != null; part = part.previous)
            if (type.isInstance(part))
                return type.cast(part);
        return null;
    }

    public String scheme() {
        return findPartString(UriScheme.class);
    }

    public String schemeSpecificPart() {
        int start = isRelative() ? 0 : scheme().length() + 1;
        return toString().substring(start);
    }

    public String authority() {
        return findPartString(UriAuthorityTemplate.class);
    }

    public String userInfo() {
        UriAuthorityTemplate authority = findPart(UriAuthorityTemplate.class);
        if (authority instanceof HostBasedAuthorityTemplate)
            return authority.userInfo();
        return null;
    }

    public String host() {
        UriAuthorityTemplate authority = findPart(UriAuthorityTemplate.class);
        if (authority instanceof HostBasedAuthorityTemplate)
            return authority.host();
        return null;
    }

    public String port() {
        UriAuthorityTemplate authority = findPart(UriAuthorityTemplate.class);
        if (authority instanceof HostBasedAuthorityTemplate)
            return authority.port();
        return null;
    }

    public String path() {
        return isOpaque() ? null : findPartString(UriPath.class).or("");
    }

    public String query() {
        return isOpaque() ? null : findPartString(Query.class);
    }

    public String fragment() {
        return findPartString(Fragment.class);
    }

    public abstract String get();

    @Override
    public abstract String toString();
}
