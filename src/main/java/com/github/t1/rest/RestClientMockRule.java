package com.github.t1.rest;

import static com.github.t1.rest.RestConfig.*;
import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;

import javax.ws.rs.core.MediaType;

import org.junit.rules.ExternalResource;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.t1.rest.fallback.JsonMessageBodyReader;

import lombok.*;

public class RestClientMockRule extends ExternalResource {
    @RequiredArgsConstructor
    public static class RequestMock {
        private final GetRequest<?> request;

        protected Headers headers = new Headers();

        public void reply(final Object object) {
            when(request.execute()).then(new Answer<EntityResponse<Object>>() {
                @Override
                public EntityResponse<Object> answer(InvocationOnMock invocation) {
                    ResponseConverter<Object> converter = converter(object);
                    InputStream inputStream = stream(object);
                    return new EntityResponse<>(null, OK, headers, converter, inputStream);
                }

                @SneakyThrows(JsonProcessingException.class)
                private ByteArrayInputStream stream(Object object) {
                    return new ByteArrayInputStream(JsonMessageBodyReader.MAPPER.writeValueAsBytes(object));
                }

                private ResponseConverter<Object> converter(final Object object) {
                    @SuppressWarnings("unchecked")
                    ResponseConverter<Object> converter =
                            (ResponseConverter<Object>) new ResponseConverter<>(object.getClass());
                    converter.addIfReadable(new JsonMessageBodyReader() {
                        @Override
                        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations,
                                MediaType mediaType) {
                            return true;
                        }
                    }, APPLICATION_JSON_TYPE);
                    return converter;
                }
            });
        }
    }

    private final RestConfig config;
    private RequestFactory originalRequestFactory;
    public RequestFactory requestFactoryMock = mock(RequestFactory.class);

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
        return on(URI.create(uri));
    }

    @SuppressWarnings("unchecked")
    public RequestMock on(URI uri) {
        GetRequest<?> getRequest = mock(GetRequest.class);
        when(requestFactoryMock.createGetRequest(any(RestConfig.class), eq(uri), any(Headers.class), //
                any(ResponseConverter.class))).thenReturn(getRequest);
        when(getRequest.config()).thenReturn(config);
        return new RequestMock(getRequest);
    }
}
