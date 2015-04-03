package com.github.t1.rest;

import lombok.Value;

/** Immutable, typesafe, fluent, strictly appendable builder for URI templates. */
@Value(staticConstructor = "scheme")
public class UriTemplate {
    public enum Scheme {
        file,
        http,
        https;

        public UriTemplate scheme() {
            return UriTemplate.scheme(name());
        }

        public Port authority(String authority) {
            return scheme().authority(authority);
        }

        public UserInfo userInfo(String userInfo) {
            return scheme().userInfo(userInfo);
        }

        public Host host(String host) {
            return scheme().host(host);
        }

        public Path path(String path) {
            return scheme().path(path);
        }
    }

    @Value
    public static class UserInfo {
        UriTemplate scheme;
        String userInfo;

        public Host host(String string) {
            return new Host(this, string);
        }

        @Override
        public String toString() {
            return scheme + "//" + ((userInfo == null) ? "" : userInfo + "@");
        }
    }

    @Value
    public static class Host {
        UserInfo userInfo;
        String host;

        public Port port(String port) {
            return new Port(this, port);
        }

        public Path path(String path) {
            return new Path(port(null), path);
        }

        @Override
        public String toString() {
            return userInfo + host;
        }
    }

    @Value
    public static class Port {
        Host host;
        String port;

        public Path path(String path) {
            return new Path(this, path);
        }

        @Override
        public String toString() {
            return host + ((port == null) ? "" : ":" + port);
        }
    }

    @Value
    public static class Path {
        Port port;
        String path;

        public Query query(String key, String value) {
            return new Query(this, key, value);
        }

        @Override
        public String toString() {
            return port + "/" + path;
        }
    }

    @Value
    public static class Query {
        Path path;
        String key;
        String value;

        public Fragment fragment(String fragment) {
            return new Fragment(this, fragment);
        }

        @Override
        public String toString() {
            return path + "?" + key + "=" + value;
        }
    }

    @Value
    public static class Fragment {
        Query query;
        String fragment;

        @Override
        public String toString() {
            return query + "#" + fragment;
        }
    }

    String scheme;

    public UserInfo userInfo(String userInfo) {
        return new UserInfo(this, userInfo);
    }

    public Host host(String host) {
        return userInfo(null).host(host);
    }

    public Path path(String path) {
        return host(null).path(path);
    }

    public Port authority(String host) {
        return host(host).port(null);
    }

    @Override
    public String toString() {
        return scheme + ":";
    }
}
