package com.github.t1.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.t1.rest.fallback.JsonMessageBodyReader;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.GET;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.*;

import static com.github.t1.rest.RestContext.*;
import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;

/**
 * Utility class for programmatically simulating REST requests and their responses. Could be a JUnit rule, but we don't
 * want to have test dependencies at runtime.
 *
 * @see RestClientRecorder
 */
@Slf4j
public class RestClientMocker {
    @RequiredArgsConstructor
    public class ResourceMock {
        private final UriTemplate uri;

        public RequestMock GET() {
            RequestMock requestMock = new RequestMock();
            RequestMock oldMock = mockedUris.put(uri.toUri(), requestMock);
            if (oldMock != null)
                log.warn("replaced {} for {}", oldMock, uri);
            return requestMock;
        }
    }

    public static class RequestMock {
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

        public <T> EntityRestCall<T> createGetRequest(final RestContext context, URI uri, final Headers requestHeaders,
                final ResponseConverter<T> converter) {
            return new EntityRestCall<T>(context, GET.class, uri, requestHeaders, null, converter) {
                private Headers responseHeaders = new Headers();

                @Override
                public EntityResponse<T> execute() {
                    if (requiredBasicAuthCredentials != null)
                        if (!requestHeaders.isBasicAuth(requiredBasicAuthCredentials))
                            return response(UNAUTHORIZED, body(""));
                    return response(OK, body(object));
                }

                private EntityResponse<T> response(Status status, byte[] body) {
                    return new EntityResponse<>(context, status, new Headers(), converter, body);
                }

                @SneakyThrows(JsonProcessingException.class)
                private byte[] body(Object object) {
                    if (object instanceof String && requestHeaders.accepts(TEXT_PLAIN_TYPE)) {
                        contentType(TEXT_PLAIN_TYPE);
                        return ((String) object).getBytes();
                    }
                    if (requestHeaders.accepts(APPLICATION_JSON_TYPE)) {
                        contentType(APPLICATION_JSON_TYPE);
                        return JsonMessageBodyReader.MAPPER.writeValueAsBytes(object);
                    }
                    throw new UnsupportedOperationException("the mock can't yet convert to any of " //
                            + requestHeaders.accept());
                }

                private void contentType(MediaType type) {
                    responseHeaders = responseHeaders.contentType(type);
                }
            };
        }

        @Override
        public String toString() {
            return "request-mock" + ((requiredBasicAuthCredentials == null) ? "" : " requiring auth") //
                    + " returning " + object;
        }
    }

    @Getter
    private final RestContext context;
    private final Map<URI, RequestMock> mockedUris = new LinkedHashMap<>();
    public RestCallFactory requestFactoryMock = new RestCallFactory() {
        @Override
        public <T, M extends Annotation> EntityRestCall<T> createRestCall(Class<M> method, RestContext context, URI uri,
                Headers headers, ResponseConverter<T> converter) {
            if (!mockedUris.containsKey(uri))
                throw new IllegalArgumentException("unmocked createGetRequest on " + uri + "\n" //
                        + "only know: " + mockedUris.keySet());
            RequestMock requestMock = mockedUris.get(uri);
            return requestMock.createGetRequest(context, uri, headers, converter);
        }
    };

    public RestClientMocker() {
        this(REST);
    }

    public RestClientMocker(RestContext context) {
        this.context = context.restCallFactory(requestFactoryMock);
    }

    public ResourceMock on(String uri) {
        return on(UriTemplate.fromString(uri));
    }

    public ResourceMock on(URI uri) {
        return on(UriTemplate.from(uri));
    }

    public ResourceMock on(UriTemplate uri) {
        return new ResourceMock(uri);
    }
}
