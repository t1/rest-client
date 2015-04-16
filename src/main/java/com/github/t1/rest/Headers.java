package com.github.t1.rest;

import static java.util.Collections.*;
import static lombok.AccessLevel.*;

import java.util.*;

import javax.ws.rs.core.*;

import lombok.*;

import com.github.t1.rest.Headers.Header;

@Value
@Getter(NONE)
public class Headers implements Iterable<Header> {
    @Value
    public static class Header {
        String name;
        String value;

        @Override
        public String toString() {
            return name + ": " + value;
        }
    }

    private final Header head;
    private final Headers tail;

    public Headers() {
        this(null, null);
    }

    private Headers(Header head, Headers tail) {
        this.head = head;
        this.tail = tail;
        if (head != null)
            checkForDuplicates(head.name());
    }

    private void checkForDuplicates(String name) {
        if (tail == null || tail.head == null)
            return;
        if (tail.head.name().equals(name))
            throw new IllegalStateException(name + " header already set");
        tail.checkForDuplicates(name);
    }

    public Headers accept(List<MediaType> mediaTypes) {
        StringBuilder out = new StringBuilder();
        for (MediaType mediaType : mediaTypes) {
            if (out.length() > 0)
                out.append(", ");
            out.append(mediaType);
        }
        return with("Accept", out.toString());
    }

    public Headers with(String name, Object value) {
        return new Headers(new Header(name, value.toString()), this);
    }

    public String get(String name) {
        if (head.name().equals(name))
            return head.value();
        return tail.get(name);
    }

    public int size() {
        if (head == null)
            return 0;
        return 1 + ((tail == null) ? 0 : tail.size());
    }

    @Override
    public Iterator<Header> iterator() {
        Deque<Header> result = new LinkedList<>();
        for (Headers headers = this; headers != null; headers = headers.tail)
            if (headers.head != null)
                result.push(headers.head);
        return unmodifiableCollection(result).iterator();
    }

    MultivaluedMap<String, String> toMultiValuedMap() {
        return new HeadersMultivaluedMap(this);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (Header header : this) {
            if (out.length() > 0)
                out.append(", ");
            out.append(header.name());
        }
        return out.toString();
    }
}
