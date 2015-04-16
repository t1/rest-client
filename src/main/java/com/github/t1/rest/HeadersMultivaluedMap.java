package com.github.t1.rest;

import static java.util.Collections.*;

import java.util.*;

import javax.ws.rs.core.MultivaluedMap;

import lombok.AllArgsConstructor;

import org.jboss.weld.exceptions.UnsupportedOperationException;

import com.github.t1.rest.Headers.Header;

/** A view on {@link Headers} */
@AllArgsConstructor
class HeadersMultivaluedMap implements MultivaluedMap<String, String> {
    private Headers headers;

    @Override
    public int size() {
        return headers.size();
    }

    @Override
    public boolean isEmpty() {
        return headers.size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return headers.get((String) key) == null;
    }

    @Override
    public boolean containsValue(Object value) {
        for (Header header : headers)
            if (header.value().equals(value))
                return true;
        return false;
    }

    @Override
    public List<String> get(Object key) {
        String value = headers.get((String) key); // TODO multi-values
        return (value == null) ? Collections.<String> emptyList() : singletonList(value);
    }

    @Override
    public List<String> put(String key, List<String> values) {
        for (String value : values)
            putSingle(key, value);
        return null; // there can't be an old value in Headers
    }

    @Override
    public List<String> remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ? extends List<String>> map) {
        for (Map.Entry<? extends String, ? extends List<String>> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> keySet() {
        // #keySet only supports element removal, which we don't
        // it's still not fully compliant, as changes to the headers are not reflected by this snapshot
        Set<String> set = new LinkedHashSet<>();
        for (Header header : headers)
            set.add(header.name());
        return unmodifiableSet(set);
    }

    @Override
    public Collection<List<String>> values() {
        // #values only supports element removal, which we don't
        // it's still not fully compliant, as changes to the headers are not reflected by this snapshot
        Set<List<String>> set = new LinkedHashSet<>();
        for (Header header : headers)
            set.add(singletonList(header.value())); // TODO multi-values
        return unmodifiableSet(set);
    }

    @Override
    public Set<Map.Entry<String, List<String>>> entrySet() {
        // #entrySet only supports element removal, which we don't
        // it's still not fully compliant, as changes to the headers are not reflected by this snapshot
        Set<Map.Entry<String, List<String>>> set = new LinkedHashSet<>();
        for (final Header header : headers) {
            set.add(new Map.Entry<String, List<String>>() {
                @Override
                public String getKey() {
                    return header.name();
                }

                @Override
                public List<String> getValue() {
                    return singletonList(header.value()); // TODO multi-values
                }

                @Override
                public List<String> setValue(List<String> value) {
                    throw new UnsupportedOperationException();
                }
            });
        }
        return unmodifiableSet(set);
    }

    @Override
    public void putSingle(String key, String value) {
        add(key, value); // TODO is this an unsupported operation?
    }

    @Override
    public void add(String key, String value) {
        headers = headers.with(key, value);
    }

    @Override
    public String getFirst(String key) {
        return headers.get(key);
    }

    @Override
    public void addAll(String key, String... values) {
        for (String value : values) {
            putSingle(key, value);
        }
    }

    @Override
    public void addAll(String key, List<String> values) {
        for (String value : values) {
            putSingle(key, value);
        }
    }

    @Override
    public void addFirst(String key, String value) {
        putSingle(key, value); // TODO multi-values
    }

    @Override
    public boolean equalsIgnoreValueOrder(MultivaluedMap<String, String> otherMap) {
        if (otherMap.size() != headers.size())
            return false;
        for (Map.Entry<String, List<String>> entry : otherMap.entrySet()) {
            if (entry.getValue().size() != 1)
                return false;
            String actual = headers.get(entry.getKey()); // TODO multi-values
            if (actual == null)
                return false;
            if (!actual.equals(entry.getValue().get(0)))
                return false;
        }
        return true;
    }
}
