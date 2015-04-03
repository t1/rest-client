package com.github.t1.rest;

import java.util.regex.*;

import lombok.Setter;
import lombok.experimental.Builder;

public abstract class UriAuthorityTemplate extends UriTemplate {
    public UriAuthorityTemplate(UriScheme previous) {
        super(previous);
    }

    // http://www.ietf.org/rfc/rfc2396.txt
    // we don't support ip addresses and other, probably esoteric cases
    private static final Pattern HOST_BASED_PATTERN = Pattern.compile("" //
            + "((?<userinfo>.*)@)?" //
            + "(?<host>([\\p{Alnum}.-]*?))" //
            + "(:(?<port>.*))?");

    public static UriAuthorityTemplate authority(UriScheme scheme, String authority) {
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
        public NullAuthority(UriScheme scheme) {
            super(scheme);
        }

        @Override
        public String toString() {
            return previous.toString();
        }
    }

    public static class RegistryBasedAuthorityTemplate extends UriAuthorityTemplate {
        private final String registryName;

        public RegistryBasedAuthorityTemplate(UriScheme scheme, String registryName) {
            super(scheme);
            this.registryName = registryName;
        }

        @Override
        public String toString() {
            return previous + "//" + registryName;
        }
    }

    @Builder
    public static class HostBasedAuthorityTemplate extends UriAuthorityTemplate {
        public static class HostBasedAuthorityTemplateBuilder {
            @Setter
            UriScheme scheme;

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

        public HostBasedAuthorityTemplate(UriScheme scheme, String userInfo, String host, String port) {
            super(scheme);
            this.userInfo = userInfo;
            this.host = host;
            this.port = port;
        }

        @Override
        public String toString() {
            return previous + "//" //
                    + ((userInfo == null) ? "" : userInfo + "@") //
                    + host //
                    + ((port == null) ? "" : ":" + port);
        }
    }

    public Path path(String path) {
        return new AbsolutePath(this, path);
    }
}
