package com.github.t1.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.Response.Status;
import java.io.*;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

import static com.github.t1.rest.RestContext.*;
import static java.nio.charset.StandardCharsets.*;

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
    public static class Recordings {
        private static final CollectionType RECORDING_LIST =
                MAPPER.getTypeFactory().constructCollectionType(List.class, Recording.class);

        private static Map<String, Recordings> CACHE = new HashMap<>();

        public static Recordings get(Path folder, String authority) {
            Recordings result = CACHE.get(authority);
            if (result == null) {
                Path file = (folder == null) ? null : folder.resolve(authority);
                log.debug("create/load recordings for {} -> {}", authority, file);
                result = new Recordings(file).load();
                CACHE.put(authority, result);
            }
            return result;
        }

        public static void clearAll() {
            CACHE.keySet().forEach(Recordings::clear);
        }

        @SneakyThrows(IOException.class)
        public static void clear(String authority) {
            Recordings existing = CACHE.remove(authority);
            if (existing != null && existing.file != null) {
                log.debug("clear recordings for {}", authority);
                Files.delete(existing.file);
            }
        }

        private final Path file;
        private List<Recording> recordings;

        @SneakyThrows(IOException.class)
        private Recordings load() {
            if (file != null && Files.exists(file))
                try (BufferedReader reader = Files.newBufferedReader(file, UTF_8)) {
                    log.debug("loaded recordings from {}", file);
                    this.recordings = MAPPER.readValue(reader, RECORDING_LIST);
                    log.debug("loaded {} recordings", recordings.size());
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
            if (file != null)
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
            return this.uri.equals(uri)
                    && this.requestHeaders.equals(requestHeaders)
                    && Objects.equals(this.requestBody, requestBody);
        }
    }

    private class EntityRestCallDecorator<T> extends EntityRestCall<T> {
        private final EntityRestCall<T> delegate;

        public EntityRestCallDecorator(EntityRestCall<T> delegate) {
            super(delegate.context(), delegate.method(), delegate.uri(), delegate.requestHeaders(), null,
                    delegate.converter());
            this.delegate = delegate;
        }

        @Override
        public EntityResponse<T> execute() {
            return delegate.execute();
        }

        protected Recordings recordings() {
            return Recordings.get(folder, uri().getAuthority());
        }
    }

    private class RecorderEntityRestCall<T> extends EntityRestCallDecorator<T> {
        public RecorderEntityRestCall(EntityRestCall<T> delegate) {
            super(delegate);
        }

        @Override
        public EntityResponse<T> execute() {
            Recording recording = new Recording();
            recording.uri(uri()).requestHeaders(requestHeaders());

            log.debug("record GET for {}", uri());
            EntityResponse<T> response = super.execute();

            recording.responseStatus((Status) response.status()); // not perfect, but StatusType can't be deserialized
            recording.responseHeaders(response.headers());
            recording.responseBody(response.getBody(String.class));

            recordings().addOrReplace(recording).write();

            log.debug("recorded {}", recording);
            return response;
        }
    }

    private class PlaybackEntityRestCall<T> extends EntityRestCallDecorator<T> {
        public PlaybackEntityRestCall(EntityRestCall<T> delegate) {
            super(delegate);
        }

        @Override
        public EntityResponse<T> execute() {
            Recording recording = recordings().find(uri(), requestHeaders());
            if (recording == null)
                return super.execute();
            log.debug("playback GET for {}", uri());
            return new EntityResponse<>(context(), recording.responseStatus(), recording.responseHeaders(), converter(),
                    recording.responseBody().getBytes());
        }
    }

    public RestCallFactory requestFactoryMock = new RestCallFactory() {
        @Override
        public <T, M extends Annotation> EntityRestCall<T> createRestCall(Class<M> method, RestContext context, URI uri,
                Headers headers, ResponseConverter<T> converter) {
            log.info("create rest GET call for {}", uri);
            return new PlaybackEntityRestCall<>(
                    new RecorderEntityRestCall<>(
                            originalRequestFactory.createRestCall(method, context, uri, headers, converter)));
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
