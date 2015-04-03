package com.github.t1.rest;

import java.util.regex.*;

import lombok.*;
import lombok.experimental.Builder;

import com.github.t1.rest.UriTemplate.Path;

@RequiredArgsConstructor
public abstract class UriAuthorityTemplate {
    // http://www.ietf.org/rfc/rfc2396.txt
    // we don't support ip addresses and other, probably esoteric cases
    private static final Pattern HOST_BASED_PATTERN = Pattern.compile("" //
            + "((?<userinfo>.*)@)?" //
            + "(?<host>([\\p{Alnum}.-]*?))" //
            + "(:(?<port>.*))?");

    public static UriAuthorityTemplate authority(UriTemplate scheme, String authority) {
        if (authority == null)
            return new NullAuthority(scheme);
        Matcher matcher = HOST_BASED_PATTERN.matcher(authority);
        if (!matcher.matches())
            return new RegistryBasedAuthorityTemplate(scheme, authority);
        return HostBasedAuthorityTemplate.builder() //
                .scheme(scheme) //
                .userInfo(matcher.group("userinfo")) //
                .host(matcher.group("host")) //
                .port(matcher.group("port")) //
                .build();
    }

    public static class NullAuthority extends UriAuthorityTemplate {
        public NullAuthority(UriTemplate scheme) {
            super(scheme);
        }

        @Override
        public String toString() {
            return scheme.toString();
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class RegistryBasedAuthorityTemplate extends UriAuthorityTemplate {
        String registryName;

        public RegistryBasedAuthorityTemplate(UriTemplate scheme, String registryName) {
            super(scheme);
            this.registryName = registryName;
        }

        @Override
        public String toString() {
            return scheme + "//" + registryName;
        }
    }

    @Value
    @Builder
    @EqualsAndHashCode(callSuper = true)
    public static class HostBasedAuthorityTemplate extends UriAuthorityTemplate {
        public static class HostBasedAuthorityTemplateBuilder {
            UriTemplate scheme;

            public HostBasedAuthorityTemplateBuilder scheme(UriTemplate scheme) {
                this.scheme = scheme;
                return this;
            }

            public Path path(String string) {
                return build().path(string);
            }

            public HostBasedAuthorityTemplate build() {
                return new HostBasedAuthorityTemplate(scheme, userInfo, host, port);
            }

            @Override
            public String toString() {
                return build().toString();
            }
        }

        String userInfo;
        String host;
        String port;

        public HostBasedAuthorityTemplate(UriTemplate scheme, String userInfo, String host, String port) {
            super(scheme);
            this.userInfo = userInfo;
            this.host = host;
            this.port = port;
        }

        @Override
        public String toString() {
            return scheme + "//" //
                    + ((userInfo == null) ? "" : userInfo + "@") //
                    + host //
                    + ((port == null) ? "" : ":" + port);
        }
    }

    @NonNull
    protected final UriTemplate scheme;

    public Path path(String path) {
        return new Path(this, path);
    }
}
