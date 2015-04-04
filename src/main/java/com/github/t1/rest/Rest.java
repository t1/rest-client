package com.github.t1.rest;

import static java.util.Arrays.*;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import lombok.*;

import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;

import com.github.t1.rest.UriTemplate.NonFragment;
import com.github.t1.rest.UriTemplate.UriPath;

/**
 * Immutable, fluent, strictly appendable client for RESTful web services.
 * <p/>
 * A REST call consists of these parts:
 * <ol>
 * <li>The configuration used for the request (esp. timeouts)</li>
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
public class Rest<T> implements Cloneable {
    private static final CloseableHttpClient client = HttpClients.createDefault();
    private static final List<String> ALLOWED_SCHEMES = asList("http", "https");

    private NonFragment uri;

    public Rest(URI uri) {
        this(uri.toString());
    }

    public Rest(String uri) {
        this.uri = check(UriTemplate.fromString(uri));
    }

    private NonFragment check(UriTemplate uri) {
        if (!ALLOWED_SCHEMES.contains(uri.scheme()))
            throw new RuntimeException("unsupported scheme for REST: " + uri.scheme());
        return (NonFragment) uri;
    }

    @Override
    @SuppressWarnings("unchecked")
    @SneakyThrows(CloneNotSupportedException.class)
    protected Rest<T> clone() {
        return (Rest<T>) super.clone();
    }

    public Rest<T> path(String path) {
        Rest<T> clone = clone();
        clone.uri = ((UriPath) this.uri).path(path);
        return clone;
    }

    @SuppressWarnings("unchecked")
    public <U> Rest<U> accept(Class<U> type) {
        // TODO find and set accept header
        return (Rest<U>) this;
    }

    public T get() {
        HttpGet get = new HttpGet(URI.create(uri.toString()));
        try (CloseableHttpResponse response = client.execute(get)) {
            return (T) EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
