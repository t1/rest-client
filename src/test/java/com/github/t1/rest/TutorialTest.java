package com.github.t1.rest;

import static org.junit.Assert.*;

import org.junit.*;

public class TutorialTest {
    @Rule
    public final RestClientMock service = new RestClientMock();

    @Before
    public void before() {
        service.on("http://example.org/path").reply("value");
    }

    @Test
    public void t1() {
        String value = new RestResource("http://example.org/path").get(String.class);

        assertEquals("value", value);
    }
}
