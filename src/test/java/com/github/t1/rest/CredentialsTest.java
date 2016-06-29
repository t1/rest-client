package com.github.t1.rest;

import org.junit.Test;

import javax.ws.rs.GET;
import java.net.URI;

import static com.github.t1.rest.RestContext.*;
import static javax.xml.bind.DatatypeConverter.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class CredentialsTest {
    private static final Credentials CREDENTIALS = new Credentials("foo", "bar");
    private static final String BASE64_CREDENTIALS =
            printBase64Binary((CREDENTIALS.userName() + ":" + CREDENTIALS.password()).getBytes());

    private final URI baseUri = URI.create("http://example.org/app/");
    private RestContext config = REST;

    private void givenCredentials() {
        givenCredentials(URI.create(baseUri.toString().substring(0, baseUri.toString().length() - 1)));
    }

    private void givenCredentials(URI uri) {
        config = config.register(uri, CREDENTIALS);
    }

    @Test
    public void shouldFindCredentialsByBaseUri() {
        givenCredentials();

        Credentials found = config.getCredentials(baseUri);

        assertEquals(CREDENTIALS, found);
    }

    @Test
    public void shouldNotExposeCredentialsInToString() {
        givenCredentials();

        Credentials found = config.getCredentials(baseUri);

        assertThat(found.toString()).doesNotContain(CREDENTIALS.userName()).doesNotContain(CREDENTIALS.password());
    }

    @Test
    public void shouldNotFindCredentialsWithOtherHost() {
        givenCredentials();

        Credentials found = config.getCredentials(URI.create("http://other.org/app"));

        assertEquals(null, found);
    }

    @Test
    public void shouldNotFindCredentialsWithOtherApp() {
        givenCredentials();

        Credentials found = config.getCredentials(baseUri.resolve("/app2"));

        assertEquals(null, found);
    }

    @Test
    public void shouldFindCredentialsIncludingSubPath() {
        URI resolvedPath = baseUri.resolve("path");
        givenCredentials(resolvedPath);

        Credentials found = config.getCredentials(resolvedPath);

        assertEquals(CREDENTIALS, found);
    }

    @Test
    public void shouldFindCredentialsIncludingMatrixParam() {
        URI resolvedPath = baseUri.resolve(";me=too");
        givenCredentials(resolvedPath);

        Credentials found = config.getCredentials(resolvedPath);

        assertEquals(CREDENTIALS, found);
    }

    @Test
    public void shouldFindCredentialsIncludingQueryParam() {
        URI resolvedPath = baseUri.resolve("?me=too");
        givenCredentials(resolvedPath);

        Credentials found = config.getCredentials(resolvedPath);

        assertEquals(CREDENTIALS, found);
    }

    @Test
    public void shouldFindCredentialsIncludingFragment() {
        URI resolvedPath = baseUri.resolve("#me=too");
        givenCredentials(resolvedPath);

        Credentials found = config.getCredentials(resolvedPath);

        assertEquals(CREDENTIALS, found);
    }

    @Test
    public void shouldNotFindCredentialsWithOtherSubPath() {
        givenCredentials(baseUri.resolve("path"));

        Credentials found = config.getCredentials(baseUri.resolve("path2"));

        assertEquals(null, found);
    }

    @Test
    public void shouldNotFindCredentialsWithOtherMatrixParam() {
        givenCredentials(baseUri.resolve(";me=too"));

        Credentials found = config.getCredentials(baseUri.resolve(";me=not"));

        assertEquals(null, found);
    }

    @Test
    public void shouldNotFindCredentialsWithOtherQueryParam() {
        givenCredentials(baseUri.resolve("?me=too"));

        Credentials found = config.getCredentials(baseUri.resolve("?me=not"));

        assertEquals(null, found);
    }

    @Test
    public void shouldNotFindCredentialsWithOtherFragment() {
        givenCredentials(baseUri.resolve("#me=too"));

        Credentials found = config.getCredentials(baseUri.resolve("#me=not"));

        assertEquals(null, found);
    }

    @Test
    public void shouldFindCredentialsIgnoringSubPath() {
        givenCredentials();

        Credentials found = config.getCredentials(baseUri.resolve("path"));

        assertEquals(CREDENTIALS, found);
    }

    @Test
    public void shouldFindCredentialsIgnoringMatrixParam() {
        givenCredentials();

        Credentials found = config.getCredentials(baseUri.resolve(";me=too"));

        assertEquals(CREDENTIALS, found);
    }

    @Test
    public void shouldFindCredentialsIgnoringQueryParam() {
        givenCredentials();

        Credentials found = config.getCredentials(baseUri.resolve("?me=too"));

        assertEquals(CREDENTIALS, found);
    }

    @Test
    public void shouldFindCredentialsIgnoringFragment() {
        givenCredentials();

        Credentials found = config.getCredentials(baseUri.resolve("#me=too"));

        assertEquals(CREDENTIALS, found);
    }

    @Test
    public void shouldAddBasicAuthHeader() {
        givenCredentials();

        EntityRestCall<?> request = config.createRestCall(GET.class, baseUri, new Headers(),
                String.class, String.class);

        String auth = request.requestHeaders().firstValue("Authorization");
        assertEquals("Basic " + BASE64_CREDENTIALS, auth);
    }
}
