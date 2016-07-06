package com.github.t1.rest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.github.t1.rest.UriAuthority.HostBasedAuthority;
import lombok.*;
import lombok.experimental.ExtensionMethod;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.*;
import java.net.URI;
import java.util.List;
import java.util.regex.*;

import static com.github.t1.rest.MethodExtensions.*;
import static com.github.t1.rest.PathVariableExpression.*;
import static java.util.Arrays.*;
import static lombok.AccessLevel.*;

/** Immutable, fluent, strictly appendable builder for URI templates. */
@SuppressWarnings("ClassReferencesSubclass")
@Immutable
@RequiredArgsConstructor(access = PRIVATE)
@ExtensionMethod(MethodExtensions.class)
@JsonSerialize(using = ToStringSerializer.class)
@NoArgsConstructor(force = true, access = PRIVATE)
@XmlRootElement
@XmlJavaTypeAdapter(UriTemplate.JaxbAdapter.class)
public abstract class UriTemplate {
    private static final Pattern URI_PATTERN =
            Pattern.compile("((?<scheme>[a-zA-Z{][a-zA-Z0-9{}.+-]+):)?(?<schemeSpecificPart>.*?)(#(?<fragment>.*))?");

    public static UriTemplate from(@NonNull URI uri) {
        return fromString(uri.toString());
    }

    @JsonCreator
    public static UriTemplate fromString(String uri) {
        Matcher matcher = URI_PATTERN.matcher(uri);
        if (!matcher.matches())
            throw new IllegalArgumentException("non-parseable uri: " + uri);

        NonFragment uriTemplate =
                UriScheme.of(matcher.group("scheme")).schemeSpecificPart(matcher.group("schemeSpecificPart"));
        String fragment = matcher.group("fragment");
        if (fragment == null)
            return uriTemplate;
        return uriTemplate.fragment(fragment);
    }

    private static List<String> split(String path) {
        List<String> list = asList(path.split("/"));
        if (path.endsWith("/"))
            list = MethodExtensions.with(list, "");
        return list;
    }

    public static class JaxbAdapter extends XmlAdapter<String, UriTemplate> {
        @Override
        public UriTemplate unmarshal(String value) {
            return UriTemplate.fromString(value);
        }

        @Override
        public String marshal(UriTemplate value) {
            return value.toString();
        }
    }

    @Immutable
    @EqualsAndHashCode(callSuper = true)
    public static class UriScheme extends NonAuthority {
        public static UriScheme of(URI uri) {
            return of(uri.getScheme());
        }

        public static UriScheme of(String scheme) {
            return new UriScheme(scheme);
        }

        private final String scheme;

        private UriScheme(String scheme) {
            super(null); // has no previous
            this.scheme = scheme;
        }

        protected NonFragment schemeSpecificPart(String schemeSpecificPart) {
            if (schemeSpecificPart.isEmpty())
                return this;
            if (schemeSpecificPart.startsWith("//"))
                return authorityAndMore(schemeSpecificPart.substring(2));
            return pathAndMore(schemeSpecificPart);
        }

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
            return new UriScheme(replaceVariable(scheme, name, value));
        }
    }

    public enum CommonScheme {
        file,
        http,
        https;

        public UriScheme scheme() {
            return UriScheme.of(name());
        }

        public UriAuthority authority(String authority) {
            return scheme().authority(authority);
        }

        public HostBasedAuthority userInfo(String userInfo) {
            return scheme().userInfo(userInfo);
        }

        public HostBasedAuthority host(String host) {
            return scheme().host(host);
        }

        public UriPath absolutePath(String path) {
            return scheme().absolutePath(path);
        }

        public UriPath relativePath(String path) {
            return scheme().relativePath(path);
        }
    }

    @Immutable
    public static abstract class NonAuthority extends NonPath {
        private NonAuthority(UriTemplate previous) {
            super(previous);
        }

        public UriAuthority authority(String authority) {
            return UriAuthority.authority(this, authority);
        }

        protected NonFragment authorityAndMore(String authorityAndMore) {
            return UriAuthority.authorityAndMore(this, authorityAndMore);
        }

        public HostBasedAuthority userInfo(String userInfo) {
            return new HostBasedAuthority(this, userInfo);
        }

        public HostBasedAuthority host(String host) {
            return userInfo(null).host(host);
        }
    }

    // @Immutable
    public static abstract class NonPath extends NonQuery {
        NonPath(UriTemplate previous) {
            super(previous);
        }

        protected NonFragment pathAndMore(String path) {
            if (path == null)
                return this;
            String[] split = path.split("\\?", 2);
            NonQuery nonQuery = path(split[0]);
            if (split.length == 2)
                return nonQuery.queryAndMore(split[1]);
            return nonQuery;
        }

        @Override
        public NonQuery path(String path) {
            List<String> split = split(path);
            String head = head(split);
            String[] headAndMatrix = head.split(";", 2);
            UriPath result = (host() != null) ? absolutePath(headAndMatrix[0]) : relativePath(headAndMatrix[0]);
            if (headAndMatrix.length > 1)
                result = result.parseMatrix(headAndMatrix[1]);
            return result.path(tail(split));
        }

        public UriPath absolutePath(String path) {
            return new AbsolutePath(this, path);
        }

        public UriPath relativePath(String path) {
            return new RelativePath(this, path);
        }
    }

    @Immutable
    public static abstract class UriPath extends NonQuery {
        private UriPath(UriTemplate previous) {
            super(previous);
        }

        @Override
        public UriPath path(String path) {
            return path(split(path));
        }

        protected UriPath path(List<String> path) {
            if (path.isEmpty())
                return this;
            String head = head(path);
            String[] headAndMatrix = head.split(";", 2);
            UriPath result = new PathElement(this, headAndMatrix[0]);
            if (headAndMatrix.length > 1)
                result = result.parseMatrix(headAndMatrix[1]);
            return result.path(tail(path));
        }

        protected UriPath parseMatrix(String string) {
            String[] split = string.split("=", 2);
            String value = (split.length == 1) ? null : split[1];
            return matrix(split[0], value);
        }

        public UriPath matrix(String key, String value) {
            return new MatrixPath(this, key, value);
        }
    }

    @Immutable
    public static class RelativePath extends UriPath {
        private RelativePath(NonPath previous, @NonNull String path) {
            super(previous);
            this.path = checkNoSlashes(path);
        }

        private RelativePath(NonPath previous, PathVariableExpression expression) {
            super(previous);
            this.path = expression.resolve();
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
            return new RelativePath((NonPath) previous.with(name, value),
                    new PathVariableExpression(path, name, value));
        }
    }

    @Immutable
    @EqualsAndHashCode(callSuper = true)
    public static class AbsolutePath extends UriPath {
        private final String path;

        AbsolutePath(NonPath previous, @NonNull String path) {
            super(previous);
            this.path = checkNoSlashes(path);
        }

        public AbsolutePath(NonPath previous, PathVariableExpression expression) {
            super(previous);
            this.path = expression.resolve();
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
            return new AbsolutePath((NonPath) previous.with(name, value),
                    new PathVariableExpression(path, name, value));
        }
    }

    @Immutable
    @EqualsAndHashCode(callSuper = true)
    public static class PathElement extends UriPath {
        private final String path;

        private PathElement(UriPath previous, @NonNull String path) {
            super(previous);
            this.path = checkNoSlashes(path);
        }

        public PathElement(UriPath previous, PathVariableExpression expression) {
            super(previous);
            this.path = expression.resolve();
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
            return new PathElement((UriPath) previous.with(name, value), new PathVariableExpression(path, name, value));
        }
    }

    @Immutable
    @EqualsAndHashCode(callSuper = true)
    public static class MatrixPath extends UriPath {
        private final String key;
        private final String value;

        private MatrixPath(UriPath previous, @NonNull String key, String value) {
            super(previous);
            this.key = checkNoSlashes(key);
            this.value = (value == null) ? null : checkNoSlashes(value);
        }

        @Override
        public String toString() {
            return previous + ";" + key + ((value == null) ? "" : ("=" + value));
        }

        @Override
        public String get() {
            return previous.get() + ";" + key + ((value == null) ? "" : ("=" + value));
        }

        @Override
        public MatrixPath with(String name, Object value) {
            return new MatrixPath((UriPath) previous.with(name, value),
                    replaceVariable(this.key, name, value),
                    replaceVariable(this.value, name, value));
        }
    }

    // @Immutable
    public static abstract class NonQuery extends NonFragment {
        private NonQuery(UriTemplate previous) {
            super(previous);
        }

        public abstract NonQuery path(String path);

        protected NonFragment queryAndMore(String string) {
            List<String> split = asList(string.split("&"));
            return query(head(split)).query(tail(split));
        }

        public Query query(String keyValue) {
            return new Query(this, keyValue);
        }

        @Override
        public Query query(String key, String value) {
            return new Query(this, key, value);
        }
    }

    @Immutable
    @EqualsAndHashCode(callSuper = true)
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
            return query(head(list)).query(tail(list));
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
            return previous + (first() ? "?" : "&") + key + "=" + value;
        }

        @Override
        public String get() {
            return (first() ? "" : previous.get() + "&") + key + "=" + value;
        }

        @Override
        public Query with(String name, Object value) {
            return new Query(previous.with(name, value),
                    replaceVariable(this.key, name, value),
                    replaceVariable(this.value, name, value));
        }
    }

    // @Immutable
    public static abstract class NonFragment extends UriTemplate {
        private NonFragment(UriTemplate previous) {
            super(previous);
        }

        public Fragment fragment(String fragment) {
            return new Fragment(this, fragment);
        }

        public abstract NonFragment query(String key, String value);
    }

    @Immutable
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
            return new Fragment(previous.with(name, value), replaceVariable(fragment, name, value));
        }
    }

    protected final UriTemplate previous;

    public boolean isOpaque() {
        return isAbsolute() && (schemeSpecificPart().isEmpty() || !schemeSpecificPart().startsWith("/"));
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

    public <T extends UriTemplate> T findPart(Class<T> type) {
        for (UriTemplate part = this; part != null; part = part.previous)
            if (type.isInstance(part))
                return type.cast(part);
        return null;
    }

    public NonAuthority nonAuthority() {
        return findPart(NonAuthority.class);
    }

    public NonPath nonPath() {
        return findPart(NonPath.class);
    }

    public NonQuery nonQuery() {
        return findPart(NonQuery.class);
    }

    public NonFragment nonFragment() {
        return findPart(NonFragment.class);
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
        return isOpaque() ? null : or(findPartString(UriPath.class), "");
    }

    public String query() {
        return isOpaque() ? null : findPartString(Query.class);
    }

    public String fragment() {
        return findPartString(Fragment.class);
    }

    public abstract UriTemplate with(String name, Object value);

    public List<String> variables() {
        return PathVariableExpression.variables(toString());
    }

    /** the string version of this part; for the complete uri, call {@link #toString()} or {@link #toUri()}. */
    public abstract String get();

    public URI toUri() {
        return URI.create(toString());
    }

    @Override
    public abstract String toString();

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UriTemplate))
            return false;
        return toString().equals(obj.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
