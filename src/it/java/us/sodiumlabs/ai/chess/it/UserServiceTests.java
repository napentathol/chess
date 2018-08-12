package us.sodiumlabs.ai.chess.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.hash.Hashing;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.utils.IOUtils;
import us.sodiumlabs.ai.chess.data.external.user.ListUserResponse;
import us.sodiumlabs.ai.chess.data.external.user.NewSessionResponse;
import us.sodiumlabs.ai.chess.data.external.user.OutputUser;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UserServiceTests {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Test
    public void test_goodHash() throws IOException {

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new GuavaModule());

        final CloseableHttpClient httpClient = HttpClientBuilder.create()
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

        // Register user
        final HttpResponse r1 = httpClient.execute(RequestBuilder.post("http://localhost:4567/user")
            .setEntity(new StringEntity("{\"username\":\"bob\"}", ContentType.APPLICATION_JSON))
            .build());
        final String o1 = IOUtils.toString(r1.getEntity().getContent());

        log.info("Received new session response: " + o1);

        final NewSessionResponse user = objectMapper.readValue(o1, NewSessionResponse.class);

        // Ensure user is registered
        final HttpResponse r2 = httpClient.execute(RequestBuilder.get("http://localhost:4567/user").build());
        final String o2 = IOUtils.toString(r2.getEntity().getContent());
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

        final HttpResponse r3 = httpClient.execute(RequestBuilder.put("http://localhost:4567/user/testSig")
            .addHeader("X-Time", now)
            .addHeader("X-User", user.getUserId())
            .addHeader("X-Signature", hash)
            .setEntity(new StringEntity(body))
            .build());

        final String o3 = IOUtils.toString(r3.getEntity().getContent());
        log.info("Received test signature response: " + o3);

        assertEquals(200, r3.getStatusLine().getStatusCode());

        // Ensure signature check works properly.
        final String now2 = OffsetDateTime.now(ZoneOffset.UTC).toString();
        final String hash2 = "badHash";
        log.info(String.format("Sending body [%s], time [%s], hash [%s]", body, now2, hash2));

        final HttpResponse r4 = httpClient.execute(RequestBuilder.put("http://localhost:4567/user/testSig")
            .addHeader("X-Time", now2)
            .addHeader("X-User", user.getUserId())
            .addHeader("X-Signature", hash2)
            .setEntity(new StringEntity(body))
            .build());

        final String o4 = IOUtils.toString(r4.getEntity().getContent());
        log.info("Received test signature response: " + o4);

        assertEquals(401, r4.getStatusLine().getStatusCode());
        assertEquals("Received invalid signature!", o4);

        // Ensure date check works properly.
        final String now3 = OffsetDateTime.now(ZoneOffset.UTC).minusSeconds(30).toString();
        final String hash3 = Hashing.sha256()
            .hashString(user.getSecret() + body + now3, StandardCharsets.UTF_8)
            .toString();
        log.info(String.format("Sending body [%s], time [%s], hash [%s]", body, now3, hash3));

        final HttpResponse r5 = httpClient.execute(RequestBuilder.put("http://localhost:4567/user/testSig")
            .addHeader("X-Time", now3)
            .addHeader("X-User", user.getUserId())
            .addHeader("X-Signature", hash3)
            .setEntity(new StringEntity(body))
            .build());

        final String o5 = IOUtils.toString(r5.getEntity().getContent());
        log.info("Received test signature response: " + o5);

        assertEquals(401, r5.getStatusLine().getStatusCode());
        assertEquals("Signature too old!", o5);

        // Ensure user id check works properly.
        final String now4 = OffsetDateTime.now(ZoneOffset.UTC).toString();
        final HttpResponse r6 = httpClient.execute(RequestBuilder.put("http://localhost:4567/user/testSig")
            .addHeader("X-Time", now4)
            .addHeader("X-User", UUID.randomUUID().toString())
            .setEntity(new StringEntity(body))
            .build());

        final String o6 = IOUtils.toString(r6.getEntity().getContent());
        log.info("Received test signature response: " + o6);

        assertEquals(404, r6.getStatusLine().getStatusCode());
        assertTrue(o6.startsWith("Missing user: "));

        httpClient.close();
    }
}
