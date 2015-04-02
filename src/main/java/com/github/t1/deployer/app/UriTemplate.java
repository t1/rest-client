package com.github.t1.deployer.app;

import static lombok.AccessLevel.*;

import java.util.*;
import java.util.regex.*;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * Immutable, fluent, strictly appendable builder for URI templates. <br/>
 * We can't use UriBuilder, as that doesn't allow templates everywhere.
 */
@Getter
@Accessors(fluent = true)
public abstract class UriTemplate implements Cloneable {
    private static final Pattern URI_PATTERN = Pattern
            .compile("((?<scheme>[a-zA-Z{][a-zA-Z0-9{}.+-]*):)?(?<schemeSpecificPart>.*?)(\\#(?<fragment>.*))?");

    public static UriTemplate fromString(String uri) {
        Matcher matcher = URI_PATTERN.matcher(uri);
        if (!matcher.matches())
            throw new IllegalArgumentException("unparseable uri: " + uri);
        String scheme = matcher.group("scheme");
        String schemeSpecificPart = matcher.group("schemeSpecificPart");
        String fragment = matcher.group("fragment");

        UriTemplate uriTemplate;
        if (scheme == null) {
            uriTemplate = new RelativeUriTemplate(schemeSpecificPart);
        } else {
            if (schemeSpecificPart.startsWith("/")) {
                uriTemplate = new HierarchicalUriTemplate(scheme).schemeSpecificPart(schemeSpecificPart);
            } else {
                uriTemplate = new OpaqueUriTemplate(scheme, schemeSpecificPart);
            }
        }
        if (fragment != null)
            uriTemplate.fragment(fragment);
        return uriTemplate;
    }

    /** scheme is null */
    @Getter
    public static class RelativeUriTemplate extends HierarchicalUriTemplate {
        private final String schemeSpecificPart;

        public RelativeUriTemplate(String schemeSpecificPart) {
            super(null);
            this.schemeSpecificPart = schemeSpecificPart;
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class HierarchicalUriTemplate extends UriTemplate {
        @Value
        private static class PathElement {
            String name;
            Map<String, String> matrix;

            @Override
            public String toString() {
                return name;
            }
        }

        private final String scheme;
        private UriAuthority authority;
        @Getter(NONE)
        private List<PathElement> path;
        @Getter(NONE)
        private Map<String, String> query;

        public HierarchicalUriTemplate schemeSpecificPart(String schemeSpecificPart) {
            // [<tt><b>//</b></tt><i>authority</i>][<i>path</i>][<tt><b>?</b></tt><i>query</i>]
            if (schemeSpecificPart.startsWith("//")) {
                String[] authoritySplit = schemeSpecificPart.substring(2).split("(/|#)", 2);
                this.authority = UriAuthority.fromString(authoritySplit[0]);
                schemeSpecificPart =
                        (authoritySplit.length == 1) ? null : (schemeSpecificPart.substring(0, 1) + authoritySplit[1]);
            }
            if (schemeSpecificPart != null) {
                this.path = new ArrayList<>();
                for (String element : schemeSpecificPart.split("/")) {
                    this.path.add(new PathElement(element, null));
                }
            }
            return this;
        }

        @Override
        public String schemeSpecificPart() {
            StringBuilder out = new StringBuilder();
            if (authority != null)
                out.append("//").append(authority);
            if (path != null)
                out.append(path());
            if (query != null)
                out.append("?").append(query);
            return out.toString();
        }

        public String path() {
            if (path == null)
                return "";
            StringBuilder out = new StringBuilder();
            boolean first = true;
            for (PathElement element : path) {
                if (first)
                    first = false;
                else
                    out.append("/");
                out.append(element);
            }
            return out.toString();
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class OpaqueUriTemplate extends UriTemplate {
        private final String scheme;
        private final String schemeSpecificPart;

        @Override
        public boolean isOpaque() {
            return true;
        }
    }

    private String fragment;


    private void fragment(String fragment) {
        if (this.fragment != null)
            throw new IllegalStateException("already have a fragment: " + this.fragment);
        this.fragment = fragment;
    }

    @Override
    @SneakyThrows(CloneNotSupportedException.class)
    protected UriTemplate clone() {
        return (UriTemplate) super.clone();
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        if (scheme() != null)
            out.append(scheme()).append(":");
        out.append(schemeSpecificPart());
        if (fragment != null)
            out.append("#").append(fragment);
        return out.toString();
    }

    public boolean isAbsolute() {
        return !isRelative();
    }

    public boolean isRelative() {
        return scheme() == null;
    }

    public abstract String scheme();

    public abstract String schemeSpecificPart();

    public boolean isOpaque() {
        return false;
    }
}
