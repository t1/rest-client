package com.github.t1.rest;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.cache.*;

import javax.annotation.concurrent.Immutable;
import java.lang.annotation.Annotation;
import java.net.URI;

/** A factory for all types of {@link RestCall}s. Useful for mocking. */
@Immutable
public class RestCallFactory {
    private static final int DEFAULT_MAX_CACHE_ENTRIES = 1000;
    private static final int DEFAULT_MAX_CACHE_OBJECT_SIZE = 8192;

    private static final CacheConfig CACHE_CONFIG = CacheConfig
            .custom()
            .setMaxCacheEntries(DEFAULT_MAX_CACHE_ENTRIES)
            .setMaxObjectSize(DEFAULT_MAX_CACHE_OBJECT_SIZE)
            .build();

    private static final int DEFAULT_CONNECTION_REQUEST_TIMEOUT = 1;
    private static final int DEFAULT_CONNECT_TIMEOUT = 1_000;
    private static final int DEFAULT_SOCKET_TIMEOUT = 5_000;

    private static final RequestConfig DEFAULT_CONFIG = RequestConfig
            .custom()
            .setConnectionRequestTimeout(DEFAULT_CONNECTION_REQUEST_TIMEOUT)
            .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
            .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT)
            .build();

    private static final CloseableHttpClient CLIENT = CachingHttpClients
            .custom()
            .setCacheConfig(CACHE_CONFIG)
            .setDefaultRequestConfig(DEFAULT_CONFIG)
            .build();

    public <T, M extends Annotation> EntityRestCall<T> createRestCall(Class<M> method, RestContext context, URI uri,
            Headers headers, ResponseConverter<T> converter) {
        return new EntityRestCall<>(context, method, uri, headers, CLIENT, converter);
    }
}
