package com.github.t1.rest;

import static java.util.Arrays.*;
import static javax.ws.rs.core.Response.Status.*;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;

import com.github.t1.rest.UriTemplate.NonFragment;
import com.github.t1.rest.UriTemplate.UriPath;

/**
 * Immutable, fluent, strictly appendable client for RESTful web services.
 * <p/>
 * A REST call consists of these parts:
 * <ol>
 * <li>The configuration used for the request (converters, timeouts, etc.)</li>
 * <li>The (initial) URI template where the request goes to</li>
 * <li>The headers that should be sent</li>
 * <li>The body that should be sent</li>
 * <li>The values to replace the templates with</li>
 * <li>The chain of link rels to follow</li>
 * </ol>
 * These parts have to be specified in this order.
 * <p/>
 * A REST response consists of these parts:
 * <ul>
 * <li>The URI template where the (final) answer came from</li>
 * <li>The response status</li>
 * <li>The headers that where returned</li>
 * <li>The body that was returned</li>
 * </ul>
 */
@Slf4j
@Getter
public class Rest<T> {
    private static final String ACCEPT_HEADER = "Accept";
    private static final CloseableHttpClient client = HttpClients.createDefault();
    private static final List<String> ALLOWED_SCHEMES = asList("http", "https");

    private final RestConfig config;
    private final NonFragment uri;
    private RestConverter<T> converter;

    public Rest(RestConfig config, String uri) {
        this(config, check(UriTemplate.fromString(uri)), null);
    }

    private Rest(RestConfig config, NonFragment uri, RestConverter<T> converter) {
        this.config = config;
        this.uri = uri;
        this.converter = converter;
    }

    private static NonFragment check(UriTemplate uri) {
        if (!ALLOWED_SCHEMES.contains(uri.scheme()))
            throw new RuntimeException("unsupported scheme for REST: " + uri.scheme());
        return (NonFragment) uri;
    }

    public Rest<T> path(String path) {
        NonFragment subPath = ((UriPath) this.uri).path(path);
        return new Rest<>(config, subPath, converter);
    }

    public <U> Rest<U> accept(Class<U> acceptedType) {
        if (this.converter != null)
            throw new IllegalStateException("already accepting " + this.converter + ". " //
                    + "Can't also accept " + acceptedType);
        RestConverter<U> converter = config.converterFor(acceptedType);
        return new Rest<>(config, uri, converter);
    }

    /**
     * Normally you wouldn't call this: the acceptable types are determined by the readers available for the type you
     * passed to {@link #accept(Class)}. This method is only needed if you (must) know that the server would return some
     * type, but it's not complete or otherwise not useful for this request.
     */
    public Rest<T> as(String mediaType) {
        return as(MediaType.valueOf(mediaType));
    }

    /**
     * Normally you wouldn't call this: the acceptable types are determined by the readers available for the type you
     * passed to {@link #accept(Class)}. This method is only needed if you (must) know that the server would return some
     * type, but it's not complete or otherwise not useful for this request.
     */
    public Rest<T> as(MediaType mediaType) {
        return new Rest<>(config, uri, converter.limitedTo(mediaType));
    }

    public T get() {
        HttpGet get = new HttpGet(URI.create(uri.toString()));
        addHeaders(get);
        try (CloseableHttpResponse response = client.execute(get)) {
            expecting(response, OK);
            MultivaluedMap<String, String> headers = convert(response.getAllHeaders());
            return converter.convert(response.getEntity().getContent(), headers);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void expecting(CloseableHttpResponse response, Status expectedStatus) {
        if (!isStatus(response, expectedStatus))
            throw new RuntimeException("expected status " + expectedStatus.getStatusCode() + " "
                    + expectedStatus.getReasonPhrase() + " but got " + response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase());
    }

    private boolean isStatus(CloseableHttpResponse response, Status expected) {
        return response.getStatusLine().getStatusCode() == expected.getStatusCode();
    }

    private void addHeaders(HttpRequest request) {
        StringBuilder accept = new StringBuilder();
        for (MediaType mediaType : converter.mediaTypes()) {
            if (accept.length() > 0)
                accept.append(", ");
            accept.append(mediaType);
        }
        request.addHeader(ACCEPT_HEADER, accept.toString());
        log.debug("accept: {}", Arrays.toString(request.getHeaders(ACCEPT_HEADER)));
    }

    private MultivaluedMap<String, String> convert(Header[] headers) {
        MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
        for (Header header : headers)
            map.add(header.getName(), header.getValue());
        return map;
    }
}
