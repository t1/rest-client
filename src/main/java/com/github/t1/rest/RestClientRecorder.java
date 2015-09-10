package com.github.t1.rest;

import static com.github.t1.rest.RestContext.*;
import static java.nio.charset.StandardCharsets.*;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for record and play back REST requests and their responses. Could be a JUnit rule, but we don't want to
 * have test dependencies at runtime.
 * 
 * @see RestClientMocker
 */
@Slf4j
public class RestClientRecorder {
    private static final Path DEFAULT_FOLDER = Paths.get("src/test/resources");
    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    @Data
    @RequiredArgsConstructor
    private static class Recordings {
        private static final CollectionType RECORDING_LIST =
                MAPPER.getTypeFactory().constructCollectionType(List.class, Recording.class);

        public static Recordings load(Path folder, String authority) {
            Path file = folder.resolve(authority);
            return new Recordings(file).load();
        }

        private final Path file;
        private List<Recording> recordings;

        @SneakyThrows(IOException.class)
        private Recordings load() {
            if (Files.exists(file))
                try (BufferedReader reader = Files.newBufferedReader(file, UTF_8)) {
                    this.recordings = MAPPER.readValue(reader, RECORDING_LIST);
                }
            else
                recordings = new ArrayList<>();
            return this;
        }

        public Recordings addOrReplace(Recording recording) {
            Iterator<Recording> iter = recordings.iterator();
            while (iter.hasNext()) {
                Recording existing = iter.next();
                if (existing.matches(recording))
                    iter.remove();
            }
            this.recordings.add(recording);
            return this;
        }

        @SneakyThrows(IOException.class)
        public void write() {
            try (BufferedWriter writer = Files.newBufferedWriter(file, UTF_8)) {
                MAPPER.writeValue(writer, recordings);
            }
        }

        public Recording find(URI uri, Headers requestHeaders) {
            for (Recording recording : recordings)
                if (recording.matches(uri, requestHeaders, null))
                    return recording;
            return null;
        }
    }

    @Data
    private static class Recording {
        @JsonProperty
        URI uri;

        @JsonProperty
        Headers requestHeaders;
        @JsonProperty
        String requestBody;

        @JsonProperty
        Status responseStatus;
        @JsonProperty
        Headers responseHeaders;
        @JsonProperty
        String responseBody;

        public boolean matches(Recording that) {
            return matches(that.uri, that.requestHeaders, that.requestBody);
        }

        public boolean matches(URI uri, Headers requestHeaders, String requestBody) {
            return this.uri.equals(uri) //
                    && this.requestHeaders.equals(requestHeaders) //
                    && Objects.equals(this.requestBody, requestBody);
        }
    }

    private class RestGetCallDecorator<T> extends RestGetCall<T> {
        private final RestGetCall<T> delegate;

        public RestGetCallDecorator(RestGetCall<T> delegate) {
            super(delegate.context(), delegate.uri(), delegate.requestHeaders(), null, delegate.converter());
            this.delegate = delegate;
        }

        @Override
        public EntityResponse<T> execute() {
            return delegate.execute();
        }

        protected Recordings recordings() {
            return Recordings.load(folder, uri().getAuthority());
        }
    }

    private class RecorderRestGetCall<T> extends RestGetCallDecorator<T> {
        public RecorderRestGetCall(RestGetCall<T> delegate) {
            super(delegate);
        }

        @Override
        public EntityResponse<T> execute() {
            Recording recording = new Recording();
            recording.uri(uri()).requestHeaders(requestHeaders());

            EntityResponse<T> response = super.execute();

            recording.responseStatus((Status) response.status()); // not perfect, but StatusType can't be deserialized
            recording.responseHeaders(response.headers());
            recording.responseBody(response.get(String.class));

            recordings().addOrReplace(recording).write();

            return response;
        }
    }

    private class PlaybackRestGetCall<T> extends RestGetCallDecorator<T> {
        public PlaybackRestGetCall(RestGetCall<T> delegate) {
            super(delegate);
        }

        @Override
        public EntityResponse<T> execute() {
            Recording recording = recordings().find(uri(), requestHeaders());
            if (recording == null)
                return super.execute();
            return new EntityResponse<>(context(), recording.responseStatus(), recording.responseHeaders(), converter(),
                    recording.responseBody().getBytes());
        }
    }

    public RestCallFactory requestFactoryMock = new RestCallFactory() {
        @Override
        public <T> RestGetCall<T> createRestGetCall(RestContext context, URI uri, Headers headers,
                ResponseConverter<T> converter) {
            log.info("forward request of {} to real target", uri);
            return new PlaybackRestGetCall<>( //
                    new RecorderRestGetCall<>( //
                            originalRequestFactory.createRestGetCall(context, uri, headers, converter)));
        }
    };

    @Getter
    private final RestContext context;
    private final Path folder;
    private final RestCallFactory originalRequestFactory;

    public RestClientRecorder() {
        this(REST);
    }

    public RestClientRecorder(RestContext context) {
        this(context, DEFAULT_FOLDER);
    }

    public RestClientRecorder(Path folder) {
        this(REST, folder);
    }

    public RestClientRecorder(RestContext context, Path folder) {
        this.originalRequestFactory = context.restCallFactory();
        this.context = context.restCallFactory(requestFactoryMock);
        this.folder = folder;
    }
}
