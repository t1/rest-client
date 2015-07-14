package com.github.t1.rest;

import static org.junit.Assert.*;

import org.junit.*;

import lombok.*;

// FIXME
@Ignore("needs rework on the RestClientMockRule")
public class TutorialTest {
    @Rule
    public final RestClientMockRule service = new RestClientMockRule();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pojo {
        String key, value;
    }

    @Before
    public void before() {
        service.on("http://example.org/path").reply("value");
        service.on("http://example.org/pojo").reply(new Pojo("k", "v"));
    }

    @Test
    public void testGetString() {
        String value = new RestResource("http://example.org/path").get(String.class);

        assertEquals("value", value);
    }

    @Test
    public void testGetPojo() {
        Pojo value = new RestResource("http://example.org/pojo").get(Pojo.class);

        assertEquals("k", value.getKey());
        assertEquals("v", value.getValue());
    }
}
