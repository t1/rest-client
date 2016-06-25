package com.github.t1.rest;

import io.dropwizard.testing.junit.DropwizardClientRule;
import lombok.*;
import org.jglue.cdiunit.CdiRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import static lombok.AccessLevel.*;
import static org.junit.Assert.*;

@RunWith(CdiRunner.class)
public class HttpPostTest extends AbstractHttpMethodTest {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor(access = PRIVATE)
    @XmlRootElement
    public static class Pojo {
        private String string;
        private int i;
    }

    @Path("/")
    public static class MockService {
        @POST
        @Path("/pojo")
        public Pojo pojo() {
            return new Pojo("s", 123);
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

    @Inject
    RestContext rest;

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
    public void shouldPostPojo() {
        Pojo pojo = base("pojo").accept(Pojo.class).POST();

        assertEquals("s", pojo.getString());
        assertEquals(123, pojo.getI());
    }

    @Test
    public void shouldPostUntypedPojo() {
        Pojo pojo = base("pojo").POST_Response().getBody(Pojo.class);

        assertEquals("s", pojo.getString());
        assertEquals(123, pojo.getI());
    }
}
