package com.github.t1.rest;

import java.util.*;

import javax.ws.rs.core.MediaType;

import lombok.Value;

@Value
public class Headers {
    @Value
    public static class Header {
        String name, value;

        @Override
        public String toString() {
            return name + ": " + value;
        }
    }

    private final List<Header> headers;

    public Headers() {
        this(Collections.<Header> emptyList());
    }

    public Headers(List<Header> headers) {
        this.headers = headers;
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
        return new Headers(with(headers, new Header(name, value.toString())));
    }

    private static List<Header> with(List<Header> base, Header header) {
        if (base.isEmpty())
            return Collections.singletonList(header);
        checkUnset(base, header.name());
        List<Header> out = new ArrayList<>(base);
        out.add(header);
        return Collections.unmodifiableList(out);
    }

    private static void checkUnset(List<Header> base, String name) {
        for (Header header : base)
            if (header.name().equals(name))
                throw new IllegalStateException(name + " header already set");
    }

    @Override
    public String toString() {
        return headers.toString();
    }
}
