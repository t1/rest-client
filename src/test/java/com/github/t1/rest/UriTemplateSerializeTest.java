package com.github.t1.rest;

import static com.github.t1.rest.UriTemplate.CommonScheme.*;
import static org.junit.Assert.*;

import java.io.*;

import javax.xml.bind.JAXB;

import org.junit.Test;

import com.github.t1.rest.fallback.JsonMessageBodyReader;

import lombok.Data;

public class UriTemplateSerializeTest {
    @Data
    public static class Container {
        UriTemplate template = URI;
    }

    private static final UriTemplate URI =
            http.userInfo("u").host("h").port("po").absolutePath("pa").query("q", "v").fragment("f");
    private static final String JSON = "{\"template\":\"" + URI + "\"}";
    private static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" //
            + "<container>\n" //
            + "    <template>http://u@h:po/pa?q=v#f</template>\n" //
            + "</container>\n" //
            ;

    private static final Container CONTAINER = new Container();

    @Test
    public void shouldReadFromJson() throws Exception {
        Container container = JsonMessageBodyReader.MAPPER.readValue(JSON, Container.class);

        assertEquals(CONTAINER, container);
    }

    @Test
    public void shouldWriteToJson() throws Exception {
        String json = JsonMessageBodyReader.MAPPER.writeValueAsString(CONTAINER);

        assertEquals(JSON, json);
    }

    @Test
    public void shouldWriteToXml() {
        StringWriter writer = new StringWriter();
        JAXB.marshal(CONTAINER, writer);
        String xml = writer.toString();

        assertEquals(XML, xml);
    }

    @Test
    public void shouldReadFromXml() {
        Container container = JAXB.unmarshal(new StringReader(XML), Container.class);

        assertEquals(CONTAINER, container);
    }
}
