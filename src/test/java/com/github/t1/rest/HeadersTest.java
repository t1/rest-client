package com.github.t1.rest;

import com.github.t1.rest.Headers.Header;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.*;

import static java.util.Arrays.*;
import static javax.ws.rs.core.MediaType.*;
import static org.junit.Assert.*;

public class HeadersTest {
    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToCreateHeaderWithNullName() {
        new Headers.Header(null, "foo");
    }

    @Test(expected = IllegalArgumentException.class)
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
        assertTrue(headers.isEmpty());
    }

    @Test
    public void shouldEqualEmptyHeaders() {
        Headers headers1 = new Headers();
        Headers headers2 = new Headers();

        assertEquals(headers2, headers1);
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
        assertEquals("true", headers.firstValue("Key"));
        assertEquals("true", headers.firstValue("key"));
        assertTrue(headers.contains("Key"));
        assertFalse(headers.isEmpty());
    }

    @Test
    public void shouldEqualSingleHeaders() {
        Headers headers1 = new Headers().header("Key", true);
        Headers headers2 = new Headers().header("Key", true);

        assertEquals(headers2, headers1);
    }

    @Test
    public void shouldNotEqualSingleHeadersDifferentKey() {
        Headers headers1 = new Headers().header("Key1", true);
        Headers headers2 = new Headers().header("Key2", true);

        assertNotEquals(headers2, headers1);
    }

    @Test
    public void shouldNotEqualSingleHeadersDifferentValue() {
        Headers headers1 = new Headers().header("Key", true);
        Headers headers2 = new Headers().header("Key", false);

        assertNotEquals(headers2, headers1);
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
        assertEquals("One: true & Two: 2", headers.toString());
        assertEquals("  One: true\n  Two: 2\n", headers.toListString());

        assertEquals("true", headers.firstValue("One"));
        assertEquals("true", headers.firstValue("one"));
        assertEquals("2", headers.firstValue("Two"));
        assertEquals("2", headers.firstValue("two"));
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
        assertEquals("One: true & Two: 2 & Three: 3.0", headers.toString());
        assertEquals("  One: true\n  Two: 2\n  Three: 3.0\n", headers.toListString());

        assertEquals("true", headers.firstValue("One"));
        assertEquals("2", headers.firstValue("Two"));
        assertEquals("3.0", headers.firstValue("Three"));
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

    @Test
    public void shouldAddAcceptHeader() {
        Map<String, String> properties = new HashMap<>();
        properties.put("q", "1.0");
        MediaType json = new MediaType("application", "json", properties);
        Headers headers = new Headers().accept(json, new MediaType("text", "html", "utf-8"));

        assertEquals(2, headers.size());
        Iterator<Header> i = headers.iterator();
        assertTrue(i.hasNext());
        assertEquals("Accept: application/json;q=1.0", i.next().toString());
        assertTrue(i.hasNext());
        assertEquals("Accept: text/html;charset=utf-8", i.next().toString());
        assertFalse(i.hasNext());
        assertEquals("Accept", headers.getHeaderNames());
        assertEquals("Accept: application/json;q=1.0 & Accept: text/html;charset=utf-8", headers.toString());

        assertEquals("text/html;charset=utf-8", headers.firstValue("Accept"));
        assertEquals(asList("application/json;q=1.0", "text/html;charset=utf-8"), headers.values("Accept"));
        assertEquals(
                asList( //
                        MediaType.valueOf("application/json;q=1.0"), //
                        MediaType.valueOf("text/html;charset=utf-8")), //
                headers.accept());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToAddEmptyAcceptHeader() {
        new Headers().accept(Collections.emptyList());
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

        assertEquals("text/html;charset=utf-8", headers.firstValue("Content-Type"));
    }

    @Test
    public void shouldGetWildCardContentTypeHeader() {
        Headers headers = new Headers();

        assertEquals(WILDCARD_TYPE, headers.contentType());
    }

    @Test
    public void shouldStripStrangeQuantityAndTemplateContentTypeHeader() {
        Headers headers = new Headers().header("Content-Type", "{*/*, q=1000}");

        assertEquals(WILDCARD_TYPE, headers.contentType());
    }

    @Test
    public void shouldAddContentLength() {
        Headers headers = new Headers().contentLength(123);

        assertEquals((Integer) 123, headers.contentLength());
    }

    @Test
    public void shouldGetNullContentLength() {
        Headers headers = new Headers();

        assertNull(headers.contentLength());
    }

    @Test
    public void shouldReplaceHeaderVariable() {
        Headers headers = new Headers().header("foo", "{bar}");

        Headers resolved = headers.with("bar", "foobar");

        assertEquals("foo: {bar}", headers.toString());
        assertEquals("foo: foobar", resolved.toString());
    }

    @Test
    public void shouldCombineViaHeaders() {
        Headers headers = new Headers().header("Via", "1.0 fred");

        Headers headers2 = headers.header("Via", "1.1 nowhere.com (Apache/1.1)");

        assertEquals("  Via: 1.0 fred, 1.1 nowhere.com (Apache/1.1)\n", headers2.toListString());
    }

    // TODO combine User-Agent headers with spaces
}
