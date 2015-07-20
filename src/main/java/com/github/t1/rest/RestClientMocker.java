package com.github.t1.rest;

import static com.github.t1.rest.RestConfig.*;
import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;

import java.io.*;
import java.net.URI;
import java.util.*;

import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;

import lombok.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.t1.rest.fallback.JsonMessageBodyReader;

public class RestClientMocker {
    @RequiredArgsConstructor
    public class ResourceMock {
        private final UriTemplate uri;

        public RequestMock GET() {
            RequestMock requestMock = new RequestMock();
            mockedUris.put(uri.toUri(), requestMock);
            return requestMock;
        }
    }

    public class RequestMock {
        private Credentials requiredBasicAuthCredentials;
        private Object object;

        public void respond(Object object) {
            this.object = object;
        }

        public RequestMock requireBasicAuth(String username, String password) {
            return requireBasicAuth(new Credentials(username, password));
        }

        public RequestMock requireBasicAuth(Credentials credentials) {
            this.requiredBasicAuthCredentials = credentials;
            return this;
        }

        public <T> RestGetCall<T> createGetRequest(final RestConfig config, URI uri, final Headers requestHeaders,
                final ResponseConverter<T> converter) {
            return new RestGetCall<T>(config, null, uri, requestHeaders, converter) {
                private Headers responseHeaders = new Headers();

                @Override
                public EntityResponse<T> execute() {
                    if (requiredBasicAuthCredentials != null)
                        if (!requestHeaders.isBasicAuth(requiredBasicAuthCredentials))
                            return response(UNAUTHORIZED, stream(""));
                    return response(OK, stream(object));
                }

                private EntityResponse<T> response(Status status, InputStream inputStream) {
                    return new EntityResponse<>(config, status, new Headers(), converter, inputStream, inputStream);
                }

                @SneakyThrows(JsonProcessingException.class)
                private ByteArrayInputStream stream(Object object) {
                    if (object instanceof String && requestHeaders.accepts(TEXT_PLAIN_TYPE)) {
                        contentType(TEXT_PLAIN_TYPE);
                        return new ByteArrayInputStream(((String) object).getBytes());
                    }
                    if (requestHeaders.accepts(APPLICATION_JSON_TYPE)) {
                        contentType(APPLICATION_JSON_TYPE);
                        return new ByteArrayInputStream(JsonMessageBodyReader.MAPPER.writeValueAsBytes(object));
                    }
                    throw new UnsupportedOperationException("the mock can't yet convert to any of " //
                            + requestHeaders.accept());
                }

                private void contentType(MediaType type) {
                    responseHeaders = responseHeaders.contentType(type);
                }
            };
        }
    }

    private final RestConfig config;
    private RestCallFactory originalRequestFactory;
    private final Map<URI, RequestMock> mockedUris = new LinkedHashMap<>();
    public RestCallFactory requestFactoryMock = new RestCallFactory() {
        @Override
        public <T> RestGetCall<T> createRestGetCall(RestConfig config, URI uri, Headers headers,
                ResponseConverter<T> converter) {
            if (!mockedUris.containsKey(uri))
                throw new IllegalArgumentException("unmocked createGetRequest on " + uri + "\n" //
                        + "only know: " + mockedUris.keySet());
            RequestMock requestMock = mockedUris.get(uri);
            return requestMock.createGetRequest(config, uri, headers, converter);
        }
    };

    private boolean setup = false;

    public RestClientMocker() {
        this(DEFAULT_CONFIG);
    }

    public RestClientMocker(RestConfig config) {
        this.config = config;
    }

    public void before() {
        this.originalRequestFactory = config.requestFactory();
        config.requestFactory(requestFactoryMock);
        setup = true;
    }

    public void after() {
        config.requestFactory(originalRequestFactory);
    }

    public ResourceMock on(String uri) {
        return on(UriTemplate.fromString(uri));
    }

    public ResourceMock on(URI uri) {
        return on(UriTemplate.from(uri));
    }

    public ResourceMock on(UriTemplate uri) {
        if (!setup)
            throw new RuntimeException(RestClientMocker.class.getSimpleName()
                    + " not properly set up: call #before() and #after()!");
        return new ResourceMock(uri);
    }
}
