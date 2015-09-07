package com.github.t1.rest;

import static com.github.t1.rest.UriTemplate.CommonScheme.*;
import static org.junit.Assert.*;

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

    @Test
    public void shouldReadFromJson() throws Exception {
        UriTemplate readValue = JsonMessageBodyReader.MAPPER.readValue(JSON, Container.class).getTemplate();

        assertEquals(URI, readValue);
    }

    @Test
    public void shouldWriteToJson() throws Exception {
        String json = JsonMessageBodyReader.MAPPER.writeValueAsString(new Container());

        assertEquals(JSON, json);
    }
}
