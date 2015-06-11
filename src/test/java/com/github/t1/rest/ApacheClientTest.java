package com.github.t1.rest;

import static org.junit.Assert.*;

import java.net.SocketTimeoutException;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.junit.*;

public class ApacheClientTest {
    @Test
    public void shouldConfigureTimeouts() {
        HttpRequestBase dummy = new HttpGet();
        @SuppressWarnings("unused")
        HttpRequest request = new HttpRequest(dummy, new Headers()) {
            @Override
            protected RestResponse convert(CloseableHttpResponse apacheResponse) {
                return null;
            }
        };

        RequestConfig config = dummy.getConfig();

        assertEquals("connection request timeout", 1, config.getConnectionRequestTimeout());
        assertEquals("connect timeout", 1000, config.getConnectTimeout());
        assertEquals("socket timeout", 5000, config.getSocketTimeout());
    }

    @Ignore("this is a slow integration test")
    @Test(expected = SocketTimeoutException.class)
    public void shouldTimeout() {
        new RestResource("http://httpbin.org/delay/10").get(String.class);
    }
}
