package com.github.t1.rest;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

import com.github.t1.rest.Headers.Header;

public class HeadersTest {
    @Test
    public void shouldCreateEmptyHeaders() {
        Headers headers = new Headers();

        assertEquals(0, headers.size());
        assertFalse(headers.iterator().hasNext());
        assertEquals("", headers.toString());
    }

    @Test
    public void shouldCreateSingleHeaders() {
        Headers headers = new Headers().with("Key", true);

        assertEquals(1, headers.size());
        Iterator<Header> i = headers.iterator();
        assertTrue(i.hasNext());
        assertEquals("Key: true", i.next().toString());
        assertFalse(i.hasNext());
        assertEquals("Key", headers.toString());
        assertEquals("true", headers.get("Key"));
    }

    @Test
    public void shouldCreateTwoHeaders() {
        Headers headers = new Headers().with("One", true).with("Two", 2);

        assertEquals(2, headers.size());
        Iterator<Header> i = headers.iterator();
        assertTrue(i.hasNext());
        assertEquals("One: true", i.next().toString());
        assertTrue(i.hasNext());
        assertEquals("Two: 2", i.next().toString());
        assertFalse(i.hasNext());
        assertEquals("One, Two", headers.toString());

        assertEquals("true", headers.get("One"));
        assertEquals("2", headers.get("Two"));
    }

    @Test
    public void shouldCreateThreeHeaders() {
        Headers headers = new Headers().with("One", true).with("Two", 2).with("Three", 3.0);

        assertEquals(3, headers.size());
        Iterator<Header> i = headers.iterator();
        assertTrue(i.hasNext());
        assertEquals("One: true", i.next().toString());
        assertTrue(i.hasNext());
        assertEquals("Two: 2", i.next().toString());
        assertTrue(i.hasNext());
        assertEquals("Three: 3.0", i.next().toString());
        assertFalse(i.hasNext());
        assertEquals("One, Two, Three", headers.toString());

        assertEquals("true", headers.get("One"));
        assertEquals("2", headers.get("Two"));
        assertEquals("3.0", headers.get("Three"));
    }

    @Test
    public void shouldJustAppendHeaders() {
        Headers zero = new Headers();
        Headers one = zero.with("One", true);
        Headers two = one.with("Two", 2);
        Headers three = two.with("Three", 3.0);

        assertEquals(0, zero.size());
        assertEquals(1, one.size());
        assertEquals(2, two.size());
        assertEquals(3, three.size());
    }
}
