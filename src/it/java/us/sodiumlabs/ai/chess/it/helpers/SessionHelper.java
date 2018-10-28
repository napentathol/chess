package us.sodiumlabs.ai.chess.it.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.hash.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.sodiumlabs.ai.chess.data.external.user.NewSessionResponse;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

public class SessionHelper {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final HttpClient httpClient;

    private final ObjectMapper objectMapper;

    private final NewSessionResponse newSessionResponse;

    public SessionHelper() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new GuavaModule());

        this.newSessionResponse = newSession(httpClient, objectMapper);
    }

    private NewSessionResponse newSession(final HttpClient httpClient, final ObjectMapper objectMapper)
    {
        try {
            // Register user
            final HttpRequest request = HttpRequest.newBuilder(new URI("http://localhost:4567/user"))
                .POST(HttpRequest.BodyPublishers.ofString("{\"username\":\"bob\"}"))
                .header("Content-Type", "application/json")
                .build();

            final String o1 = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();

            log.info("Received new session response: " + o1);

            return objectMapper.readValue(o1, NewSessionResponse.class);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        } catch (final InterruptedException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T execute(final String method, final URI uri, final Object payload, final Class<T> clazz) {
        try {
            return execute(method, uri, Optional.of(objectMapper.writeValueAsString(payload)), clazz);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public HttpResponse<String> execute(final String method, final URI uri, final Object payload) {
        try {
            return execute(method, uri, Optional.of(objectMapper.writeValueAsString(payload)));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public <T> T get(final URI uri, final Class<T> clazz) {
        return execute("GET", uri, Optional.empty(), clazz);
    }

    private <T> T execute(final String method, final URI uri, final Optional<String> payload, final Class<T> clazz) {
        try {
            final HttpResponse<String> response = execute(method, uri, payload);
            final String value = response.body();
            log.info("Received response: " + value);
            return objectMapper.readValue(value, clazz);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private HttpResponse<String> execute(final String method, final URI uri, final Optional<String> payload) {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri)
            .method(method, payload
                .map(HttpRequest.BodyPublishers::ofString)
                .orElse(HttpRequest.BodyPublishers.noBody()));

        payload.ifPresent(s -> {
            final String now = OffsetDateTime.now(ZoneOffset.UTC).toString();
            final String hash = Hashing.sha256()
                .hashString(newSessionResponse.getSecret() + s + now, StandardCharsets.UTF_8)
                .toString();

            requestBuilder
                .header("Content-Type", "application/json")
                .header("X-Time", now)
                .header("X-User", newSessionResponse.getUserId())
                .header("X-Signature", hash);
        });

        try {
            return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getUserId() {
        return newSessionResponse.getUserId();
    }
}
