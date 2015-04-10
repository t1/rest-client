package com.github.t1.rest;

import static ch.qos.logback.classic.Level.*;
import static com.github.t1.rest.fallback.YamlMessageBodyReader.*;
import static javax.ws.rs.core.MediaType.*;
import static lombok.AccessLevel.*;
import static org.junit.Assert.*;
import io.dropwizard.testing.junit.DropwizardClientRule;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.*;

import org.jglue.cdiunit.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.*;

import com.github.t1.rest.fallback.*;

@RunWith(CdiRunner.class)
@ActivatedAlternatives({ JsonMessageBodyReader.class, XmlMessageBodyReader.class, YamlMessageBodyReader.class,
        StringMessageBodyReader.class })
public class RestTest {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor(access = PRIVATE)
    @XmlRootElement
    public static class Pojo {
        private String string;
        private int i;
    }

    @VendorType("foo")
    @XmlRootElement
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
    public static class JsonPojo {
        private String string;
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

    private Rest<Object> base() {
        return rest.uri(service.baseUri());
    }

    private void setLogLevel(String loggerName, Level level) {
        ((Logger) LoggerFactory.getLogger(loggerName)).setLevel(level);
    }

    private Rest<Pojo> pojo() {
        return base().path("pojo").accept(Pojo.class);
    }

    @Before
    public void before() {
        setLogLevel("org.apache.http.wire", DEBUG);
        setLogLevel("com.github.t1.rest", DEBUG);
    }

    @Test(expected = RuntimeException.class)
    public void shouldFailParsingUnsupportedScheme() {
        rest.uri("mailto:test@example.com");
    }

    @Test
    public void shouldGetPing() {
        String pong = base().path("ping").accept(String.class).get();

        assertEquals("pong", pong);
    }

    @Test
    public void shouldAcceptType() {
        Rest<Object> base = base();
        Rest<Pojo> rest = base.accept(Pojo.class);

        assertNull(base.converter());
        assertEquals(Pojo.class, rest.converter().acceptedType());

        try {
            rest.accept(String.class);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            // fine
        }
    }

    @Test
    public void shouldGetJsonPojo() {
        JsonPojo pojo = base().path("jsonpojo").get(JsonPojo.class);

        assertEquals("json", pojo.getString());
    }

    @Test
    public void shouldLimitTypeToSomethingConvertibleTo() {
        base().path("jsonpojo").accept(JsonPojo.class).as(APPLICATION_JSON_TYPE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToLimitTypeToSomethingNotConvertibleTo() {
        base().path("jsonpojo").accept(JsonPojo.class).as(APPLICATION_XML_TYPE);
    }

    @Test
    public void shouldGetPojoAsAnything() {
        Pojo pojo = pojo().get();

        assertEquals("s", pojo.getString());
        assertEquals(123, pojo.getI());
    }

    @Test
    public void shouldGetPojoAsJson() {
        Pojo pojo = pojo().as(APPLICATION_JSON_TYPE).get();

        assertEquals("s", pojo.getString());
        assertEquals(123, pojo.getI());
    }

    @Test
    public void shouldGetPojoAsXml() {
        Pojo pojo = pojo().as(APPLICATION_XML).get();

        assertEquals("s", pojo.getString());
        assertEquals(123, pojo.getI());
    }

    @Test
    public void shouldGetPojoAsYaml() {
        Pojo pojo = pojo().as(APPLICATION_YAML).get();

        assertEquals("s", pojo.getString());
        assertEquals(123, pojo.getI());
    }

    @Test
    public void shouldGetPojoAsFooVendorType() {
        FooVendorTypePojo pojo =
                base().path("foopojo").accept(FooVendorTypePojo.class).as("application/vnd.foo+json").get();

        assertEquals("f", pojo.getString());
        assertEquals(345, pojo.getI());
    }

    @Test
    public void shouldGetPojoAsDefaultVendorTypeJson() {
        DefaultVendorTypePojo pojo =
                base().path("vpojo").accept(DefaultVendorTypePojo.class)
                        .as("application/vnd." + DefaultVendorTypePojo.class.getName() + "+json").get();

        assertEquals("v", pojo.getString());
        assertEquals(456, pojo.getI());
    }

    @Test
    public void shouldGetPojoAsDefaultVendorTypeXml() {
        DefaultVendorTypePojo pojo =
                base().path("vpojo").accept(DefaultVendorTypePojo.class)
                        .as("application/vnd." + DefaultVendorTypePojo.class.getName() + "+xml").get();

        assertEquals("v", pojo.getString());
        assertEquals(456, pojo.getI());
    }

    @Test
    public void shouldGetPojoAsDefaultVendorTypeYaml() {
        DefaultVendorTypePojo pojo =
                base().path("vpojo").accept(DefaultVendorTypePojo.class)
                        .as("application/vnd." + DefaultVendorTypePojo.class.getName() + "+yaml").get();

        assertEquals("v", pojo.getString());
        assertEquals(456, pojo.getI());
    }
}
