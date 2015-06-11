package com.github.t1.rest;

import static org.junit.Assert.*;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.junit.Test;

public class ApacheConfigTest {
    @Test
    public void shouldConfigureTimeouts() {
        HttpRequestBase dummy = new HttpGet();
        @SuppressWarnings("unused")
        HttpRequest request = new HttpRequest(dummy, new Headers()) {};

        RequestConfig config = dummy.getConfig();

        assertEquals("connection request timeout", 1, config.getConnectionRequestTimeout());
        assertEquals("connect timeout", 1000, config.getConnectTimeout());
        assertEquals("socket timeout", 5000, config.getSocketTimeout());
    }
}
