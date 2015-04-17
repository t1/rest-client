package com.github.t1.rest;

import static java.util.Arrays.*;
import static java.util.Collections.*;

import java.util.*;

import javax.ws.rs.core.MultivaluedMap;

import lombok.AllArgsConstructor;

import org.jboss.weld.exceptions.UnsupportedOperationException;

import com.github.t1.rest.Headers.Header;

/** Based on {@link Headers}; required for some JAX-RS calls; *not* strictly appendable */
@AllArgsConstructor
class HeadersMultivaluedMap implements MultivaluedMap<String, String> {
    private static final String WHITESPACE = "\\s*";

    private static List<String> toMultiValue(String value) {
        return asList(value.split(WHITESPACE + "," + WHITESPACE));
    }

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
        return headers.contains((String) key);
    }

    @Override
    public boolean containsValue(Object value) {
        for (Header header : headers)
            if (toMultiValue(header.value()).equals(value))
                return true;
        return false;
    }

    @Override
    public List<String> get(Object key) {
        if (!containsKey(key))
            return null;
        return toMultiValue(headers.get((String) key));
    }

    @Override
    public List<String> put(String key, List<String> values) {
        for (String value : values)
            add(key, value);
        return null; // there can't be an old value in Headers
    }

    @Override
    public List<String> remove(Object key) {
        String name = (String) key;
        List<String> out = null;
        if (headers.contains(name)) {
            Headers newHeaders = new Headers();
            for (Header header : headers) {
                if (header.isNamed(name)) {
                    out = toMultiValue(header.value());
                } else {
                    newHeaders = newHeaders.with(header.name(), header.value());
                }
            }
            this.headers = newHeaders;
        }
        return out;
    }

    @Override
    public void putAll(Map<? extends String, ? extends List<String>> map) {
        for (Map.Entry<? extends String, ? extends List<String>> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        headers = new Headers();
    }

    @Override
    public Set<String> keySet() {
        // TODO reflect changes to the headers in this snapshot
        Set<String> set = new LinkedHashSet<>();
        for (Header header : headers)
            set.add(header.name());
        return unmodifiableSet(set);
    }

    @Override
    public Collection<List<String>> values() {
        // TODO reflect changes to the headers in this snapshot
        Set<List<String>> set = new LinkedHashSet<>();
        for (Header header : headers)
            set.add(toMultiValue(header.value()));
        return unmodifiableSet(set);
    }

    @Override
    public Set<Map.Entry<String, List<String>>> entrySet() {
        // TODO reflect changes to the headers in this snapshot
        Set<Map.Entry<String, List<String>>> set = new LinkedHashSet<>();
        for (final Header header : headers) {
            set.add(new Map.Entry<String, List<String>>() {
                @Override
                public String getKey() {
                    return header.name();
                }

                @Override
                public List<String> getValue() {
                    return toMultiValue(header.value());
                }

                @Override
                public List<String> setValue(List<String> value) {
                    throw new UnsupportedOperationException(); // TODO set value
                }

                @Override
                public boolean equals(Object obj) {
                    if (this == obj)
                        return true;
                    if (!(obj instanceof Map.Entry))
                        return false;
                    @SuppressWarnings("unchecked")
                    Map.Entry<String, List<String>> that = (java.util.Map.Entry<String, List<String>>) obj;
                    return Objects.equals(this.getKey(), that.getKey())
                            && Objects.equals(this.getValue(), that.getValue());
                }

                @Override
                public int hashCode() {
                    return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
                }

                @Override
                public String toString() {
                    return getKey() + ":" + getValue();
                }
            });
        }
        return unmodifiableSet(set);
    }

    @Override
    public void putSingle(String key, String value) {
        remove(key);
        add(key, value);
    }

    @Override
    public void add(String key, String value) {
        String old = headers.get(key);
        if (old != null) {
            remove(key);
            value = old + ", " + value;
        }
        headers = headers.with(key, value);
    }

    @Override
    public String getFirst(String key) {
        return headers.get(key);
    }

    @Override
    public void addAll(String key, String... values) {
        for (String value : values) {
            add(key, value);
        }
    }

    @Override
    public void addAll(String key, List<String> values) {
        for (String value : values) {
            add(key, value);
        }
    }

    @Override
    public void addFirst(String key, String value) {
        String old = headers.get(key);
        if (old != null) {
            remove(key);
            value = value + ", " + old;
        }
        headers = headers.with(key, value);
    }

    @Override
    public boolean equalsIgnoreValueOrder(MultivaluedMap<String, String> otherMap) {
        if (otherMap.size() != headers.size())
            return false;
        for (Header header : headers) {
            List<String> otherValues = otherMap.get(header.name());
            if (otherValues == null)
                return false;
            Set<String> thisValues = asSet(toMultiValue(header.value()));
            if (!thisValues.equals(asSet(otherValues))) {
                return false;
            }
        }
        return true;
    }

    private HashSet<String> asSet(List<String> list) {
        return new HashSet<>(list);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof MultivaluedMap))
            return false;
        @SuppressWarnings("unchecked")
        MultivaluedMap<String, String> that = (MultivaluedMap<String, String>) obj;
        if (this.size() != that.size())
            return false;
        return this.entrySet().equals(that.entrySet());
    }

    @Override
    public int hashCode() {
        return entrySet().hashCode(); // AbstractSet.hashCode sums the hashes of all elements
        // which is the same algorithm as MultivaluedHashMap (jax-rs 2.0) uses.
    }

    @Override
    public String toString() {
        return headers.toString();
    }
}
