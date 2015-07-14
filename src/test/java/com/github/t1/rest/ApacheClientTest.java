package com.github.t1.rest;

import static org.junit.Assert.*;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.junit.*;

public class ApacheClientTest {
    @Test
    @Ignore("the config is currently only done on the client, not on the request")
    public void shouldConfigureTimeouts() {
        HttpRequestBase dummy = new HttpGet();
        @SuppressWarnings("unused")
        HttpRequest request = new HttpRequest(null, null, dummy, new Headers()) {
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
    @Test(expected = HttpTimeoutException.class)
    public void shouldTimeout() {
        new RestResource("http://httpbin.org/delay/10").get(String.class);
    }
}
