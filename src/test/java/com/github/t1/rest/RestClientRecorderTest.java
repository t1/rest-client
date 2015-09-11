package com.github.t1.rest;

import static com.github.t1.rest.RestContext.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;

import org.junit.*;

import lombok.SneakyThrows;

public class RestClientRecorderTest {
    private static final URI BASE = URI.create("http://example.mock");
    private static final Path TMP = Paths.get("target/recordings");
    private static final Credentials CREDENTIALS = new Credentials("foo", "bar");

    private RestClientMocker mock;

    @Before
    public void before() {
        deleteRecordings();
        mock = new RestClientMocker(REST.register("base", BASE));
    }

    @After
    public void after() {
        deleteRecordings();
    }

    @SneakyThrows(IOException.class)
    private void deleteRecordings() {
        Files.deleteIfExists(TMP.resolve("example.mock"));
        Files.createDirectories(TMP);
    }

    private RestContext recorder(Path folder) {
        return new RestClientRecorder(mock.context(), folder).context();
    }

    @Test
    public void shouldRecordAndPlaybackFromMemory() {
        RestContext recorder = recorder(null);
        mock.on(BASE + "/string").GET().respond("value-0"); // make sure the mock return only the last body
        mock.on(BASE + "/string").GET().respond("value-1");

        String initialBody = recorder.resource("base", "/string").GET();
        assertEquals("save recording", "value-1", initialBody);

        mock.on(BASE + "/string").GET().respond("value-2");

        String recordedBody = recorder.resource("base", "/string").GET();
        assertEquals("return recorded", "value-1", recordedBody);
    }

    @Test
    public void shouldRecordAndPlaybackFromFile() {
        RestContext recorder = recorder(TMP);
        mock.on(BASE + "/string").GET().respond("value-0");
        mock.on(BASE + "/string").GET().respond("value-1");

        String initialBody = recorder.resource("base", "/string").GET();

        assertEquals("save recording", "value-1", initialBody);

        mock.on(BASE + "/string").GET().respond("value-2");

        String recordedBody = recorder.resource("base", "/string").GET();

        assertEquals("return recorded", "value-1", recordedBody);
    }

    @Test
    public void shouldRecordTwoPaths() {
        RestContext recorder = recorder(TMP);
        mock.on(BASE + "/string-0").GET().respond("value-0");
        mock.on(BASE + "/string-1").GET().respond("value-1");

        String body0 = recorder.resource("base", "/string-0").GET();
        String body1 = recorder.resource("base", "/string-1").GET();

        assertEquals("value-0", body0);
        assertEquals("value-1", body1);
    }

    @Test
    @Ignore("the mock can't handle this, yet")
    public void shouldRecordTwoDifferentRequestHeaders() {
        RestContext recorder = recorder(TMP);
        mock.on(BASE + "/string").GET().requireBasicAuth(CREDENTIALS).respond("value-auth");
        mock.on(BASE + "/string").GET().respond("value-clear");

        String bodyAuthorized = recorder.resource("base", "/string").basicAuth(CREDENTIALS).GET(String.class);
        String bodyClear = recorder.resource("base", "/string").GET();

        assertEquals("value-auth", bodyAuthorized);
        assertEquals("value-clear", bodyClear);
    }
}
