package com.github.t1.rest;

import static java.util.Arrays.*;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import lombok.Getter;

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
@Getter
public class Rest<T> {
    private static final CloseableHttpClient client = HttpClients.createDefault();
    private static final List<String> ALLOWED_SCHEMES = asList("http", "https");

    private final RestConfig config;
    private final NonFragment uri;
    private RestBodyReader<T> converter;

    public Rest(RestConfig config, String uri) {
        this(config, check(UriTemplate.fromString(uri)), null);
    }

    private Rest(RestConfig config, NonFragment uri, RestBodyReader<T> converter) {
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
        RestBodyReader<U> converter = config.converterFor(acceptedType);
        return new Rest<>(config, uri, converter);
    }

    public T get() {
        HttpGet get = new HttpGet(URI.create(uri.toString()));
        try (CloseableHttpResponse response = client.execute(get)) {
            return converter.convert(response.getEntity().getContent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
