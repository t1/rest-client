package com.github.t1.rest;

import static com.github.t1.rest.RestConfig.*;
import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.*;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.junit.rules.ExternalResource;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.t1.rest.fallback.JsonMessageBodyReader;

import lombok.*;

public class RestClientMockRule extends ExternalResource {
    @RequiredArgsConstructor
    public class RequestMock {
        private final UriTemplate uri;

        protected Headers requestHeaders;
        protected ResponseConverter<Object> responseConverter;

        private Credentials requiredBasicAuthCredentials;

        public void GET(final Object object) {
            final GetRequest<?> getRequest = mock(GetRequest.class);
            mockCreateGetRequest(getRequest);
            when(getRequest.config()).thenReturn(config);
            when(getRequest.execute()).then(new Answer<EntityResponse<Object>>() {
                private Headers responseHeaders;

                @Override
                public EntityResponse<Object> answer(InvocationOnMock invocation) {
                    this.responseHeaders = new Headers();
                    if (requiredBasicAuthCredentials != null)
                        if (!requestHeaders.isBasicAuth(requiredBasicAuthCredentials))
                            return response(UNAUTHORIZED, stream(""));
                    return response(OK, stream(object));
                }

                private EntityResponse<Object> response(Status status, InputStream inputStream) {
                    return new EntityResponse<>(null, status, responseHeaders, responseConverter, inputStream);
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
            });
        }

        @SuppressWarnings("unchecked")
        private void mockCreateGetRequest(final GetRequest<?> getRequest) {
            RestClientMockRule.this.mockedUris.add(uri);
            when(requestFactoryMock.createGetRequest(any(RestConfig.class), eq(uri.toUri()), any(Headers.class), //
                    any(ResponseConverter.class))).thenAnswer(new Answer<GetRequest<?>>() {
                        @Override
                        public GetRequest<?> answer(InvocationOnMock invocation) {
                            if (invocation.getArgumentAt(0, RestConfig.class) != RestClientMockRule.this.config)
                                throw new AssertionError("called mock with wrong config");
                            requestHeaders = invocation.getArgumentAt(2, Headers.class);
                            responseConverter = invocation.getArgumentAt(3, ResponseConverter.class);
                            return getRequest;
                        }
                    });
        }

        public RequestMock requireBasicAuth(String username, String password) {
            return requireBasicAuth(new Credentials(username, password));
        }

        public RequestMock requireBasicAuth(Credentials credentials) {
            this.requiredBasicAuthCredentials = credentials;
            return this;
        }
    }

    private final RestConfig config;
    private RequestFactory originalRequestFactory;
    private final Set<UriTemplate> mockedUris = new LinkedHashSet<>();
    public RequestFactory requestFactoryMock =
            mock(RequestFactory.class, withSettings().defaultAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) {
                    if (isStubbing(invocation))
                        return null;
                    assert invocation.getMethod().equals(createGetRequestMethod());
                    throw new IllegalArgumentException(
                            "unmocked createGetRequest on " + invocation.getArgumentAt(1, URI.class) + "\n"//
                                    + "only know: " + mockedUris);
                }

                private Method createGetRequestMethod() {
                    try {
                        return RequestFactory.class.getMethod("createGetRequest", RestConfig.class, URI.class,
                                Headers.class, ResponseConverter.class);
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                }

                private boolean isStubbing(InvocationOnMock invocation) {
                    for (Object argument : invocation.getArguments())
                        if (argument != null)
                            return false;
                    return true;
                }
            }));

    public RestClientMockRule() {
        this(DEFAULT_CONFIG);
    }

    public RestClientMockRule(RestConfig config) {
        this.config = config;
    }

    @Override
    protected void before() {
        this.originalRequestFactory = config.requestFactory();
        config.requestFactory(requestFactoryMock);
    }

    @Override
    protected void after() {
        config.requestFactory(originalRequestFactory);
    }

    public RequestMock on(String uri) {
        return on(UriTemplate.fromString(uri));
    }

    public RequestMock on(URI uri) {
        return on(UriTemplate.from(uri));
    }

    public RequestMock on(UriTemplate uri) {
        return new RequestMock(uri);
    }
}
