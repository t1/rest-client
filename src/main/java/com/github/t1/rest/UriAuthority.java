package com.github.t1.rest;

import static com.github.t1.rest.MethodExtensions.*;

import java.util.regex.*;

import com.github.t1.rest.UriTemplate.NonPath;

import lombok.*;

public abstract class UriAuthority extends NonPath {
    private UriAuthority(NonAuthority previous) {
        super(previous);
    }

    // http://www.ietf.org/rfc/rfc2396.txt ... some, probably esoteric cases are not supported
    private static final Pattern HOST_BASED_PATTERN = Pattern.compile("" //
            + "((?<userinfo>(([\\p{Alnum};:&=+$,%]+|\\{.+\\})).*)@)?" //
            + "(?<host>(([\\p{Alnum}.-]+|[0-9:.]+|\\{.+\\})))" //
            + "(:(?<port>([0-9]+|\\{.+\\})))?" //
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

    protected static NonFragment authorityAndMore(NonAuthority scheme, String authorityAndMore) {
        UriAuthority authority;
        Matcher matcher = HOST_BASED_PATTERN.matcher(authorityAndMore);
        if (matcher.matches()) {
            authority = hostBasedAuthority(scheme, matcher);
        } else {
            matcher = REGISTRY_BASED_PATTERN.matcher(authorityAndMore);
            if (!matcher.matches())
                throw new IllegalArgumentException("unparseable authority; this should not be possible");
            authority = new RegistryBasedAuthority(scheme, matcher.group("name"));
        }
        return authority.pathAndMore(matcher.group("more"));
    }

    private static HostBasedAuthority hostBasedAuthority(NonAuthority scheme, Matcher matcher) {
        return new HostBasedAuthority(scheme, matcher.group("userinfo")). //
                host(matcher.group("host")). //
                port(matcher.group("port")) //
                ;
    }

    @Getter
    @EqualsAndHashCode(callSuper = true)
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
                    replaceVariable(this.registryName, name, value));
        }
    }

    @Getter
    @EqualsAndHashCode(callSuper = true)
    public static class HostBasedAuthority extends UriAuthority {
        private final String userInfo;
        private final String host;
        private final String port;

        public HostBasedAuthority(NonAuthority scheme, String userInfo) {
            this(scheme, userInfo, null, null);
        }

        private HostBasedAuthority(NonAuthority scheme, String userInfo, String host, String port) {
            super(scheme);
            this.userInfo = userInfo;
            this.host = host;
            this.port = port;
        }

        public HostBasedAuthority host(String host) {
            assert host == null : "shouldn't change host";
            assert port == null : "shouldn't change port";
            return new HostBasedAuthority((NonAuthority) previous, userInfo, host, null);
        }

        public HostBasedAuthority port(String port) {
            assert port == null : "shouldn't change port";
            return new HostBasedAuthority((NonAuthority) previous, userInfo, host, port);
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
                    replaceVariable(this.userInfo, name, value)) //
                            .host(replaceVariable(this.host, name, value)) //
                            .port(replaceVariable(this.port, name, value));
        }
    }
}
