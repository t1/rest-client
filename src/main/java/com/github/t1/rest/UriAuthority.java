package com.github.t1.rest;

import java.util.regex.*;

import lombok.*;
import lombok.experimental.*;

import com.github.t1.rest.UriTemplate.NonPath;

@ExtensionMethod(MethodExtensions.class)
public abstract class UriAuthority extends NonPath {
    private UriAuthority(NonAuthority previous) {
        super(previous);
    }

    // http://www.ietf.org/rfc/rfc2396.txt
    // we don't support ip addresses and other, probably esoteric cases
    private static final Pattern HOST_BASED_PATTERN = Pattern.compile("" //
            + "((?<userinfo>.*)@)?" //
            + "(?<host>([\\p{Alnum}.{}-]*?))" //
            + "(:(?<port>[^/]*))?" //
            + "(?<more>[/?].*)?");

    private static final Pattern REGISTRY_BASED_PATTERN = Pattern.compile("" //
            + "(?<name>(.*?))" //
            + "(?<more>[/?].*)?");

    public static UriAuthority authority(NonAuthority scheme, String authority) {
        Matcher matcher = HOST_BASED_PATTERN.matcher(authority);
        if (!matcher.matches())
            return new RegistryBasedAuthority(scheme, authority);
        return hostBasedAuthority(scheme, matcher);
    }

    public static NonFragment authorityAndMore(NonAuthority scheme, String authorityAndMore) {
        UriAuthority authority;
        Matcher matcher = HOST_BASED_PATTERN.matcher(authorityAndMore);
        if (!matcher.matches()) {
            matcher = REGISTRY_BASED_PATTERN.matcher(authorityAndMore);
            if (!matcher.matches())
                throw new IllegalArgumentException("unparseable authority; this should not be possible");
            authority = new RegistryBasedAuthority(scheme, matcher.group("name"));
        } else {
            authority = hostBasedAuthority(scheme, matcher);
        }
        return authority.pathAndMore(matcher.group("more"));
    }

    private static HostBasedAuthority hostBasedAuthority(NonAuthority scheme, Matcher matcher) {
        return HostBasedAuthority.builder() //
                .scheme(scheme) //
                .userInfo(matcher.group("userinfo")) //
                .host(matcher.group("host")) //
                .port(matcher.group("port")) //
                .build();
    }

    public static class RegistryBasedAuthority extends UriAuthority {
        private final String registryName;

        public RegistryBasedAuthority(NonAuthority scheme, String registryName) {
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

        @Override
        public RegistryBasedAuthority with(String name, Object value) {
            return new RegistryBasedAuthority((NonAuthority) previous.with(name, value), //
                    this.registryName.replaceVariable(name, value));
        }
    }

    @Getter
    @Builder
    public static class HostBasedAuthority extends UriAuthority {
        public static class HostBasedAuthorityBuilder {
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

            public HostBasedAuthority build() {
                return new HostBasedAuthority(scheme, userInfo, host, port);
            }

            @Override
            public String toString() {
                return build().toString();
            }
        }

        String userInfo;
        String host;
        String port;

        public HostBasedAuthority(NonAuthority scheme, String userInfo, String host, String port) {
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

        @Override
        public HostBasedAuthority with(String name, Object value) {
            return new HostBasedAuthority((NonAuthority) previous.with(name, value), //
                    this.userInfo.replaceVariable(name, value), //
                    this.host.replaceVariable(name, value), //
                    this.port.replaceVariable(name, value));
        }
    }
}
