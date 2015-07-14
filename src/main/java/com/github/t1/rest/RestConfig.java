package com.github.t1.rest;

import java.net.URI;
import java.nio.file.*;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;

import com.github.t1.rest.fallback.*;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Holds the configuration that applies to a set of {@link RestResource}s:
 * <ul>
 * <li>The {@link ResourceFactory} to create the base uris for {@link RestResource}</li>
 * <li>The credentials to use by base uri</li>
 * <li>The {@link RequestFactory} to create requests</li>
 * <li>The readers to convert bodies from their {@link MediaType} to the target object</li>
 * </ul>
 * For most applications, one of these is enough, so there's a default singleton: #DEFAULT_CONFIG}.
 */
@Slf4j
public class RestConfig {
    public static final RestConfig DEFAULT_CONFIG = new RestConfig();

    private final List<ResourceFactory> resourceFactories = new ArrayList<>();
    private final List<MessageBodyReader<?>> readers = new ArrayList<>();
    @Getter
    @Setter
    private RequestFactory requestFactory = new RequestFactory();

    @Inject
    Instance<ResourceFactory> resourceFactoryInstances;

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
        for (ResourceFactory factory : resourceFactoryInstances) {
            add(factory);
        }
    }

    public RestConfig add(ResourceFactory resourceFactory) {
        this.resourceFactories.add(resourceFactory);
        return this;
    }

    public RestResource resource(String name) {
        for (ResourceFactory resourceFactory : resourceFactories) {
            RestResource resource = resourceFactory.forName(name);
            if (resource != null)
                return resource;
        }
        throw new IllegalStateException("no uri factory for resource named '" + name + "'");
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
        ResponseConverter<T> out = new ResponseConverter<>(type);
        for (MessageBodyReader<T> bean : this.<T> readers()) {
            out.addIfReadable(bean, contentType);
        }
        if (out.mediaTypes().isEmpty())
            throw new IllegalArgumentException("no MessageBodyReader found for " + type);
        return out;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> Iterable<MessageBodyReader<T>> readers() {
        return (List) readers;
    }

    public <T> GetRequest<T> createGetRequest(URI uri, Headers headers, ResponseConverter<T> converter) {
        Credentials credentials = getCredentials(uri);
        if (credentials != null)
            headers = headers.basicAuth(credentials);
        return requestFactory.createGetRequest(this, uri, headers, converter);
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
}
