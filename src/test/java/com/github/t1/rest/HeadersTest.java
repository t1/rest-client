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
        new Headers().header(null, "foo");
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailToAddHeaderWithNullValue() {
        new Headers().header("foo", null);
    }

    @Test
    public void shouldCreateEmptyHeaders() {
        Headers headers = new Headers();

        assertEquals(0, headers.size());
        assertFalse(headers.iterator().hasNext());
        assertEquals("", headers.toString());
        assertFalse(headers.contains("Key"));
    }

    @Test
    public void shouldCreateSingleHeaders() {
        Headers headers = new Headers().header("Key", true);

        assertEquals(1, headers.size());
        Iterator<Header> i = headers.iterator();
        assertTrue(i.hasNext());
        assertEquals("Key: true", i.next().toString());
        assertFalse(i.hasNext());
        assertEquals("Key", headers.getHeaderNames());
        assertEquals("Key: true", headers.toString());
        assertEquals("true", headers.get("Key"));
        assertEquals("true", headers.get("key"));
        assertTrue(headers.contains("Key"));
    }

    @Test
    public void shouldCreateTwoHeaders() {
        Headers headers = new Headers().header("One", true).header("Two", 2);

        assertEquals(2, headers.size());
        Iterator<Header> i = headers.iterator();
        assertTrue(i.hasNext());
        assertEquals("One: true", i.next().toString());
        assertTrue(i.hasNext());
        assertEquals("Two: 2", i.next().toString());
        assertFalse(i.hasNext());
        assertEquals("One, Two", headers.getHeaderNames());
        assertEquals("One: true | Two: 2", headers.toString());

        assertEquals("true", headers.get("One"));
        assertEquals("true", headers.get("one"));
        assertEquals("2", headers.get("Two"));
        assertEquals("2", headers.get("two"));
    }

    @Test
    public void shouldCreateThreeHeaders() {
        Headers headers = new Headers().header("One", true).header("Two", 2).header("Three", 3.0);

        assertEquals(3, headers.size());
        Iterator<Header> i = headers.iterator();
        assertTrue(i.hasNext());
        assertEquals("One: true", i.next().toString());
        assertTrue(i.hasNext());
        assertEquals("Two: 2", i.next().toString());
        assertTrue(i.hasNext());
        assertEquals("Three: 3.0", i.next().toString());
        assertFalse(i.hasNext());
        assertEquals("One, Two, Three", headers.getHeaderNames());
        assertEquals("One: true | Two: 2 | Three: 3.0", headers.toString());

        assertEquals("true", headers.get("One"));
        assertEquals("2", headers.get("Two"));
        assertEquals("3.0", headers.get("Three"));
    }

    @Test
    public void shouldJustAppendHeaders() {
        Headers zero = new Headers();
        Headers one = zero.header("One", true);
        Headers two = one.header("Two", 2);
        Headers three = two.header("Three", 3.0);

        assertEquals(0, zero.size());
        assertEquals(1, one.size());
        assertEquals(2, two.size());
        assertEquals(3, three.size());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldCheckForDuplicateHeaders() {
        new Headers().header("Key", 1).header("Key", 2);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldCheckForDuplicateHeadersDifferentCase() {
        new Headers().header("KEY", 1).header("key", 2);
    }

    @Test
    @Ignore
    public void shouldCollectHeaders() {
        Headers headers = new Headers().header("Key", 1).header("Key", 2);

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
        assertEquals("Accept", headers.getHeaderNames());
        assertEquals("Accept: application/json;q=1.0, text/html;charset=utf-8", headers.toString());

        assertEquals("application/json;q=1.0, text/html;charset=utf-8", headers.get("Accept"));
    }

    @Test
    public void shouldAddContentTypeHeader() {
        Headers headers = new Headers().contentType(new MediaType("text", "html", "utf-8"));

        assertEquals(1, headers.size());
        Iterator<Header> i = headers.iterator();
        assertTrue(i.hasNext());
        assertEquals("Content-Type: text/html;charset=utf-8", i.next().toString());
        assertFalse(i.hasNext());
        assertEquals("Content-Type", headers.getHeaderNames());
        assertEquals("Content-Type: text/html;charset=utf-8", headers.toString());

        assertEquals("text/html;charset=utf-8", headers.get("Content-Type"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToAddEmptyAcceptHeader() {
        new Headers().accept(Collections.<MediaType> emptyList());
    }
}
