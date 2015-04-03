package com.github.t1.rest;

import static org.junit.Assert.*;

import org.junit.Test;

import com.github.t1.rest.Rest;

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
