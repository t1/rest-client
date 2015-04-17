package com.github.t1.rest;

import static org.junit.Assert.*;

import java.util.*;

import javax.ws.rs.core.MediaType;

import org.junit.*;

import com.github.t1.rest.Headers.Header;

public class HeadersTest {
    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("unused")
    public void shouldFailToCreateHeaderWithNullName() {
        new Headers.Header(null, "foo");
    }

    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("unused")
    public void shouldFailToCreateHeaderWithNullValue() {
        new Headers.Header("foo", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToAddHeaderWithNullName() {
        new Headers().with(null, "foo");
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailToAddHeaderWithNullValue() {
        new Headers().with("foo", null);
    }

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
        assertEquals("true", headers.get("key"));
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
        assertEquals("true", headers.get("one"));
        assertEquals("2", headers.get("Two"));
        assertEquals("2", headers.get("two"));
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

    @Test(expected = IllegalStateException.class)
    public void shouldCheckForDuplicateHeaders() {
        new Headers().with("Key", 1).with("Key", 2);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldCheckForDuplicateHeadersDifferentCase() {
        new Headers().with("KEY", 1).with("key", 2);
    }

    @Test
    @Ignore
    public void shouldCollectHeaders() {
        Headers headers = new Headers().with("Key", 1).with("Key", 2);

        assertEquals(1, headers.size());
        Iterator<Header> i = headers.iterator();
        assertTrue(i.hasNext());
        assertEquals("Key: 1, 2", i.next().toString());
        assertFalse(i.hasNext());
        assertEquals("Key", headers.toString());

        assertEquals("1", headers.get("Key"));
    }

    @Test
    public void shouldAddAcceptHeader() {
        Map<String, String> properties = new HashMap<>();
        properties.put("q", "1.0");
        MediaType json = new MediaType("application", "json", properties);
        Headers headers = new Headers().accept(json, new MediaType("text", "html", "utf-8"));

        assertEquals(1, headers.size());
        Iterator<Header> i = headers.iterator();
        assertTrue(i.hasNext());
        assertEquals("Accept: application/json;q=1.0, text/html;charset=utf-8", i.next().toString());
        assertFalse(i.hasNext());
        assertEquals("Accept", headers.toString());

        assertEquals("application/json;q=1.0, text/html;charset=utf-8", headers.get("Accept"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToAddEmptyAcceptHeader() {
        new Headers().accept(Collections.<MediaType> emptyList());
    }
}
