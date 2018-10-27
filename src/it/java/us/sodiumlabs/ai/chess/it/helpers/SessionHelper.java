package us.sodiumlabs.ai.chess.it.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.hash.Hashing;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.utils.IOUtils;
import us.sodiumlabs.ai.chess.data.external.user.NewSessionResponse;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class SessionHelper implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final CloseableHttpClient httpClient;

    private final ObjectMapper objectMapper;

    private final NewSessionResponse newSessionResponse;

    public SessionHelper() {
        this.httpClient = HttpClientBuilder.create()
            .setRedirectStrategy(new RedirectStrategy() {
                @Override
                public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) {
                    return false;
                }

                @Override
                public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context) {
                    throw new RuntimeException("Never redirect.");
                }
            })
            .setConnectionTimeToLive(10, TimeUnit.SECONDS )
            .build();
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new GuavaModule());

        this.newSessionResponse = newSession(httpClient, objectMapper);
    }

    private NewSessionResponse newSession(final HttpClient httpClient, final ObjectMapper objectMapper)
    {
        try {
            // Register user
            final HttpResponse r1 = httpClient.execute(RequestBuilder.post("http://localhost:4567/user")
                .setEntity(new StringEntity("{\"username\":\"bob\"}", ContentType.APPLICATION_JSON))
                .build());
            final String o1 = IOUtils.toString(r1.getEntity().getContent());

            log.info("Received new session response: " + o1);

            return objectMapper.readValue(o1, NewSessionResponse.class);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public <T> T execute(final String method, final String uri, final Object payload, final Class<T> clazz) {
        try {
            return execute(method, uri, Optional.of(objectMapper.writeValueAsString(payload)), clazz);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public HttpResponse execute(final String method, final String uri, final Object payload) {
        try {
            return execute(method, uri, Optional.of(objectMapper.writeValueAsString(payload)));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public <T> T get(final String uri, final Class<T> clazz) {
        return execute(HttpGet.METHOD_NAME, uri, Optional.empty(), clazz);
    }

    private <T> T execute(final String method, final String uri, final Optional<String> payload, final Class<T> clazz) {
        try {
            final HttpResponse response = execute(method, uri, payload);
            final String value = IOUtils.toString(response.getEntity().getContent());
            log.info("Received response: " + value);
            return objectMapper.readValue(value, clazz);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private HttpResponse execute(final String method, final String uri, final Optional<String> payload) {
        final RequestBuilder requestBuilder = RequestBuilder.create(method).setUri(uri);

        payload.ifPresent(s -> {
            final String now = OffsetDateTime.now(ZoneOffset.UTC).toString();
            final String hash = Hashing.sha256()
                .hashString(newSessionResponse.getSecret() + payload + now, StandardCharsets.UTF_8)
                .toString();

            requestBuilder
                .addHeader("X-Time", now)
                .addHeader("X-User", newSessionResponse.getUserId())
                .addHeader("X-Signature", hash)
                .setEntity(new StringEntity(s, ContentType.APPLICATION_JSON));
        });

        try {
            return httpClient.execute(requestBuilder.build());
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String getUserId() {
        return newSessionResponse.getUserId();
    }

    @Override
    public void close()
    {
        try {
            httpClient.close();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
