package com.github.t1.rest;

import static javax.ws.rs.core.Response.Status.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.net.URI;

import org.junit.rules.ExternalResource;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import lombok.RequiredArgsConstructor;

public class RestClientMockRule extends ExternalResource {
    @RequiredArgsConstructor
    public static class RequestMock {
        private final GetRequest<?> request;
        protected Headers headers = new Headers();

        @SuppressWarnings("unchecked")
        public void reply(final Object object) {
            when(request.execute()).then(new Answer<EntityResponse<Object>>() {
                @Override
                public EntityResponse<Object> answer(InvocationOnMock invocation) {
                    ResponseConverter<Object> responseConverter = invocation.getArgumentAt(0, ResponseConverter.class);
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(object.toString().getBytes());
                    return new EntityResponse<>(null, OK, headers, responseConverter, inputStream);
                }
            });
        }
    }

    private RequestFactory originalRequestFactory;
    public RequestFactory requestFactoryMock = mock(RequestFactory.class);

    @Override
    protected void before() {
        // FIXME
        // this.originalRequestFactory = EntityRequest.CONFIG.requestFactory();
        // EntityRequest.CONFIG.requestFactory(requestFactoryMock);
    }

    @Override
    protected void after() {
        // EntityRequest.CONFIG.requestFactory(originalRequestFactory);
    }

    public RequestMock on(String uri) {
        return on(URI.create(uri));
    }

    @SuppressWarnings("unchecked")
    public RequestMock on(URI uri) {
        GetRequest<?> getRequest = mock(GetRequest.class);
        when(requestFactoryMock.createGetRequest(any(RestConfig.class), eq(uri), any(Headers.class), //
                any(ResponseConverter.class))).thenReturn(getRequest);
        return new RequestMock(getRequest);
    }
}
