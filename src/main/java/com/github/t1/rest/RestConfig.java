package com.github.t1.rest;

import java.net.URI;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import com.github.t1.rest.UriTemplate.NonQuery;
import com.github.t1.rest.fallback.*;

/**
 * Holds the configuration that applies to a set of {@link RestResource}s:
 * <ul>
 * <li>The {@link RestResource resources} and {@link RestResourceRegistry resource registries} to lookup resources by alias</li>
 * <li>The {@link Credentials} and {@link CredentialsRegistry credential registries} to lookup by base uri</li>
 * <li>The {@link RestCallFactory} to create requests</li>
 * <li>The readers to convert bodies from their {@link MediaType} to the target object</li>
 * </ul>
 * For most applications, one of these is enough, so there's a default config: {@link #DEFAULT_CONFIG}.
 * 
 * FIXME make config immutable
 */
@Slf4j
public class RestConfig {
    public static final RestConfig DEFAULT_CONFIG = new RestConfig();

    private final List<MessageBodyReader<?>> readers = new ArrayList<>();

    @Getter
    @Setter
    private RestCallFactory requestFactory = new RestCallFactory();

    private RestResourceRegistry uriRegistry = null;
    @Inject
    private Instance<RestResourceRegistry> uriRegistryInstances;

    private CredentialsRegistry credentialsRegistry = null;

    @Inject
    private Instance<CredentialsRegistry> credentialsRegistryInstances;

    public RestConfig() {
        add(new ByteArrayMessageBodyReader());
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
    void loadRegistries() {
        for (RestResourceRegistry registry : uriRegistryInstances) {
            add(registry);
        }
        for (CredentialsRegistry credentialsRegistry : credentialsRegistryInstances) {
            add(credentialsRegistry);
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
        return register(alias, createResource(uri));
    }

    public RestConfig register(String alias, RestResource resource) {
        this.uriRegistry = new StaticRestResourceRegistry(alias, resource, this.uriRegistry);
        return this;
    }

    public RestResource resource(String alias, String... path) {
        UriTemplate uri = uri(alias);
        if (path.length == 0)
            return createResource(uri);
        return createResource(nonQueryUri(alias), path);
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

    public RestResource createResource(URI uri) {
        return createResource(UriTemplate.from(uri));
    }

    public RestResource createResource(NonQuery uri, String... path) {
        for (String item : path)
            uri = uri.path(item);
        return createResource(uri);
    }

    public RestResource createResource(UriTemplate uri) {
        return new RestResource(this, uri);
    }


    public RestConfig add(CredentialsRegistry credentialsRegistry) {
        if (this.credentialsRegistry == null)
            this.credentialsRegistry = credentialsRegistry;
        else
            this.credentialsRegistry = new CombinedCredentialsRegistry(credentialsRegistry, this.credentialsRegistry);
        return this;
    }

    public RestConfig put(URI uri, Credentials credentials) {
        this.credentialsRegistry = new StaticCredentialsRegistry(uri, credentials, credentialsRegistry);
        return this;
    }

    public Credentials getCredentials(URI uri) {
        Credentials credentials = (credentialsRegistry == null) ? null : credentialsRegistry.lookup(uri);
        if (credentials == null)
            log.debug("found no credentials for {}", uri);
        else
            log.debug("found credentials for {}", uri);
        return credentials;
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

    @Override
    public String toString() {
        return "config" + ((uriRegistry == null) ? "(no aliases)" : "(" + uriRegistry.names() + ")");
    }
}
