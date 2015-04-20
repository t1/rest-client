package com.github.t1.rest;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static javax.ws.rs.core.MediaType.*;
import static javax.xml.bind.DatatypeConverter.*;
import static lombok.AccessLevel.*;

import java.util.*;

import javax.ws.rs.core.*;

import lombok.*;

import com.github.t1.rest.Headers.Header;

@Value
@Getter(NONE)
public class Headers implements Iterable<Header> {
    private static final String ACCEPT = "Accept";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String AUTHORIZATION = "Authorization";

    private static final String WHITESPACE = "\\s*";

    static List<String> toMultiValue(String value) {
        return asList(value.split(WHITESPACE + "," + WHITESPACE));
    }

    @Value
    public static class Header {
        String name, value;

        public Header(String name, String value) {
            this.name = checkNonEmpty("name", name);
            this.value = checkNonEmpty(name + " header", value);
        }

        private String checkNonEmpty(String name, String string) {
            if (string == null || string.isEmpty())
                throw new IllegalArgumentException(name + " must not be empty");
            return string;
        }

        @Override
        public String toString() {
            return name + ": " + value;
        }

        public boolean isNamed(String name) {
            return this.name.equalsIgnoreCase(name);
        }

        public List<String> multiValue() {
            return toMultiValue(value);
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
        if (tail.head.isNamed(name))
            throw new IllegalStateException(name + " header already set");
        tail.checkForDuplicates(name);
    }

    public Headers contentType(MediaType mediaType) {
        return header(CONTENT_TYPE, mediaType);
    }

    public MediaType contentType() {
        String contentType = get(CONTENT_TYPE);
        if (contentType == null)
            return WILDCARD_TYPE;
        if (contentType.startsWith("{") && contentType.endsWith(", q=1000}")) // Jersey/Dropwizard bug?
            contentType = contentType.substring(1, contentType.length() - 9);
        return MediaType.valueOf(contentType);
    }

    public Integer contentLength() {
        String contentLength = get(CONTENT_LENGTH);
        if (contentLength == null)
            return null;
        return Integer.valueOf(contentLength);
    }

    public Headers accept(MediaType... mediaTypes) {
        return accept(asList(mediaTypes));
    }

    public Headers accept(List<MediaType> mediaTypes) {
        StringBuilder out = new StringBuilder();
        for (MediaType mediaType : mediaTypes) {
            if (out.length() > 0)
                out.append(", ");
            out.append(mediaType);
        }
        return header(ACCEPT, out.toString());
    }

    public List<MediaType> accept() {
        List<MediaType> result = new ArrayList<>();
        for (String value : header(ACCEPT).multiValue()) {
            result.add(MediaType.valueOf(value));
        }
        return result;
    }

    public Headers basicAuth(String userName, String password) {
        return header(AUTHORIZATION, "Basic " + base64(userName + ":" + password));
    }

    private String base64(String string) {
        return printBase64Binary(string.getBytes());
    }

    public Headers header(String name, Object value) {
        return new Headers(new Header(name, value.toString()), this);
    }

    public Header header(String name) {
        if (head == null)
            return null;
        if (head.isNamed(name))
            return head;
        return tail.header(name);
    }

    public String get(String name) {
        Header header = header(name);
        return (header == null) ? null : header.value();
    }

    public boolean contains(String name) {
        return get(name) != null;
    }

    public int size() {
        if (head == null)
            return 0;
        return 1 + tail.size();
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

    public String getHeaderNames() {
        StringBuilder out = new StringBuilder();
        for (Header header : this) {
            if (out.length() > 0)
                out.append(", ");
            out.append(header.name);
        }
        return out.toString();
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (Header header : this) {
            if (out.length() > 0)
                out.append(" | ");
            out.append(header);
        }
        return out.toString();
    }
}
