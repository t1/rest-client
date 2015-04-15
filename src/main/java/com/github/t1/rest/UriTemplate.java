package com.github.t1.rest;

import static java.util.Arrays.*;
import static lombok.AccessLevel.*;

import java.net.URI;
import java.util.List;
import java.util.regex.*;

import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

import com.github.t1.rest.UriAuthority.HostBasedAuthority;
import com.github.t1.rest.UriAuthority.HostBasedAuthority.HostBasedAuthorityBuilder;

/** Immutable, fluent, strictly appendable builder for URI templates. */
@RequiredArgsConstructor(access = PRIVATE)
@ExtensionMethod(MethodExtensions.class)
public abstract class UriTemplate {
    private static final Pattern URI_PATTERN = Pattern
            .compile("((?<scheme>[a-zA-Z{][a-zA-Z0-9{}.+-]+):)?(?<schemeSpecificPart>.*?)(\\#(?<fragment>.*))?");

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

        @Override
        public UriScheme with(String name, Object value) {
            return new UriScheme(scheme.replaceVariable(name, value));
        }
    }

    public enum CommonScheme {
        file,
        http,
        https;

        public UriScheme scheme() {
            return UriScheme.scheme(name());
        }

        public UriAuthority authority(String authority) {
            return scheme().authority(authority);
        }

        public UriTemplate authorityAndMore(String authority) {
            return scheme().authorityAndMore(authority);
        }

        public HostBasedAuthorityBuilder userInfo(String userInfo) {
            return scheme().userInfo(userInfo);
        }

        public HostBasedAuthorityBuilder host(String host) {
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

        public UriAuthority authority(String authority) {
            return UriAuthority.authority(this, authority);
        }

        public NonFragment authorityAndMore(String authorityAndMore) {
            return UriAuthority.authorityAndMore(this, authorityAndMore);
        }

        public HostBasedAuthorityBuilder userInfo(String userInfo) {
            return HostBasedAuthority.builder().scheme(this).userInfo(userInfo);
        }

        public HostBasedAuthorityBuilder host(String host) {
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
            String[] split = path.split("\\?", 2);
            NonQuery nonQuery = path(split[0]);
            if (split.length == 2)
                return nonQuery.queryAndMore(split[1]);
            return nonQuery;
        }

        public NonQuery path(String path) {
            if (path == null)
                return this;
            boolean isAbsolute = path.startsWith("/");
            if (isAbsolute)
                path = path.substring(1);
            List<String> split = split(path);
            UriPath result;
            if (isAbsolute)
                result = absolutePath(split.head());
            else
                result = relativePath(split.head());
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

        @Override
        public RelativePath with(String name, Object value) {
            return new RelativePath((NonPath) previous.with(name, value), path.replaceVariable(name, value));
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

        @Override
        public AbsolutePath with(String name, Object value) {
            return new AbsolutePath((NonPath) previous.with(name, value), path.replaceVariable(name, value));
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

        @Override
        public PathElement with(String name, Object value) {
            return new PathElement((UriPath) previous.with(name, value), path.replaceVariable(name, value));
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

        @Override
        public MatrixPath with(String name, Object value) {
            return new MatrixPath((UriPath) previous.with(name, value), //
                    this.key.replaceVariable(name, value), //
                    this.value.replaceVariable(name, value));
        }
    }

    public static abstract class NonQuery extends NonFragment {
        private NonQuery(UriTemplate previous) {
            super(previous);
        }

        public NonFragment queryAndMore(String string) {
            if (string.isEmpty())
                return this;
            List<String> split = asList(string.split("&"));
            return query(split.head()).query(split.tail());
        }

        public Query query(String keyValue) {
            return new Query(this, keyValue);
        }

        @Override
        public Query query(String key, String value) {
            return new Query(this, key, value);
        }
    }

    public static class Query extends NonFragment {
        private Query(UriTemplate before, String keyValue) {
            this(before, key(keyValue), value(keyValue));
        }

        private static String key(String keyValue) {
            return keyValue.split("=")[0];
        }

        private static String value(String keyValue) {
            return keyValue.split("=")[1];
        }

        private Query(UriTemplate before, String key, String value) {
            super(before);
            this.key = key;
            this.value = value;
        }

        private final String key;
        private final String value;

        public NonFragment query(List<String> list) {
            if (list.isEmpty())
                return this;
            return query(list.head()).query(list.tail());
        }

        public Query query(String keyValue) {
            return new Query(this, keyValue);
        }

        @Override
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

        @Override
        public Query with(String name, Object value) {
            return new Query(previous.with(name, value), //
                    this.key.replaceVariable(name, value), //
                    this.value.replaceVariable(name, value));
        }
    }

    public static abstract class NonFragment extends UriTemplate {
        private NonFragment(UriTemplate previous) {
            super(previous);
        }

        public Fragment fragment(String fragment) {
            return new Fragment(this, fragment);
        }

        public abstract NonFragment query(String key, String value);
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

        @Override
        public Fragment with(String name, Object value) {
            return new Fragment(previous.with(name, value), fragment.replaceVariable(name, value));
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
        return findPartString(UriAuthority.class);
    }

    public String userInfo() {
        HostBasedAuthority authority = findPart(HostBasedAuthority.class);
        return (authority == null) ? null : authority.userInfo();
    }

    public String host() {
        HostBasedAuthority authority = findPart(HostBasedAuthority.class);
        return (authority == null) ? null : authority.host();
    }

    public String port() {
        HostBasedAuthority authority = findPart(HostBasedAuthority.class);
        return (authority == null) ? null : authority.port();
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

    public abstract UriTemplate with(String name, Object value);

    /** the string version of this part; for the complete uri, call {@link #toString()} or {@link #toUri()}. */
    public abstract String get();

    public URI toUri() {
        return URI.create(toString());
    }

    @Override
    public abstract String toString();
}
