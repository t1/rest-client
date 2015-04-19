package com.github.t1.rest;

import static ch.qos.logback.classic.Level.*;
import static com.github.t1.rest.fallback.YamlMessageBodyReader.*;
import static javax.ws.rs.core.MediaType.*;
import static lombok.AccessLevel.*;
import static org.junit.Assert.*;
import io.dropwizard.testing.junit.DropwizardClientRule;

import java.io.InputStream;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.*;

import org.jglue.cdiunit.CdiRunner;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.*;

import com.github.t1.rest.fallback.ConverterTools;

@RunWith(CdiRunner.class)
public class RestTest {
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
        @Produces({ "application/vnd.com.github.t1.rest.resttest$bazvendortypepojo+json", APPLICATION_JSON })
        public BazVendorTypePojo bazpojo() {
            return new BazVendorTypePojo(789);
        }

        @GET
        @Path("/bongpojo")
        @Produces({ "application/vnd.com.github.t1.rest.resttest$bongvendortypepojo+json", APPLICATION_JSON })
        public BongVendorTypePojo bongpojo() {
            return new BongVendorTypePojo(true);
        }
    }

    private final DropwizardClientRule service = new DropwizardClientRule(new MockService(),
            new YamlMessageBodyWriter());

    // this rule must be on a method so this bean is valid for CDI-Unit which makes it application scoped,
    // i.e. it must not have any public fields
    @Rule
    public DropwizardClientRule service() {
        return service;
    }

    @Inject
    RestConfig rest = new RestConfig();

    private RestResource base() {
        return new RestResource(service.baseUri());
    }

    @Before
    public void before() {
        setLogLevel("org.apache.http.wire", DEBUG);
        setLogLevel("com.github.t1.rest", DEBUG);
    }

    private void setLogLevel(String loggerName, Level level) {
        ((Logger) LoggerFactory.getLogger(loggerName)).setLevel(level);
    }

    @SuppressWarnings("deprecation")
    private <T> EntityRequest<T> baseAccept(String path, Class<T> type, MediaType mediaType) {
        return base().path(path).accept(type, mediaType);
    }

    @SuppressWarnings("unused")
    @Test(expected = RuntimeException.class)
    public void shouldFailParsingUnsupportedScheme() {
        new RestResource("mailto:test@example.com");
    }

    @Test
    public void shouldGetPing() {
        String pong = base().path("ping").accept(String.class).get();

        assertEquals("pong", pong);
    }

    @Test
    public void shouldGetDirectPing() {
        String pong = base().path("ping").get(String.class);

        assertEquals("pong", pong);
    }

    @Test
    public void shouldGetPingResponse() {
        EntityResponse<String> response = base().path("ping").accept(String.class).getResponse();

        assertEquals("pong", response.get());
        assertEquals(TEXT_PLAIN_TYPE, response.contentType());
        assertEquals((Integer) 4, response.contentLength());
    }

    @Test
    public void shouldGetDirectPingResponse() {
        EntityResponse<String> response = base().path("ping").getResponse(String.class);

        assertEquals("pong", response.get());
    }

    @Test
    public void shouldGetPingAsStream() throws Exception {
        try (InputStream pong = baseAccept("ping", InputStream.class, WILDCARD_TYPE).get()) {
            assertEquals("pong", ConverterTools.readString(pong, null));
        }
    }

    @Test
    public void shouldAcceptType() {
        RestResource base = base();
        EntityRequest<Pojo> rest = base.accept(Pojo.class);

        assertEquals(Pojo.class, rest.converter().acceptedType());
    }

    @Test
    public void shouldGetJsonPojo() {
        JsonPojo pojo = base().path("jsonpojo").get(JsonPojo.class);

        assertEquals("json", pojo.getString());
    }

    @Test
    public void shouldGetPojo() {
        Pojo pojo = base().path("pojo").accept(Pojo.class).get();

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
        EntityRequest<Pojo> request = baseAccept("pojo", Pojo.class, APPLICATION_JSON_TYPE);

        Pojo pojo = request.get();

        assertEquals("s", pojo.getString());
        assertEquals(123, pojo.getI());
    }

    @Test
    public void shouldGetPojoAsXml() {
        EntityRequest<Pojo> request = baseAccept("pojo", Pojo.class, APPLICATION_XML_TYPE);

        Pojo pojo = request.get();

        assertEquals("s", pojo.getString());
        assertEquals(123, pojo.getI());
    }

    @Test
    public void shouldGetPojoAsYaml() {
        EntityRequest<Pojo> request = baseAccept("pojo", Pojo.class, APPLICATION_YAML_TYPE);

        Pojo pojo = request.get();

        assertEquals("s", pojo.getString());
        assertEquals(123, pojo.getI());
    }

    @Test
    public void shouldGetPojoAsFooVendorType() {
        MediaType vendorType = MediaType.valueOf("application/vnd.foo+json");
        EntityRequest<FooVendorTypePojo> request = baseAccept("foopojo", FooVendorTypePojo.class, vendorType);

        FooVendorTypePojo pojo = request.get();

        assertEquals("f", pojo.getString());
        assertEquals(345, pojo.getI());
    }

    @Test
    public void shouldGetPojoAsDefaultVendorTypeJson() {
        MediaType vendorType = MediaType.valueOf("application/vnd." + DefaultVendorTypePojo.class.getName() + "+json");
        EntityRequest<DefaultVendorTypePojo> request = baseAccept("vpojo", DefaultVendorTypePojo.class, vendorType);

        DefaultVendorTypePojo pojo = request.get();

        assertEquals("v", pojo.getString());
        assertEquals(456, pojo.getI());
    }

    @Test
    public void shouldGetPojoAsDefaultVendorTypeXml() {
        MediaType vendorType = MediaType.valueOf("application/vnd." + DefaultVendorTypePojo.class.getName() + "+xml");
        EntityRequest<DefaultVendorTypePojo> request = baseAccept("vpojo", DefaultVendorTypePojo.class, vendorType);

        DefaultVendorTypePojo pojo = request.get();

        assertEquals("v", pojo.getString());
        assertEquals(456, pojo.getI());
    }

    @Test
    public void shouldGetPojoAsDefaultVendorTypeYaml() {
        MediaType vendorType = MediaType.valueOf("application/vnd." + DefaultVendorTypePojo.class.getName() + "+yaml");
        EntityRequest<DefaultVendorTypePojo> request = baseAccept("vpojo", DefaultVendorTypePojo.class, vendorType);

        DefaultVendorTypePojo pojo = request.get();

        assertEquals("v", pojo.getString());
        assertEquals(456, pojo.getI());
    }

    @Test
    public void shouldGetUntypedPojo() {
        Pojo pojo = base().path("pojo").getResponse().get(Pojo.class);

        assertEquals("s", pojo.getString());
        assertEquals(123, pojo.getI());
    }

    @Test
    public void shouldGetTwoVendorTypesWithCommonBaseClass() {
        EntityRequest<?> request = base().path("foopojo").accept(FooVendorTypePojo.class, DefaultVendorTypePojo.class);
        FooVendorTypePojo pojo = request.getResponse().get(FooVendorTypePojo.class);

        assertEquals("f", pojo.getString());
        assertEquals(345, pojo.getI());
    }

    @Test
    public void shouldGetThreeVendorTypesWithoutCommonBaseClass() {
        EntityRequest<?> request = base().path("{path}") //
                .accept(BarVendorTypePojo.class, BazVendorTypePojo.class, BongVendorTypePojo.class);

        EntityResponse<?> barResponse = request.with("path", "barpojo").getResponse();
        assertEquals("application/vnd.com.github.t1.rest.resttest$barvendortypepojo+json", barResponse.contentType()
                .toString());
        BarVendorTypePojo bar = barResponse.get(BarVendorTypePojo.class);
        assertEquals("bar", bar.getString());

        EntityResponse<?> bazResponse = request.with("path", "bazpojo").getResponse();
        assertEquals("application/vnd.com.github.t1.rest.resttest$bazvendortypepojo+json", bazResponse.contentType()
                .toString());
        BazVendorTypePojo baz = bazResponse.get(BazVendorTypePojo.class);
        assertEquals(789, baz.getInteger());

        EntityResponse<?> bongResponse = request.with("path", "bongpojo").getResponse();
        assertEquals("application/vnd.com.github.t1.rest.resttest$bongvendortypepojo+json", bongResponse.contentType()
                .toString());
        BongVendorTypePojo bong = bongResponse.get(BongVendorTypePojo.class);
        assertEquals(true, bong.getBool());
    }

    // TODO check all types that are not convertible (according to spec) see ConverterTools
    // TODO use MessageBodyReaders from container, but they don't seem to be CDI beans; how can we detect them?
    // TODO limit readers to ConstrainedTo annotation
}
