package com.github.t1.rest;

import ch.qos.logback.classic.*;
import com.fasterxml.jackson.core.JsonParseException;
import io.dropwizard.testing.junit.DropwizardClientRule;
import lombok.Data;
import org.apache.http.client.config.RequestConfig;
import org.junit.*;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.net.UnknownHostException;

import static ch.qos.logback.classic.Level.*;
import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

public class ApacheHttpClientTest {
    /**
     * some of these tests are slow by design... too slow for regular unit tests. And they require an Internet
     * connection
     */
    private static final boolean EXECUTE_SLOW_TESTS = false;

    private static byte[] STREAM;

    @Data
    public static class Pojo {
        String value;
    }

    @Path("/")
    public static class MockService {
        private static final int MB = 1024 * 1024;

        @GET
        @Path("/bad-request")
        public Response badRequest() {
            return Response.status(BAD_REQUEST).build();
        }

        @GET
        @Path("/delay")
        public void delay() throws InterruptedException {
            Thread.sleep(10 * 1000);
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
        public byte[] stream() {
            if (STREAM == null)
                STREAM = new byte[12 * MB];
            return STREAM;
        }
    }

    @ClassRule
    public static final DropwizardClientRule service = new DropwizardClientRule(new MockService());

    @Before
    public void before() {
        setLogLevel("org.apache.http.wire", DEBUG);
        setLogLevel("com.github.t1.rest", DEBUG);
    }

    private void setLogLevel(String loggerName, Level level) {
        ((Logger) LoggerFactory.getLogger(loggerName)).setLevel(level);
    }

    @Rule
    public final ApacheConfigRule apacheRule = new ApacheConfigRule();

    private RestResource pojoResource() {
        return new RestResource(service.baseUri() + "/pojo");
    }

    @Test
    public void shouldConfigureTimeouts() {
        RequestConfig config = apacheRule.getRequestConfig();

        assertEquals("connection request timeout", 1, config.getConnectionRequestTimeout());
        assertEquals("connect timeout", 1000, config.getConnectTimeout());
        assertEquals("socket timeout", 5000, config.getSocketTimeout());
    }

    @Test(expected = HttpTimeoutException.class)
    public void shouldTimeout() {
        assumeTrue(EXECUTE_SLOW_TESTS); // TODO add per-request timeouts, so this can run normally
        new RestResource(service.baseUri() + "/delay").GET(String.class);
    }

    @Test
    public void shouldConfigureConnectionPool() {
        RequestConfig config = apacheRule.getRequestConfig();

        assertEquals("socket timeout", 5000, config.getSocketTimeout());
    }

    @Test
    public void shouldCloseConnectionWhenConnectionFails() {
        try {
            new RestResource("http://example.nowhere").GET_Response();
            fail("expected RuntimeException");
        } catch (RuntimeException e) {
            assertEquals(UnknownHostException.class, e.getCause().getClass());
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
            pojoResource().GET_Response().getBody(Pojo.class);
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
    public void shouldReadBigStream() {
        @SuppressWarnings("deprecation")
        byte[] buffer = new RestResource(service.baseUri() + "/stream") //
                .accept(byte[].class, APPLICATION_OCTET_STREAM_TYPE) //
                .GET();

        assertArrayEquals(STREAM, buffer);
    }
}
