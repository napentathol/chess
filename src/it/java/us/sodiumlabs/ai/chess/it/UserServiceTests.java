package us.sodiumlabs.ai.chess.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.hash.Hashing;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.sodiumlabs.ai.chess.data.external.user.ListUserResponse;
import us.sodiumlabs.ai.chess.data.external.user.NewSessionResponse;
import us.sodiumlabs.ai.chess.data.external.user.OutputUser;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UserServiceTests {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Test
    public void test_goodHash() throws IOException, InterruptedException {

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new GuavaModule());

        final HttpClient httpClient = HttpClient.newHttpClient();

        // Register user
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:4567/user"))
            .POST(HttpRequest.BodyPublishers.ofString("{\"username\":\"bob\"}"))
            .header("Content-Type", "application/json")
            .build();
        final String o1 = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();

        log.info("Received new session response: " + o1);

        final NewSessionResponse user = objectMapper.readValue(o1, NewSessionResponse.class);

        // Ensure user is registered
        request = HttpRequest.newBuilder(URI.create("http://localhost:4567/user")).GET().build();
        final String o2 = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
        log.info("Received list user response: " + o2);
        final ListUserResponse users = objectMapper.readValue(o2, ListUserResponse.class);

        boolean foundUser = false;
        for(final OutputUser testUser : users.getUsers()) {
            if(Objects.equals(testUser.getUserId(), user.getUserId())) {
                foundUser = true;
                assertEquals("bob", testUser.getUserName());
            }
        }
        assertTrue(foundUser);

        // Ensure hashing works properly.
        final String body = UUID.randomUUID().toString();
        final String now = OffsetDateTime.now(ZoneOffset.UTC).toString();
        final String hash = Hashing.sha256()
            .hashString(user.getSecret() + body + now, StandardCharsets.UTF_8)
            .toString();
        log.info(String.format("Sending body [%s], time [%s], hash [%s]", body, now, hash));

        request = HttpRequest.newBuilder(URI.create("http://localhost:4567/user/testSig"))
            .PUT(HttpRequest.BodyPublishers.ofString(body))
            .header("X-Time", now)
            .header("X-User", user.getUserId())
            .header("X-Signature", hash)
            .build();
        final HttpResponse<String> r3 = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("Received test signature response: " + r3.body());

        assertEquals(200, r3.statusCode());

        // Ensure signature check works properly.
        final String now2 = OffsetDateTime.now(ZoneOffset.UTC).toString();
        final String hash2 = "badHash";
        log.info(String.format("Sending body [%s], time [%s], hash [%s]", body, now2, hash2));

        request = HttpRequest.newBuilder(URI.create("http://localhost:4567/user/testSig"))
            .PUT(HttpRequest.BodyPublishers.ofString(body))
            .header("Content-Type", "application/json")
            .header("X-Time", now2)
            .header("X-User", user.getUserId())
            .header("X-Signature", hash2)
            .build();

        final HttpResponse<String> r4 = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("Received test signature response: " + r4.body());

        assertEquals(401, r4.statusCode());
        assertEquals("Received invalid signature!", r4.body());

        // Ensure date check works properly.
        final String now3 = OffsetDateTime.now(ZoneOffset.UTC).minusSeconds(30).toString();
        final String hash3 = Hashing.sha256()
            .hashString(user.getSecret() + body + now3, StandardCharsets.UTF_8)
            .toString();
        log.info(String.format("Sending body [%s], time [%s], hash [%s]", body, now3, hash3));

        request = HttpRequest.newBuilder(URI.create("http://localhost:4567/user/testSig"))
            .PUT(HttpRequest.BodyPublishers.ofString(body))
            .header("Content-Type", "application/json")
            .header("X-Time", now3)
            .header("X-User", user.getUserId())
            .header("X-Signature", hash3)
            .build();

        final HttpResponse<String> r5 = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("Received test signature response: " + r5.body());

        assertEquals(401, r5.statusCode());
        assertEquals("Signature too old!", r5.body());

        // Ensure user id check works properly.
        final String now4 = OffsetDateTime.now(ZoneOffset.UTC).toString();

        request = HttpRequest.newBuilder(URI.create("http://localhost:4567/user/testSig"))
            .PUT(HttpRequest.BodyPublishers.ofString(body))
            .header("Content-Type", "application/json")
            .header("X-Time", now4)
            .header("X-User", UUID.randomUUID().toString())
            .build();

        final HttpResponse<String> r6 = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("Received test signature response: " + r6.body());

        assertEquals(404, r6.statusCode());
        assertTrue(r6.body().startsWith("Missing user: "));
    }
}
