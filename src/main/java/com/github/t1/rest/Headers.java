package com.github.t1.rest;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.github.t1.rest.Headers.Header;
import lombok.*;

import javax.annotation.concurrent.Immutable;
import javax.ws.rs.core.*;
import java.io.*;
import java.util.ArrayList;
import java.util.*;
import java.util.Map.Entry;

import static com.fasterxml.jackson.core.JsonToken.*;
import static com.github.t1.rest.PathVariableExpression.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;
import static javax.ws.rs.core.MediaType.*;
import static javax.xml.bind.DatatypeConverter.*;
import static lombok.AccessLevel.*;

@Immutable
@Value
@Getter(NONE)
@JsonSerialize(using = Headers.HeadersSerializer.class)
@JsonDeserialize(using = Headers.HeadersDeserializer.class)
public class Headers implements Iterable<Header>, Serializable {
    private static final long serialVersionUID = 1L;

    public static class HeadersSerializer extends JsonSerializer<Headers> {
        @Override
        public void serialize(Headers headers, JsonGenerator json, SerializerProvider serializers) throws IOException {
            json.writeStartObject();
            for (Header header : headers) {
                json.writeFieldName(header.name());
                json.writeString(header.value());
            }
            json.writeEndObject();
        }
    }

    public static class HeadersDeserializer extends JsonDeserializer<Headers> {
        @Override
        public Headers deserialize(JsonParser json, DeserializationContext context) throws IOException {
            Headers headers = new Headers();
            if (!json.isExpectedStartObjectToken())
                throw new RuntimeException("expected to start headers as object but found: " + json.getCurrentToken());
            while (json.nextToken() == FIELD_NAME) {
                String name = json.getCurrentName();
                String value = json.nextTextValue();
                headers = headers.header(name, value);
            }
            if (json.getCurrentToken() != END_OBJECT)
                throw new RuntimeException("expected to end object but found: " + json.getCurrentToken());
            return headers;
        }
    }

    private static final String ACCEPT = "Accept";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String AUTHORIZATION = "Authorization";

    @Immutable
    @Value
    public static class Header implements Serializable {
        private static final long serialVersionUID = 1L;

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
    }

    private final Header head;
    private final Headers tail;

    /** creates an {@link #isEmpty() empty} header to start with */
    public Headers() {
        this(null, null);
    }

    private Headers(Header head, Headers tail) {
        this.head = head;
        this.tail = tail;
        if (head == null)
            assert tail == null : "a null-header should have no tail";
        if (tail != null)
            assert head != null : "a non-null tail should have a non-null header";
    }

    /** is this a {@link #Headers empty} header */
    public boolean isEmpty() {
        return head == null;
    }

    public Headers header(String name, Object value) {
        return header(new Header(name, value.toString()));
    }

    public Headers header(Header header) {
        return new Headers(header, this);
    }

    public Header firstHeader(String name) {
        if (head == null)
            return null;
        if (head.isNamed(name))
            return head;
        return tail.firstHeader(name);
    }

    public Iterable<String> names() {
        List<String> result = new ArrayList<>();
        for (Header header : this)
            if (!result.contains(header.name()))
                result.add(header.name());
        return unmodifiableList(result);
    }

    public String firstValue(String name) {
        Header header = firstHeader(name);
        return (header == null) ? null : header.value();
    }

    public List<String> values(String name) {
        List<String> result = new ArrayList<>();
        for (Header header : this)
            if (header.isNamed(name))
                result.add(header.value());
        return unmodifiableList(result);
    }

    public boolean contains(String name) {
        return firstHeader(name) != null;
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
        Set<String> result = new LinkedHashSet<>();
        for (Header header : this)
            result.add(header.name);
        StringBuilder out = new StringBuilder();
        for (String name : result) {
            if (out.length() > 0)
                out.append(", ");
            out.append(name);
        }
        return out.toString();
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (Header header : this) {
            if (out.length() > 0)
                out.append(" & ");
            out.append(header);
        }
        return out.toString();
    }

    public String toListString() {
        StringBuilder out = new StringBuilder();
        for (Entry<String, List<String>> entry : toMultiValuedMap().entrySet()) {
            out.append("  ").append(entry.getKey()).append(": ");
            boolean first = true;
            for (String value : entry.getValue()) {
                if (first)
                    first = false;
                else
                    out.append(", ");
                out.append(value);
            }
            out.append("\n");
        }
        return out.toString();
    }


    public Headers with(String name, String value) {
        Headers out = new Headers();
        for (Header header : this) {
            String replacedName = replaceVariable(header.name(), name, value);
            String replacedValue = replaceVariable(header.value(), name, value);
            out = out.header(replacedName, replacedValue);
        }
        return out;
    }


    public Headers contentType(MediaType mediaType) {
        return header(CONTENT_TYPE, mediaType);
    }

    public MediaType contentType() {
        String contentType = firstValue(CONTENT_TYPE);
        if (contentType == null)
            return WILDCARD_TYPE;
        if (contentType.startsWith("{") && contentType.endsWith(", q=1000}")) // Jersey/Dropwizard bug?
            contentType = contentType.substring(1, contentType.length() - 9);
        return MediaType.valueOf(contentType);
    }


    public Headers contentLength(Integer contentLength) {
        return header(CONTENT_LENGTH, contentLength);
    }

    public Integer contentLength() {
        String contentLength = firstValue(CONTENT_LENGTH);
        if (contentLength == null)
            return null;
        return Integer.valueOf(contentLength);
    }


    public Headers accept(MediaType... mediaTypes) {
        return accept(asList(mediaTypes));
    }

    public Headers accept(List<MediaType> mediaTypes) {
        if (mediaTypes.isEmpty())
            throw new IllegalArgumentException("can't accept empty list of media types");
        Headers result = this;
        for (MediaType mediaType : mediaTypes)
            result = result.header(ACCEPT, mediaType);
        return result;
    }

    public List<MediaType> accept() {
        List<MediaType> result = new ArrayList<>();
        for (String value : values(ACCEPT)) {
            result.add(MediaType.valueOf(value));
        }
        return result;
    }

    public boolean accepts(MediaType required) {
        for (MediaType accepted : accept())
            if (accepted.isCompatible(required))
                return true;
        return false;
    }


    public Headers basicAuth(Credentials credentials) {
        return header(AUTHORIZATION, basicAuthValue(credentials));
    }

    public boolean isBasicAuth(Credentials credentials) {
        return contains(AUTHORIZATION) && firstValue(AUTHORIZATION).equals(basicAuthValue(credentials));
    }

    private String basicAuthValue(Credentials credentials) {
        return "Basic " + base64(credentials.userName() + ":" + credentials.password());
    }

    private String base64(String string) {
        return printBase64Binary(string.getBytes());
    }
}
