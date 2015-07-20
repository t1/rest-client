package com.github.t1.rest;

import static ch.qos.logback.classic.Level.*;
import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import io.dropwizard.testing.junit.DropwizardClientRule;

import java.io.InputStream;
import java.lang.reflect.Field;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import lombok.Data;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.*;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.*;

import com.fasterxml.jackson.core.JsonParseException;

public class ApacheClientTest {
    /** some of these tests are slow by design... too slow for regular unit tests. And they require an Internet connection */
    private static final boolean EXECUTE_REMOTE_INTEGRATION_TESTS = false;

    @Data
    public static class Pojo {
        String value;
    }

    @Path("/")
    public static class MockService {
        @GET
        @Path("/bad-request")
        public Response badRequest() {
            return Response.status(BAD_REQUEST).build();
        }

        @GET
        @Path("/pojo")
        @Produces(APPLICATION_JSON)
        public String pojo() {
            return "invalid";
        }

        @GET
        @Path("/stream")
        @Produces(APPLICATION_OCTET_STREAM)
        public String stream() {
            return "invalid";
        }
    }

    @ClassRule
    public static final DropwizardClientRule service = new DropwizardClientRule(new MockService());

    @Before
    public void verifyNoOpenConnectionsBefore() {
        assertEquals(0, getTotalConnections());
    }

    @After
    public void verifyNoOpenConnectionsAfter() {
        assertEquals(0, getTotalConnections());
    }

    private int getTotalConnections() {
        return getInternalHttpClientField(PoolingHttpClientConnectionManager.class, "connManager").getTotalStats()
                .getLeased();
    }

    @Before
    public void before() {
        setLogLevel("org.apache.http.wire", DEBUG);
        setLogLevel("com.github.t1.rest", DEBUG);
    }

    private void setLogLevel(String loggerName, Level level) {
        ((Logger) LoggerFactory.getLogger(loggerName)).setLevel(level);
    }

    private RestResource pojoResource() {
        return new RestResource(service.baseUri() + "/pojo");
    }

    private RequestConfig getRequestConfig() {
        return getInternalHttpClientField(RequestConfig.class, "defaultConfig");
    }

    private <T> T getInternalHttpClientField(Class<T> type, String name) {
        Object internalHttpClient = getField(null, RestCallFactory.class.getName(), "CLIENT");
        Object result = getField(internalHttpClient, "org.apache.http.impl.client.InternalHttpClient", name);
        return type.cast(result);
    }

    private Object getField(Object object, String className, String fieldName) {
        try {
            Class<?> type = Class.forName(className);
            Field connManagerField = type.getDeclaredField(fieldName);
            connManagerField.setAccessible(true);
            return connManagerField.get(object);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void shouldConfigureTimeouts() {
        RequestConfig config = getRequestConfig();

        assertEquals("connection request timeout", 1, config.getConnectionRequestTimeout());
        assertEquals("connect timeout", 1000, config.getConnectTimeout());
        assertEquals("socket timeout", 5000, config.getSocketTimeout());
    }

    @Test(expected = HttpTimeoutException.class)
    public void shouldTimeout() {
        assumeTrue(EXECUTE_REMOTE_INTEGRATION_TESTS);
        new RestResource("http://httpbin.org/delay/10").GET(String.class);
    }

    @Test
    public void shouldConfigureConnectionPool() {
        RequestConfig config = getRequestConfig();

        assertEquals("socket timeout", 5000, config.getSocketTimeout());
    }

    @Test
    public void shouldCloseConnectionWhenConnectionFails() {
        try {
            new RestResource("http://example.nowhere").GET_Response();
            fail("expected RuntimeException");
        } catch (RuntimeException e) {
            assertEquals("can't execute GET http://example.nowhere HTTP/1.1", e.getMessage());
        }
    }

    @Test(expected = UnexpectedStatusException.class)
    public void shouldCloseConnectionWhenExpectingFails() {
        new RestResource(service.baseUri() + "/bad-request").GET(String.class);
    }

    @Test
    public void shouldCloseConnectionWhenConversionFails() {
        try {
            pojoResource().GET(Pojo.class);
            fail("expected RuntimException");
        } catch (RuntimeException e) {
            assertThat(e.getCause(), instanceOf(JsonParseException.class));
        }
    }

    @Test
    public void shouldCloseConnectionWhenAfterTheFactConversionFails() {
        try {
            pojoResource().GET_Response().get(Pojo.class);
            fail("expected RuntimException");
        } catch (RuntimeException e) {
            assertThat(e.getCause(), instanceOf(JsonParseException.class));
        }
    }

    @Test
    public void shouldCloseConnectionAfterConversionSucceeds() {
        String get = pojoResource().GET(String.class);

        assertEquals("invalid", get);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void shouldNotCloseConnectionWhenResultIsCloseable() throws Exception {
        assumeTrue(EXECUTE_REMOTE_INTEGRATION_TESTS);
        RestResource resource = new RestResource("http://httpbin.org/stream-bytes/" + 1024 * 1024);
        EntityRequest<InputStream> request = resource.accept(InputStream.class, APPLICATION_OCTET_STREAM_TYPE);
        try (InputStream stream = request.GET()) {
            assertEquals(1, getTotalConnections());
        }
    }
}
