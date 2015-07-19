package com.github.t1.rest;

import java.net.URI;
import java.nio.file.*;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;

import com.github.t1.rest.UriTemplate.NonQuery;
import com.github.t1.rest.fallback.*;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Holds the configuration that applies to a set of {@link RestResource}s:
 * <ul>
 * <li>The {@link RestResourceRegistry} to create the base uris for {@link RestResource}</li>
 * <li>The credentials to use by base uri</li>
 * <li>The {@link RestCallFactory} to create requests</li>
 * <li>The readers to convert bodies from their {@link MediaType} to the target object</li>
 * </ul>
 * For most applications, one of these is enough, so there's a default singleton: #DEFAULT_CONFIG}.
 */
@Slf4j
public class RestConfig {
    public static final RestConfig DEFAULT_CONFIG = new RestConfig();

    private RestResourceRegistry uriRegistry = null;
    private final List<MessageBodyReader<?>> readers = new ArrayList<>();
    @Getter
    @Setter
    private RestCallFactory requestFactory = new RestCallFactory();

    @Inject
    private Instance<RestResourceRegistry> uriRegistryInstances;

    private final Map<URI, Credentials> credentials = new LinkedHashMap<>();

    public RestConfig() {
        add(new InputStreamMessageBodyReader());
        add(new JsonMessageBodyReader());
        add(new StringMessageBodyReader());
        add(new XmlMessageBodyReader());
        add(new YamlMessageBodyReader());
    }

    public RestConfig add(MessageBodyReader<?> reader) {
        readers.add(reader);
        return this;
    }

    @PostConstruct
    void postConstruct() {
        for (RestResourceRegistry registry : uriRegistryInstances) {
            add(registry);
        }
    }

    public RestConfig add(RestResourceRegistry uriRegistry) {
        if (this.uriRegistry == null)
            this.uriRegistry = uriRegistry;
        else
            this.uriRegistry = new CombinedRestResourceRegistry(uriRegistry, this.uriRegistry);
        return this;
    }

    public RestConfig register(String alias, String uri) {
        return register(alias, UriTemplate.fromString(uri));
    }

    public RestConfig register(String alias, URI uri) {
        return register(alias, UriTemplate.from(uri));
    }

    public RestConfig register(String alias, UriTemplate uri) {
        return register(alias, resource(uri));
    }

    public RestConfig register(String alias, RestResource resource) {
        this.uriRegistry = new StaticRestResourceRegistry(alias, resource, this.uriRegistry);
        return this;
    }

    public RestResource resource(String alias, String... path) {
        UriTemplate uri = uri(alias);
        if (path.length == 0)
            return resource(uri);
        return resource(nonQueryUri(alias), path);
    }

    public UriTemplate uri(String alias) {
        if (uriRegistry == null)
            throw new IllegalStateException("no uris registered when looking for alias " + alias);
        RestResource resource = uriRegistry.get(alias);
        if (resource == null)
            throw new IllegalStateException("no uri registered for resource " + alias);
        return resource.uri();
    }

    public NonQuery nonQueryUri(String alias) {
        UriTemplate uri = uri(alias);
        if (!(uri instanceof NonQuery))
            throw new IllegalArgumentException("not a non-query uri alias " + alias + ": " + uri);
        return (NonQuery) uri;
    }

    public RestResource resource(URI uri) {
        return resource(UriTemplate.from(uri));
    }

    public RestResource resource(NonQuery uri, String... path) {
        for (String item : path)
            uri = uri.path(item);
        return resource(uri);
    }

    public RestResource resource(UriTemplate uri) {
        return new RestResource(this, uri);
    }

    public <T> ResponseConverter<T> converterFor(Class<T> type) {
        ResponseConverter<T> result = new ResponseConverter<>(type);
        addReadersFor(type, result);
        return result;
    }

    public ResponseConverter<?> converterFor(Class<?> first, Class<?>... more) {
        ResponseConverter<?> result = new ResponseConverter<>(Object.class);
        addReadersFor(first, result);
        for (Class<?> m : more)
            addReadersFor(m, result);
        return result;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void addReadersFor(Class<?> type, ResponseConverter<?> result) {
        ResponseConverter<?> converter = converterFor(type, (MediaType) null);
        result.readers().putAll((Map) converter.readers());
    }

    /**
     * Normally you wouldn't call this directly: the acceptable types are determined by the readers available for the
     * type. Call this method only, if you (must) know that the server would return some content type that is not
     * complete or otherwise not useful for this request, so you need a different one.
     */
    @Deprecated
    public <T> ResponseConverter<T> converterFor(Class<T> type, MediaType contentType) {
        ResponseConverter<T> converter = new ResponseConverter<>(type);
        for (MessageBodyReader<T> reader : this.<T> readers()) {
            converter.addIfReadable(reader, contentType);
        }
        if (converter.mediaTypes().isEmpty())
            throw new IllegalArgumentException("no MessageBodyReader found for " + type);
        return converter;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> Iterable<MessageBodyReader<T>> readers() {
        return (List) readers;
    }

    public <T> RestGetCall<T> createRestGetCall(URI uri, Headers headers, ResponseConverter<T> converter) {
        Credentials credentials = getCredentials(uri);
        if (credentials != null)
            headers = headers.basicAuth(credentials);
        return requestFactory.createRestGetCall(this, uri, headers, converter);
    }

    public RestConfig put(URI baseUri, Credentials credentials) {
        this.credentials.put(baseUri, credentials);
        return this;
    }

    public Credentials getCredentials(URI uriIn) {
        URI uri = uriIn;
        while (true) {
            Credentials result = credentials.get(uri);
            if (result != null) {
                log.debug("found credentials for {}", uri);
                return result;
            } else if (uri.getFragment() != null) {
                uri = removeTrailing(uri, "#" + uri.getFragment());
            } else if (uri.getQuery() != null) {
                uri = removeTrailing(uri, "?" + uri.getQuery());
            } else if (uri.toString().endsWith("/")) {
                uri = removeTrailing(uri, "/");
            } else if (uri.getPath() != null && !uri.getPath().isEmpty()) {
                Path path = Paths.get(uri.getPath());
                path = path.getName(path.getNameCount() - 1);
                uri = removeTrailing(uri, "/" + path);
            } else {
                break;
            }
        }
        log.debug("found no credentials for {}", uriIn);
        return null;
    }

    private URI removeTrailing(URI uri, String toBeRemoved) {
        String string = uri.toString();
        assert string.endsWith(toBeRemoved) : "uri should end with " + toBeRemoved + " but was " + uri;
        string = string.substring(0, string.length() - toBeRemoved.length());
        return URI.create(string);
    }

    @Override
    public String toString() {
        return "config" + ((uriRegistry == null) ? "(no aliases)" : "(" + uriRegistry.names() + ")");
    }
}
