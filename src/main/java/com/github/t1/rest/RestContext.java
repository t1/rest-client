package com.github.t1.rest;

import java.net.URI;
import java.util.*;

import javax.annotation.concurrent.Immutable;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;

import org.jboss.weld.exceptions.UnsupportedOperationException;

import com.github.t1.rest.UriTemplate.NonQuery;
import com.github.t1.rest.fallback.*;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Holds the configuration that applies to a set of {@link RestResource}s:
 * <ul>
 * <li>The {@link RestResource resources} and {@link RestResourceRegistry resource registries} to lookup resources by
 * alias</li>
 * <li>The {@link Credentials} and {@link CredentialsRegistry credential registries} to lookup by base uri</li>
 * <li>The {@link RestCallFactory} to create requests</li>
 * <li>The readers to convert bodies from their {@link MediaType} to the target object</li>
 * </ul>
 * For most applications, one of these is enough, so there's a default config: {@link #REST}.
 */
@Slf4j
@Immutable
public class RestContext {
    private static final MessageBodyReaders DEFAULT_MESSAGE_BODY_READERS = MessageBodyReaders //
            .of(new StringMessageBodyReader()) //
            .and(new ByteArrayMessageBodyReader()) //
            .and(new InputStreamMessageBodyReader()) //
            .and(new YamlMessageBodyReader()) //
            .and(new XmlMessageBodyReader()) //
            .and(new JsonMessageBodyReader()) //
            ;

    public static final RestContext REST = new RestContext();

    @Immutable
    @Value
    private static class MessageBodyReaders implements Iterable<MessageBodyReader<?>> {
        public static MessageBodyReaders of(MessageBodyReader<?> head) {
            return new MessageBodyReaders(head, null);
        }

        MessageBodyReader<?> head;
        MessageBodyReaders tail;

        public MessageBodyReaders and(MessageBodyReader<?> reader) {
            return new MessageBodyReaders(reader, this);
        }

        @Override
        public Iterator<MessageBodyReader<?>> iterator() {
            return new Iterator<MessageBodyReader<?>>() {
                private MessageBodyReaders readers = MessageBodyReaders.this;

                @Override
                public boolean hasNext() {
                    return readers != null;
                }

                @Override
                public MessageBodyReader<?> next() {
                    MessageBodyReader<?> head = readers.head;
                    readers = readers.tail;
                    return head;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    private final MessageBodyReaders readers;
    @Getter
    private final RestCallFactory restCallFactory;
    private final RestResourceRegistry restResourceRegistry;
    private final CredentialsRegistry credentialsRegistry;


    private RestContext() {
        this(DEFAULT_MESSAGE_BODY_READERS, new RestCallFactory(), null, null);
    }

    /** for the builders */
    private RestContext(MessageBodyReaders readers, RestCallFactory restCallFactory,
            RestResourceRegistry restResourceRegistry, CredentialsRegistry credentialsRegistry) {
        this.readers = readers;
        this.restCallFactory = restCallFactory;
        this.restResourceRegistry = restResourceRegistry;
        this.credentialsRegistry = credentialsRegistry;
    }

    /** for CDI */
    @Inject
    private RestContext(Instance<RestResourceRegistry> restResourceRegistryInstances,
            Instance<CredentialsRegistry> credentialsRegistryInstances) {
        this.readers = DEFAULT_MESSAGE_BODY_READERS;
        this.restCallFactory = new RestCallFactory();
        this.restResourceRegistry = CombinedRestResourceRegistry.combine(restResourceRegistryInstances);
        this.credentialsRegistry = CombinedCredentialsRegistry.combine(credentialsRegistryInstances);
    }

    public RestContext and(MessageBodyReader<?> reader) {
        return new RestContext(new MessageBodyReaders(reader, readers), restCallFactory, restResourceRegistry,
                credentialsRegistry);
    }

    public <T> ResponseConverter<T> converterFor(Class<T> type) {
        return converterFor(type, (MediaType) null);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> ResponseConverter<T> converterFor(Class<?> first, Class<?>... more) {
        ResponseConverter<T> result = converterFor((Class<T>) first, (MediaType) null);
        for (Class<?> m : more) {
            ResponseConverter<?> converter = converterFor(m, (MediaType) null);
            result.readers().putAll((Map) converter.readers());
        }
        return result;
    }

    /**
     * Normally you wouldn't call this directly: the acceptable types are determined by the readers available for the
     * type. Call this method only, if you (must) know that the server would return some content type that is not
     * complete or otherwise not useful for this request, so you need a different one.
     */
    @Deprecated
    public <T> ResponseConverter<T> converterFor(Class<T> type, MediaType contentType) {
        ResponseConverter<T> converter = new ResponseConverter<>(type);
        for (MessageBodyReader<T> reader : this.<T> readers())
            converter.addIfReadable(reader, contentType);
        if (converter.mediaTypes().isEmpty())
            throw new IllegalArgumentException("no MessageBodyReader found for " + type);
        return converter;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> Iterable<MessageBodyReader<T>> readers() {
        return (Iterable) readers;
    }


    public RestContext restCallFactory(RestCallFactory restCallFactory) {
        return new RestContext(readers, restCallFactory, restResourceRegistry, credentialsRegistry);
    }

    public <T> RestGetCall<T> createRestGetCall(URI uri, Headers headers, Class<T> acceptedType) {
        Credentials credentials = getCredentials(uri);
        if (credentials != null)
            headers = headers.basicAuth(credentials);
        ResponseConverter<T> converter = converterFor(acceptedType);
        return restCallFactory.createRestGetCall(this, uri, headers, converter);
    }


    public RestContext register(String alias, String uri) {
        return register(alias, UriTemplate.fromString(uri));
    }

    public RestContext register(String alias, URI uri) {
        return register(alias, UriTemplate.from(uri));
    }

    public RestContext register(String alias, UriTemplate uri) {
        return register(alias, createResource(uri));
    }

    public RestContext register(String alias, RestResource resource) {
        return new RestContext(readers, restCallFactory,
                new StaticRestResourceRegistry(alias, resource, this.restResourceRegistry), credentialsRegistry);
    }

    public RestResource resource(String alias, String... path) {
        UriTemplate uri = uri(alias);
        if (path.length == 0)
            return createResource(uri);
        return createResource(nonQueryUri(alias), path);
    }

    public UriTemplate uri(String alias) {
        if (restResourceRegistry == null)
            throw new IllegalStateException("no uris registered when looking for alias " + alias);
        RestResource resource = restResourceRegistry.get(alias);
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

    public RestResource createResource(String uri) {
        return createResource(UriTemplate.fromString(uri));
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


    public RestContext register(URI uri, Credentials credentials) {
        return new RestContext(readers, restCallFactory, restResourceRegistry,
                new StaticCredentialsRegistry(uri, credentials, credentialsRegistry));
    }

    public Credentials getCredentials(URI uri) {
        Credentials credentials = (credentialsRegistry == null) ? null : credentialsRegistry.lookup(uri);
        if (credentials == null)
            log.debug("found no credentials for {}", uri);
        else
            log.debug("found credentials for {}", uri);
        return credentials;
    }


    @Override
    public String toString() {
        return "config" //
                + "(" + ((restResourceRegistry == null) ? "no aliases" : restResourceRegistry.names()) + ")" //
                + "(" + ((credentialsRegistry == null) ? "no credentials" : credentialsRegistry.uris()) + ")" //
                ;
    }
}
