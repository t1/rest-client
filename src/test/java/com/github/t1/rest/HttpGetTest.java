package com.github.t1.rest;

import com.github.t1.rest.fallback.ConverterTools;
import io.dropwizard.testing.junit.DropwizardClientRule;
import lombok.*;
import org.jglue.cdiunit.CdiRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.StatusType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.InputStream;

import static com.github.t1.rest.fallback.InputStreamMessageBodyReader.*;
import static com.github.t1.rest.fallback.YamlMessageBodyReader.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;
import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;
import static javax.ws.rs.core.Response.Status.Family.*;
import static lombok.AccessLevel.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

@RunWith(CdiRunner.class)
public class HttpGetTest extends AbstractHttpMethodTest {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor(access = PRIVATE)
    @XmlRootElement
    public static class Pojo {
        private String string;
        private int i;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor(access = PRIVATE)
    public static class JsonPojo {
        private String string;
    }

    @VendorType("foo")
    @NoArgsConstructor(access = PRIVATE)
    public static class FooVendorTypePojo extends Pojo {
        public FooVendorTypePojo(String string, int i) {
            super(string, i);
        }
    }

    @VendorType
    @XmlRootElement
    @NoArgsConstructor(access = PRIVATE)
    public static class DefaultVendorTypePojo extends Pojo {
        public DefaultVendorTypePojo(String string, int i) {
            super(string, i);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor(access = PRIVATE)
    @VendorType
    public static class BarVendorTypePojo {
        String string;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor(access = PRIVATE)
    @VendorType
    public static class BazVendorTypePojo {
        int integer;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor(access = PRIVATE)
    @VendorType
    public static class BongVendorTypePojo {
        Boolean bool;
    }

    @Path("/")
    public static class MockService {
        @GET
        @Path("/ping")
        @Produces(TEXT_PLAIN)
        public String ping() {
            return "pong";
        }

        @GET
        @Path("/java-archive")
        @Produces(APPLICATION_JAVA_ARCHIVE)
        public String javaArchive() {
            return "this-is-a-jar";
        }

        @GET
        @Path("/pojo")
        public Pojo pojo() {
            return new Pojo("s", 123);
        }

        @GET
        @Path("/jsonpojo")
        public JsonPojo jsonpojo() {
            return new JsonPojo("json");
        }

        @GET
        @Path("/foopojo")
        public FooVendorTypePojo foopojo() {
            return new FooVendorTypePojo("f", 345);
        }

        @GET
        @Path("/vpojo")
        public DefaultVendorTypePojo vpojo() {
            return new DefaultVendorTypePojo("v", 456);
        }

        @GET
        @Path("/barpojo")
        public BarVendorTypePojo barpojo() {
            return new BarVendorTypePojo("bar");
        }

        @GET
        @Path("/bazpojo")
        @Produces({ "application/vnd.com.github.t1.rest.httpgettest$bazvendortypepojo+json", APPLICATION_JSON })
        public BazVendorTypePojo bazpojo() {
            return new BazVendorTypePojo(789);
        }

        @GET
        @Path("/bongpojo")
        @Produces({ "application/vnd.com.github.t1.rest.httpgettest$bongvendortypepojo+json", APPLICATION_JSON })
        public BongVendorTypePojo bongpojo() {
            return new BongVendorTypePojo(true);
        }

        @GET
        @Path("/authorized-pojo")
        public Pojo authorizedPojo(@HeaderParam("Authorization") String auth) {
            if (!"Basic dXNlcjpwYXNz".equals(auth))
                throw new WebApplicationException(UNAUTHORIZED);
            return new Pojo("authorized", 987);
        }

        @GET
        @Path("/no-content")
        public Response noContent() {
            return Response.status(NO_CONTENT).build();
        }

        @GET
        @Path("/zombie-apocalypse")
        public Response zombieAcopalypse() {
            return Response.status(793).build();
        }
    }

    @ClassRule
    public static final DropwizardClientRule service =
            new DropwizardClientRule(new MockService(), new YamlMessageBodyWriter());

    @Rule
    public ApacheConfigRule apacheConfig() {
        return new ApacheConfigRule();
    }

    @javax.enterprise.inject.Produces
    RestResourceRegistry testResource() {
        return new StaticRestResourceRegistry("test", new RestResource(service.baseUri()));
    }

    @javax.enterprise.inject.Produces
    RestResourceRegistry otherResource() {
        return new StaticRestResourceRegistry("other", new RestResource("http://twitter.com"));
    }


    private void givenCredentials() {
        rest = rest.register(base().uri().toUri(), new Credentials("user", "pass"));
    }

    private RestResource base(String... path) {
        return rest.resource("test", path);
    }

    private RestResource base() {
        return rest.resource("test");
    }

    @SuppressWarnings("deprecation")
    private <T> RestRequest<T> baseAccept(String path, Class<T> type, MediaType mediaType) {
        return base(path).accept(type, mediaType);
    }

    @Test(expected = RuntimeException.class)
    public void shouldFailParsingUnsupportedScheme() {
        new RestResource("mailto:test@example.com");
    }

    @Test
    public void shouldGetPing() {
        String pong = base("ping").accept(String.class).GET();

        assertEquals("pong", pong);
    }

    @Test
    public void shouldGetDirectPing() {
        String pong = base("ping").GET(String.class);

        assertEquals("pong", pong);
    }

    @Test
    public void shouldGetPingResponse() {
        EntityResponse<String> response = base("ping").accept(String.class).GET_Response();

        assertEquals("pong", response.getBody());
        assertEquals("4", response.header("Content-Length").value());
        assertEquals(TEXT_PLAIN_TYPE, response.contentType());
        assertEquals((Integer) 4, response.contentLength());
    }

    @Test
    public void shouldGetDirectPingResponse() {
        EntityResponse<String> response = base("ping").GET_Response(String.class);

        assertEquals("pong", response.getBody());
    }

    @Test
    public void shouldGetPingAsStream() throws Exception {
        try (InputStream pong = baseAccept("ping", InputStream.class, WILDCARD_TYPE).GET()) {
            assertEquals("pong", ConverterTools.readString(pong, null));
        }
    }

    @Test
    public void shouldGetJavaArchiveStream() throws Exception {
        try (InputStream pong = baseAccept("java-archive", InputStream.class, APPLICATION_JAVA_ARCHIVE_TYPE).GET()) {
            assertEquals("this-is-a-jar", ConverterTools.readString(pong, null));
        }
    }

    @Test
    public void shouldAcceptType() {
        RestRequest<Pojo> request = base().accept(Pojo.class);

        assertEquals(asList(APPLICATION_JSON_TYPE, APPLICATION_XML_TYPE, APPLICATION_YAML_TYPE),
                request.headers().accept());
    }

    @Test
    public void shouldGetJsonPojo() {
        JsonPojo pojo = base("jsonpojo").GET(JsonPojo.class);

        assertEquals("json", pojo.getString());
    }

    @Test
    public void shouldGetPojo() {
        Pojo pojo = base("pojo").accept(Pojo.class).GET();

        assertEquals("s", pojo.getString());
        assertEquals(123, pojo.getI());
    }

    @Test
    public void shouldGetPojoWithPathVariable() {
        Pojo pojo = base("{path}").accept(Pojo.class).with("path", "pojo").GET();

        assertEquals("s", pojo.getString());
        assertEquals(123, pojo.getI());
    }

    @Test
    public void shouldLimitTypeToSomethingConvertibleTo() {
        baseAccept("jsonpojo", JsonPojo.class, APPLICATION_JSON_TYPE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToLimitTypeToSomethingNotConvertibleTo() {
        baseAccept("jsonpojo", JsonPojo.class, TEXT_HTML_TYPE);
    }

    @Test
    public void shouldGetPojoAsJson() {
        RestRequest<Pojo> request = baseAccept("pojo", Pojo.class, APPLICATION_JSON_TYPE);

        Pojo pojo = request.GET();

        assertEquals("s", pojo.getString());
        assertEquals(123, pojo.getI());
    }

    @Test
    public void shouldGetPojoAsXml() {
        RestRequest<Pojo> request = baseAccept("pojo", Pojo.class, APPLICATION_XML_TYPE);

        Pojo pojo = request.GET();

        assertEquals("s", pojo.getString());
        assertEquals(123, pojo.getI());
    }

    @Test
    public void shouldGetPojoAsYaml() {
        RestRequest<Pojo> request = baseAccept("pojo", Pojo.class, APPLICATION_YAML_TYPE);

        Pojo pojo = request.GET();

        assertEquals("s", pojo.getString());
        assertEquals(123, pojo.getI());
    }

    @Test
    public void shouldGetPojoAsFooVendorType() {
        MediaType vendorType = MediaType.valueOf("application/vnd.foo+json");
        RestRequest<FooVendorTypePojo> request = baseAccept("foopojo", FooVendorTypePojo.class, vendorType);

        FooVendorTypePojo pojo = request.GET();

        assertEquals("f", pojo.getString());
        assertEquals(345, pojo.getI());
    }

    @Test
    public void shouldGetPojoAsDefaultVendorTypeJson() {
        MediaType vendorType = MediaType.valueOf("application/vnd." + DefaultVendorTypePojo.class.getName() + "+json");
        RestRequest<DefaultVendorTypePojo> request = baseAccept("vpojo", DefaultVendorTypePojo.class, vendorType);

        DefaultVendorTypePojo pojo = request.GET();

        assertEquals("v", pojo.getString());
        assertEquals(456, pojo.getI());
    }

    @Test
    public void shouldGetPojoAsDefaultVendorTypeXml() {
        MediaType vendorType = MediaType.valueOf("application/vnd." + DefaultVendorTypePojo.class.getName() + "+xml");
        RestRequest<DefaultVendorTypePojo> request = baseAccept("vpojo", DefaultVendorTypePojo.class, vendorType);

        DefaultVendorTypePojo pojo = request.GET();

        assertEquals("v", pojo.getString());
        assertEquals(456, pojo.getI());
    }

    @Test
    public void shouldGetPojoAsDefaultVendorTypeYaml() {
        MediaType vendorType = MediaType.valueOf("application/vnd." + DefaultVendorTypePojo.class.getName() + "+yaml");
        RestRequest<DefaultVendorTypePojo> request = baseAccept("vpojo", DefaultVendorTypePojo.class, vendorType);

        DefaultVendorTypePojo pojo = request.GET();

        assertEquals("v", pojo.getString());
        assertEquals(456, pojo.getI());
    }

    @Test
    public void shouldGetUntypedPojo() {
        Pojo pojo = base("pojo").GET_Response().getBody(Pojo.class);

        assertEquals("s", pojo.getString());
        assertEquals(123, pojo.getI());
    }

    @Test
    public void shouldGetTwoVendorTypesWithCommonBaseClass() {
        RestRequest<?> request = base("foopojo").accept(FooVendorTypePojo.class, DefaultVendorTypePojo.class);
        FooVendorTypePojo pojo = request.GET_Response().getBody(FooVendorTypePojo.class);

        assertEquals("f", pojo.getString());
        assertEquals(345, pojo.getI());
    }

    @Test
    public void shouldGetThreeVendorTypesWithoutCommonBaseClass() {
        RestRequest<?> request = base("{path}") //
                .accept(BarVendorTypePojo.class, BazVendorTypePojo.class, BongVendorTypePojo.class);

        EntityResponse<?> barResponse = request.with("path", "barpojo").GET_Response();
        assertEquals("application/vnd.com.github.t1.rest.httpgettest$barvendortypepojo+json",
                barResponse.contentType().toString());
        BarVendorTypePojo bar = barResponse.getBody(BarVendorTypePojo.class);
        assertEquals("bar", bar.getString());

        EntityResponse<?> bazResponse = request.with("path", "bazpojo").GET_Response();
        assertEquals("application/vnd.com.github.t1.rest.httpgettest$bazvendortypepojo+json",
                bazResponse.contentType().toString());
        BazVendorTypePojo baz = bazResponse.getBody(BazVendorTypePojo.class);
        assertEquals(789, baz.getInteger());

        EntityResponse<?> bongResponse = request.with("path", "bongpojo").GET_Response();
        assertEquals("application/vnd.com.github.t1.rest.httpgettest$bongvendortypepojo+json",
                bongResponse.contentType().toString());
        BongVendorTypePojo bong = bongResponse.getBody(BongVendorTypePojo.class);
        assertEquals(true, bong.getBool());
    }

    @Test
    public void shouldAuthorize() {
        Pojo pojo = base("authorized-pojo").basicAuth(new Credentials("user", "pass")).GET(Pojo.class);

        assertEquals("authorized", pojo.getString());
        assertEquals(987, pojo.getI());
    }

    @Test
    public void shouldAuthorizeFromRegistry() {
        givenCredentials();

        Pojo pojo = base("authorized-pojo").GET(Pojo.class);

        assertEquals("authorized", pojo.getString());
        assertEquals(987, pojo.getI());
    }

    @Test
    public void shouldAuthorizeAfterWith() {
        givenCredentials();

        Pojo pojo = base("{path}").with("path", "authorized-pojo").GET(Pojo.class);

        assertEquals("authorized", pojo.getString());
        assertEquals(987, pojo.getI());
    }

    @Test
    public void shouldFailToAuthorize() {
        EntityResponse<String> response = base("authorized-pojo").GET_Response(String.class);

        assertEquals(UNAUTHORIZED, response.status());
        assertNotNull(response.getBody());
    }

    @Test
    public void shouldDeriveTwoResorucesWithHeaders() {
        RestRequest<Pojo> base = base("pojo").accept(Pojo.class);
        RestRequest<Pojo> bar = base.header("foo", "bar");
        RestRequest<Pojo> baz = base.header("foo", "{foobar}").with("foobar", "baz");

        assertEquals(null, base.headers().firstValue("foo"));
        assertEquals("bar", bar.headers().firstValue("foo"));
        assertEquals("baz", baz.headers().firstValue("foo"));
        assertNotEquals(bar, baz);

        assertEquals(bar, base.header("foo", "bar"));
        assertEquals(base().authority(), base.authority());
    }

    @Test
    public void shouldExpectOk() {
        try {
            base("no-content").GET(String.class);
            fail("expected UnexpectedStatusException");
        } catch (UnexpectedStatusException e) {
            assertEquals(NO_CONTENT, e.actual());
            assertEquals(singletonList(OK), e.expected());
            assertThat(e.getMessage(), containsString("expected status 200 OK but got 204 No Content"));
        }
    }

    @Test
    public void shouldCheckExpectedStatus() {
        EntityResponse<String> response = base("ping").accept(String.class).GET_Response();

        response.expecting(NO_CONTENT, OK);
    }

    @Test
    public void shouldFailToCheckUnexpectedStatus() {
        try {
            EntityResponse<String> response = base("ping").accept(String.class).GET_Response();

            response.expecting(BAD_REQUEST, NOT_ACCEPTABLE);
            fail("expected UnexpectedStatusException");
        } catch (UnexpectedStatusException e) {
            assertEquals(OK, e.actual());
            assertEquals(asList(BAD_REQUEST, NOT_ACCEPTABLE), e.expected());
        }
    }

    @Test
    public void shouldHandleUnknownResponseCode() {
        StatusType status = base("zombie-apocalypse").GET_Response().status();

        assertEquals(793, status.getStatusCode());
        assertEquals("Unknown", status.getReasonPhrase());
        assertEquals(OTHER, status.getFamily());
    }

    // TODO check all types that are not convertible (according to spec) see ConverterTools
    // TODO safely encode all uri parts: template replacements as well as fixed strings
    // TODO safely endode all headers: template replacements as well as fixed strings
    // TODO subtype mapping with fq class name or annotations
    // TODO expecting via Response object
    // TODO full mappings (via joda-convert:struct)
    // TODO writers for PUT, POST, PATCH
    // TODO limit MessageBodyReaders to ConstrainedTo annotation
}
