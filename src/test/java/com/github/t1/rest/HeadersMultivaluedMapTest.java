package com.github.t1.rest;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static javax.ws.rs.core.MediaType.*;
import static org.junit.Assert.*;

import java.util.*;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.Test;

public class HeadersMultivaluedMapTest {
    @Test
    public void shouldCreateEmptyMultivaluedMap() {
        Headers headers = new Headers();

        MultivaluedMap<String, String> map = headers.toMultiValuedMap();

        assertEquals(0, map.size());
        assertEquals(true, map.isEmpty());
        assertEquals(false, map.containsKey("Accept"));
        assertEquals(false, map.containsValue(TEXT_PLAIN));
        assertEquals(false, map.containsValue(TEXT_HTML));
        assertEquals(false, map.containsValue(asList(TEXT_PLAIN, TEXT_HTML)));
        assertEquals(emptyList(), map.get("Accept"));

        Iterator<Map.Entry<String, List<String>>> entries = map.entrySet().iterator();
        assertFalse(entries.hasNext());

        Collection<List<String>> values = map.values();
        assertTrue(values.isEmpty());

        Set<String> keySet = map.keySet();
        assertTrue(keySet.isEmpty());
    }

    @Test
    public void shouldAddMultipleHeadersToMultivaluedMap() {
        Headers headers = new Headers().accept(TEXT_PLAIN_TYPE, TEXT_HTML_TYPE);

        MultivaluedMap<String, String> map = headers.toMultiValuedMap();

        assertEquals(2, map.size());
        assertEquals(false, map.isEmpty());
        assertEquals(true, map.containsKey("Accept"));
        assertEquals(true, map.containsValue(TEXT_PLAIN));
        assertEquals(true, map.containsValue(TEXT_HTML));
        assertEquals(asList(TEXT_PLAIN, TEXT_HTML), map.get("Accept"));

        Iterator<Map.Entry<String, List<String>>> entries = map.entrySet().iterator();
        assertTrue(entries.hasNext());
        Map.Entry<String, List<String>> entry = entries.next();
        assertEquals("Accept", entry.getKey());
        assertEquals(asList(TEXT_PLAIN, TEXT_HTML), entry.getValue());
        assertFalse(entries.hasNext());

        Collection<List<String>> values = map.values();
        assertFalse(values.isEmpty());
        assertEquals(1, values.size());
        assertEquals(asList(TEXT_PLAIN, TEXT_HTML), values.iterator().next());

        Set<String> keySet = map.keySet();
        assertEquals(singleton("Accept"), keySet);
    }

    @Test
    public void shouldBeEqualButNotTheSameMultivaluedMap() {
        Headers headers = new Headers().accept(TEXT_PLAIN_TYPE, TEXT_HTML_TYPE);

        MultivaluedMap<String, String> map1 = headers.toMultiValuedMap();
        MultivaluedMap<String, String> map2 = headers.toMultiValuedMap();

        assertFalse(map1 == map2);
        assertEquals(map1, map2);
        assertEquals(map2, map1);

        assertEquals(map1.entrySet(), map2.entrySet());
        assertEquals(map2.entrySet(), map1.entrySet());

        assertEquals(map1.get("Accept").get(0), map2.get("Accept").get(0));
        assertEquals(map2.get("Accept").get(0), map1.get("Accept").get(0));
        assertEquals(map1.get("Accept").get(1), map2.get("Accept").get(1));
        assertEquals(map2.get("Accept").get(1), map1.get("Accept").get(1));
        assertEquals(map1.get("Accept"), map2.get("Accept"));
        assertEquals(map2.get("Accept"), map1.get("Accept"));
    }

    @Test
    public void shouldAddToEmpty() {
        Headers headers = new Headers();
        MultivaluedMap<String, String> map = headers.toMultiValuedMap();

        map.add("Accept", TEXT_PLAIN);

        assertEquals(asList(TEXT_PLAIN), map.get("Accept"));
    }

    @Test
    public void shouldAddToExisting() {
        Headers headers = new Headers().accept(TEXT_XML_TYPE);
        MultivaluedMap<String, String> map = headers.toMultiValuedMap();

        map.add("Accept", TEXT_PLAIN);

        assertEquals(asList(TEXT_XML, TEXT_PLAIN), map.get("Accept"));
    }

    @Test
    public void shouldAddFirstToEmpty() {
        Headers headers = new Headers();
        MultivaluedMap<String, String> map = headers.toMultiValuedMap();

        map.addFirst("Accept", TEXT_PLAIN);

        assertEquals(asList(TEXT_PLAIN), map.get("Accept"));
    }

    @Test
    public void shouldAddFirstToExisting() {
        Headers headers = new Headers().accept(TEXT_XML_TYPE);
        MultivaluedMap<String, String> map = headers.toMultiValuedMap();

        map.addFirst("Accept", TEXT_PLAIN);

        assertEquals(asList(TEXT_PLAIN, TEXT_XML), map.get("Accept"));
    }

    @Test
    public void shouldAddAllByList() {
        Headers headers = new Headers().accept(TEXT_XML_TYPE);
        MultivaluedMap<String, String> map = headers.toMultiValuedMap();

        map.addAll("Accept", asList(TEXT_PLAIN, TEXT_HTML));

        assertEquals(asList(TEXT_XML, TEXT_PLAIN, TEXT_HTML), map.get("Accept"));
    }

    @Test
    public void shouldAddAllByVarargs() {
        Headers headers = new Headers().accept(TEXT_XML_TYPE);
        MultivaluedMap<String, String> map = headers.toMultiValuedMap();

        map.addAll("Accept", TEXT_PLAIN, TEXT_HTML);

        assertEquals(asList(TEXT_XML, TEXT_PLAIN, TEXT_HTML), map.get("Accept"));
    }

    @Test
    public void shouldPutSingle() {
        Headers headers = new Headers().accept(TEXT_PLAIN_TYPE, TEXT_HTML_TYPE);
        MultivaluedMap<String, String> map = headers.toMultiValuedMap();

        map.putSingle("Content-Type", APPLICATION_JSON);

        assertEquals(APPLICATION_JSON, map.getFirst("Content-Type"));
    }

    @Test
    public void shouldPutList() {
        Headers headers = new Headers();
        MultivaluedMap<String, String> map = headers.toMultiValuedMap();

        map.put("Accept", asList(TEXT_PLAIN, TEXT_HTML));

        assertEquals(asList(TEXT_PLAIN, TEXT_HTML), map.get("Accept"));
    }

    @Test
    public void shouldPutAll() {
        Headers headers = new Headers();
        MultivaluedMap<String, String> map = headers.toMultiValuedMap();

        Map<String, List<String>> newMap = new HashMap<>();
        newMap.put("Accept", asList(TEXT_PLAIN, TEXT_HTML));

        map.putAll(newMap);

        assertEquals(asList(TEXT_PLAIN, TEXT_HTML), map.get("Accept"));
    }

    @Test
    public void shouldRemove() {
        Headers headers = new Headers().accept(TEXT_PLAIN_TYPE, TEXT_HTML_TYPE).contentType(TEXT_XML_TYPE);
        MultivaluedMap<String, String> map = headers.toMultiValuedMap();

        List<String> removed = map.remove("Accept");

        assertEquals(asList(TEXT_PLAIN, TEXT_HTML), removed);
        assertEquals(null, map.getFirst("Accept"));
        assertEquals(TEXT_XML, map.getFirst("Content-Type"));
    }

    @Test
    public void shouldClear() {
        Headers headers = new Headers().accept(TEXT_PLAIN_TYPE, TEXT_HTML_TYPE);
        MultivaluedMap<String, String> map = headers.toMultiValuedMap();

        map.clear();

        assertEquals(null, map.getFirst("Accept"));
    }

    @Test
    public void shouldNotModifyHeaderWhenModifyingMultivaluedMap() {
        Headers headers = new Headers().accept(TEXT_PLAIN_TYPE, TEXT_HTML_TYPE);
        String orig = headers.toString();

        MultivaluedMap<String, String> map = headers.toMultiValuedMap();
        map.add("Content-Type", APPLICATION_JSON);

        assertEquals(orig, headers.toString());
    }

    @Test
    public void shouldBeEqualWhenIgnoringOrder() {
        Headers headers = new Headers().accept(TEXT_PLAIN_TYPE);
        MultivaluedMap<String, String> map1 = headers.toMultiValuedMap();
        MultivaluedMap<String, String> map2 = headers.toMultiValuedMap();

        map1.add("Accept", TEXT_HTML);
        map2.addFirst("Accept", TEXT_HTML);

        assertNotEquals(map1, map2);
        assertTrue(map1.equalsIgnoreValueOrder(map2));
    }

    @Test
    public void shouldBeDifferentSizeEvenWhenIgnoringOrder() {
        Headers headers = new Headers().accept(TEXT_PLAIN_TYPE);
        MultivaluedMap<String, String> map1 = headers.toMultiValuedMap();
        MultivaluedMap<String, String> map2 = headers.toMultiValuedMap();

        map1.add("Accept", TEXT_HTML);

        assertNotEquals(map1, map2);
        assertFalse(map1.equalsIgnoreValueOrder(map2));
    }

    @Test
    public void shouldBeDifferentKeyWhenIgnoringOrder() {
        Headers headers = new Headers();
        MultivaluedMap<String, String> map1 = headers.toMultiValuedMap();
        MultivaluedMap<String, String> map2 = headers.toMultiValuedMap();

        map1.add("Accept", TEXT_HTML);
        map2.add("Content-Type", TEXT_HTML);

        assertNotEquals(map1, map2);
        assertFalse(map1.equalsIgnoreValueOrder(map2));
    }

    @Test
    public void shouldBeDifferentValueWhenIgnoringOrder() {
        Headers headers = new Headers().accept(TEXT_PLAIN_TYPE);
        MultivaluedMap<String, String> map1 = headers.toMultiValuedMap();
        MultivaluedMap<String, String> map2 = headers.toMultiValuedMap();

        map1.add("Content-Type", TEXT_HTML);

        assertNotEquals(map1, map2);
        assertFalse(map1.equalsIgnoreValueOrder(map2));
    }

    @Test
    public void shouldNotBeEqualToADifferentType() {
        MultivaluedMap<String, String> map = new Headers().accept(TEXT_PLAIN_TYPE).toMultiValuedMap();

        assertNotEquals(map, "");
    }

    @Test
    public void shouldCollectWithSameKeys() {
        Headers headers = new Headers().header("foo", "bar").header("foo", "baz");
        MultivaluedMap<String, String> map = headers.toMultiValuedMap();

        assertEquals(asList("bar", "baz"), map.get("foo"));
    }

    // TODO write changes to HeadersMultivaluedMap#keySet()
    // TODO write changes to HeadersMultivaluedMap#values()
    // TODO write changes to HeadersMultivaluedMap#entrySet()
    // TODO set value in HeadersMultivaluedMap#entrySet() -> setValue()
}
