package us.sodiumlabs.ai.chess.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import spark.Request;
import spark.Response;
import us.sodiumlabs.ai.chess.data.external.ImmutableNewSessionResponse;
import us.sodiumlabs.ai.chess.data.external.ListUserResponse;
import us.sodiumlabs.ai.chess.data.external.NewSessionRequest;
import us.sodiumlabs.ai.chess.data.external.OutputUser;
import us.sodiumlabs.ai.chess.data.internal.user.ImmutableUser;
import us.sodiumlabs.ai.chess.data.internal.user.User;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.post;

public class UserService {
    private final ConcurrentMap<UUID, User> userMap = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    public UserService(final ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    public void initialize() {
        post("/user", this::newSession);
        get("/user", this::getUsers);
        get("/user/:userId", this::getUser);
    }

    public User validateSignature(final Request request) {
        final UUID userId = UUID.fromString(request.headers("X-User"));
        final String signature = request.headers("X-Signature");

        final User user = userMap.get(userId);

        if(user == null) throw halt(401, "User id invalid.");

        final String computedSignature = Hashing.sha256()
            .hashString( user.getSecret() + request.body(), StandardCharsets.UTF_8 )
            .toString();

        if(!Objects.equals(signature, computedSignature)) throw  halt(401, "Computer signature invalid!");

        return user;
    }

    private String newSession(final Request request, final Response response) {
        try {
            final NewSessionRequest newSessionRequest = objectMapper.readValue(request.body(), NewSessionRequest.class);
            final UUID uuid = UUID.randomUUID();
            final UUID secret = UUID.randomUUID();
            final ByteBuffer secretBuffer = ByteBuffer.allocate(Long.BYTES * 2);

            secretBuffer.putLong(secret.getMostSignificantBits());
            secretBuffer.putLong(secret.getLeastSignificantBits());

            final String secretString = Base64.getMimeEncoder().encodeToString(secretBuffer.array());

            final User user = new ImmutableUser.Builder()
                .withUserId(uuid)
                .withUsername(newSessionRequest.getUsername())
                .withSecret(secretString)
                .build();

            userMap.putIfAbsent(uuid, user);

            response.type("application/json");

            return objectMapper.writeValueAsString(new ImmutableNewSessionResponse.Builder()
                .withSecret(secretString)
                .withUserId(uuid.toString())
                .build());
        } catch (final IOException e) {
            throw new RuntimeException("Unable to read new session request.", e);
        }
    }

    private String getUsers(final Request request, final Response response) {
        response.type("application/json");
        try {
            return objectMapper.writeValueAsString(ListUserResponse.builder()
                .withUsers(userMap.values().stream()
                    .map(OutputUser::fromUser)
                    .collect(Collectors.toList()))
                .build());
        } catch (final JsonProcessingException e) {
            throw new RuntimeException("Unable to materialize user map as json.");
        }
    }

    private String getUser(final Request request, final Response response) {
        final UUID userId = UUID.fromString(request.params(":userId"));
        final User user = userMap.get(userId);
        response.type("application/json");
        try {
            return objectMapper.writeValueAsString(OutputUser.fromUser(user));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to materialize user: " + userId, e);
        }
    }
}
