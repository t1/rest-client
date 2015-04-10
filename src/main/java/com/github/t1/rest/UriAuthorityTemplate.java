package com.github.t1.rest;

import java.util.regex.*;

import lombok.*;
import lombok.experimental.Builder;

import com.github.t1.rest.UriTemplate.NonPath;

public abstract class UriAuthorityTemplate extends NonPath {
    private UriAuthorityTemplate(NonAuthority previous) {
        super(previous);
    }

    // http://www.ietf.org/rfc/rfc2396.txt
    // we don't support ip addresses and other, probably esoteric cases
    private static final Pattern HOST_BASED_PATTERN = Pattern.compile("" //
            + "((?<userinfo>.*)@)?" //
            + "(?<host>([\\p{Alnum}.-]*?))" //
            + "(:(?<port>[^/]*))?" //
            + "(?<more>/.*)?");

    public static UriAuthorityTemplate authority(NonAuthority scheme, String authority) {
        Matcher matcher = HOST_BASED_PATTERN.matcher(authority);
        if (!matcher.matches())
            return new RegistryBasedAuthorityTemplate(scheme, authority);
        return builder(scheme, matcher);
    }

    public static NonFragment authorityAndMore(NonAuthority scheme, String authorityAndMore) {
        Matcher matcher = HOST_BASED_PATTERN.matcher(authorityAndMore);
        if (!matcher.matches())
            return new RegistryBasedAuthorityTemplate(scheme, authorityAndMore);
        return builder(scheme, matcher).pathAndMore(matcher.group("more"));
    }

    private static HostBasedAuthorityTemplate builder(NonAuthority scheme, Matcher matcher) {
        return HostBasedAuthorityTemplate.builder() //
                .scheme(scheme) //
                .userInfo(matcher.group("userinfo")) //
                .host(matcher.group("host")) //
                .port(matcher.group("port")) //
                .build();
    }

    public static class RegistryBasedAuthorityTemplate extends UriAuthorityTemplate {
        private final String registryName;

        public RegistryBasedAuthorityTemplate(NonAuthority scheme, String registryName) {
            super(scheme);
            this.registryName = registryName;
        }

        @Override
        public String toString() {
            return previous + "//" + registryName;
        }

        @Override
        public String get() {
            return registryName;
        }
    }

    @Getter
    @Builder
    public static class HostBasedAuthorityTemplate extends UriAuthorityTemplate {
        public static class HostBasedAuthorityTemplateBuilder {
            @Setter
            NonAuthority scheme;

            public UriPath absolutePath(String string) {
                return build().absolutePath(string);
            }

            public UriPath relativePath(String string) {
                return build().relativePath(string);
            }

            public NonFragment pathAndMore(String string) {
                return build().pathAndMore(string);
            }

            public NonFragment path(String string) {
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

        public HostBasedAuthorityTemplate(NonAuthority scheme, String userInfo, String host, String port) {
            super(scheme);
            this.userInfo = userInfo;
            this.host = host;
            this.port = port;
        }

        @Override
        public String toString() {
            return previous + "//" + get();
        }

        @Override
        public String get() {
            return ((userInfo == null) ? "" : userInfo + "@") //
                    + host //
                    + ((port == null) ? "" : ":" + port);
        }
    }
}
