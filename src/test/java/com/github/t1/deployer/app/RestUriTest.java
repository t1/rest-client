package com.github.t1.deployer.app;

import static org.junit.Assert.*;

import org.junit.Test;

public class RestUriTest {
    private Rest parse(String uri) {
        Rest rest = new Rest(uri);
        assertEquals(uri, rest.uri());
        return rest;
    }

    @Test(expected = RuntimeException.class)
    public void shouldFailParsingWithoutScheme() {
        parse("http");
    }

    @Test(expected = RuntimeException.class)
    public void shouldFailParsingUnknownScheme() {
        @SuppressWarnings("unused")
        Rest rest = new Rest("mailto:test@example.com");
    }
}
