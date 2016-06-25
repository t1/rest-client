package com.github.t1.rest;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.rules.ExternalResource;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class ApacheConfigRule extends ExternalResource {
    @Override
    protected void before() {
        assertEquals("connections before", 0, getTotalConnections());
    }

    @Override
    protected void after() {
        assertEquals("connections after", 0, getTotalConnections());
    }

    public int getTotalConnections() {
        return getInternalHttpClient(PoolingHttpClientConnectionManager.class, "connManager")
                .getTotalStats()
                .getLeased();
    }

    public RequestConfig getRequestConfig() {
        return getInternalHttpClient(RequestConfig.class, "defaultConfig");
    }

    static <T> T getInternalHttpClient(Class<T> type, String name) {
        Object internalHttpClient = getField(null, RestCallFactory.class.getName(), "CLIENT");
        Object result = getField(internalHttpClient, "org.apache.http.impl.client.InternalHttpClient", name);
        return type.cast(result);
    }

    private static Object getField(Object object, String className, String fieldName) {
        try {
            Class<?> type = Class.forName(className);
            Field field = type.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
