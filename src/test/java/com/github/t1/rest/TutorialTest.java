package com.github.t1.rest;

import static org.junit.Assert.*;

import org.junit.*;

import lombok.*;

public class TutorialTest {
    @Rule
    public final RestClientMockRule service = new RestClientMockRule();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pojo {
        String key, value;
    }

    @Test
    public void testGetString() {
        service.on("http://example.org/string").reply("value");

        String value = new RestResource("http://example.org/string").get(String.class);

        assertEquals("value", value);
    }

    @Test
    public void testGetInt() {
        service.on("http://example.org/int").reply(123);

        int value = new RestResource("http://example.org/int").get(int.class);

        assertEquals(123, value);
    }

    @Test
    public void testGetPojo() {
        service.on("http://example.org/pojo").reply(new Pojo("k", "v"));

        Pojo value = new RestResource("http://example.org/pojo").get(Pojo.class);

        assertEquals("k", value.getKey());
        assertEquals("v", value.getValue());
    }
}
