package com.github.t1.rest;

import com.github.t1.rest.fallback.YamlMessageBodyWriter;
import io.dropwizard.testing.junit.DropwizardClientRule;
import lombok.*;
import org.jglue.cdiunit.CdiRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;

import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;
import static lombok.AccessLevel.*;
import static org.junit.Assert.*;

@RunWith(CdiRunner.class)
public class HttpPutTest extends AbstractHttpMethodTest {
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

    @Path("/")
    public static class MockService {
        @PUT
        @Path("/text-plain")
        @Consumes(TEXT_PLAIN)
        public Response putTextPlain(String body) {
            return Response.ok().header("pong", "*" + body + "*").build();
        }
    }

    @ClassRule
    public static final DropwizardClientRule service =
            new DropwizardClientRule(new MockService(), new YamlMessageBodyWriter());

    @Inject
    RestContext rest;

    private RestResource base(String path) {
        return new RestResource(UriTemplate.fromString(service.baseUri() + "/" + path));
    }

    @Test
    @Ignore
    public void shouldPutAsDefaultJson() {
        RestResponse response = base("text-plain").PUT(new Pojo("s", 123));

        assertEquals("*hi*", response.expecting(OK).header("pong").value());
    }

    @Test
    @Ignore
    public void shouldPutAsExplicitTextPlain() {
        RestResponse response = base("text-plain").PUT(TEXT_PLAIN_TYPE, "hi");

        assertEquals("*hi*", response.expecting(OK).header("pong").value());
    }
}
