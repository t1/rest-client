package com.github.t1.rest;

import lombok.Value;

import com.github.t1.rest.UriAuthorityTemplate.HostBasedAuthorityTemplate;
import com.github.t1.rest.UriAuthorityTemplate.HostBasedAuthorityTemplate.HostBasedAuthorityTemplateBuilder;

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

        public UriAuthorityTemplate authority(String authority) {
            return scheme().authority(authority);
        }

        public HostBasedAuthorityTemplateBuilder userInfo(String userInfo) {
            return scheme().userInfo(userInfo);
        }

        public HostBasedAuthorityTemplateBuilder host(String host) {
            return scheme().host(host);
        }

        public Path path(String path) {
            return scheme().path(path);
        }
    }

    @Value
    public static class Path {
        UriAuthorityTemplate authority;
        String path;

        public Query query(String key, String value) {
            return new Query(this, key, value);
        }

        @Override
        public String toString() {
            return authority + "/" + path;
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

    public HostBasedAuthorityTemplateBuilder userInfo(String userInfo) {
        return HostBasedAuthorityTemplate.builder().scheme(this).userInfo(userInfo);
    }

    public HostBasedAuthorityTemplateBuilder host(String host) {
        return userInfo(null).host(host);
    }

    public Path path(String path) {
        return authority(null).path(path);
    }

    public UriAuthorityTemplate authority(String authority) {
        return UriAuthorityTemplate.authority(this, authority);
    }

    @Override
    public String toString() {
        return scheme + ":";
    }
}
