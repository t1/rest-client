package com.github.t1.deployer.app;

public class UriAuthority {
    public static UriAuthority fromString(String string) {
        HostBasedUriAuthority authority = new HostBasedUriAuthority();
        authority.host = string;
        return authority;
    }

    public static class HostBasedUriAuthority extends UriAuthority {
        private String userInfo;
        private String host;
        private String port;

        @Override
        public String toString() {
            StringBuilder out = new StringBuilder();
            out.append(host);
            if (port != null)
                out.append(":").append(port);
            return out.toString();
        }
    }

    public static class RegistryBasedUriAuthority extends UriAuthority {
        private String name;
    }
}
