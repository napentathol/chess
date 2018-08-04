package us.sodiumlabs.ai.chess.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import spark.Request;
import spark.Response;
import us.sodiumlabs.ai.chess.data.external.game.CurrentTurnResponse;
import us.sodiumlabs.ai.chess.data.external.game.ListGameResponse;
import us.sodiumlabs.ai.chess.data.external.game.ListHistoryResponse;
import us.sodiumlabs.ai.chess.data.external.game.MoveData;
import us.sodiumlabs.ai.chess.data.external.game.NewGameRequest;
import us.sodiumlabs.ai.chess.data.external.game.OutputGame;
import us.sodiumlabs.ai.chess.data.internal.game.Game;
import us.sodiumlabs.ai.chess.data.internal.game.Move;
import us.sodiumlabs.ai.chess.data.internal.user.User;
import us.sodiumlabs.ai.chess.rules.GameRuleValidator;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.post;
import static spark.Spark.put;

public class GameService {

    private final ConcurrentMap<UUID, Game> gameMap = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    private final UserService userService;

    private final GameRuleValidator gameRuleValidator;

    public GameService(
        final ObjectMapper objectMapper,
        final UserService userService,
        final GameRuleValidator gameRuleValidator
    ) {
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.userService = Objects.requireNonNull(userService);
        this.gameRuleValidator = Objects.requireNonNull(gameRuleValidator);
    }

    public void initialize() {
        get("/game", this::getGames);
        get("/game/:gameId", this::getGame);
        get("/game/turn/:gameId", this::getCurrentTurn);
        get("/game/byUserId/:userId", this::getGamesForUser);
        get("/game/history/:gameId", this::getGameHistory);
        post("/game", this::createGame);
        put("/game/:gameId", this::putMove);
    }

    private String putMove(final Request request, final Response response) {
        try {
            final UUID gameId = UUID.fromString(request.params(":gameId"));
            final Game game = getRequiredGame(gameId);
            final Move move = objectMapper.readValue(request.body(), MoveData.class).toMove();
            final User user = userService.validateSignature(request);

            game.updateBoard(() -> {
                if(!game.getCurrentPlayerUser().equals(user)) throw halt(403, "Not your turn.");
                if(!gameRuleValidator.validateMove(game, move)) throw halt(400, "Invalid move: " + move);
                return move;
            });

            response.type("application/json");
            return objectMapper.writeValueAsString(OutputGame.fromGame(game));
        } catch (IOException e) {
            throw new RuntimeException("Unable to read move request.", e);
        }
    }

    private String createGame(final Request request, final Response response) {
        try {
            final NewGameRequest newGameRequest = objectMapper.readValue(request.body(), NewGameRequest.class);
            final UUID gameId = UUID.randomUUID();
            final Game game = new Game(gameId,
                userService.getRequiredUser(UUID.fromString(newGameRequest.getWhiteUserId())),
                userService.getRequiredUser(UUID.fromString(newGameRequest.getBlackUserId())));

            gameMap.put(gameId, game);

            response.type("application/json");
            return objectMapper.writeValueAsString(OutputGame.fromGame(game));
        } catch (IOException e) {
            throw new RuntimeException("Unable to read new game request.", e);
        }
    }

    private String getGameHistory(final Request request, final Response response) {
        final UUID gameId = UUID.fromString(request.params(":gameId"));
        final Game game = getRequiredGame(gameId);

        response.type("application/json");
        try {
            return objectMapper.writeValueAsString(ListHistoryResponse.builder()
                .withMoveHistory(game.getMoveHistory().stream()
                    .map(MoveData::fromMove)
                    .collect(Collectors.toList()))
                .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to materialize game history as json for game: " + gameId);
        }
    }

    private String getGamesForUser(final Request request, final Response response) {
        final UUID userId = UUID.fromString(request.params(":userId"));

        response.type("application/json");
        try {
            return objectMapper.writeValueAsString(ListGameResponse.builder()
                .withGames(gameMap.values().stream()
                    .filter(((Predicate<Game>) g -> Objects.equals(g.getBlackPlayer().getUserId(), userId))
                        .or(g -> Objects.equals(g.getWhitePlayer().getUserId(), userId)))
                    .map(OutputGame::fromGame)
                    .collect(Collectors.toList()))
                .build());
        } catch (final JsonProcessingException e) {
            throw new RuntimeException("Unable to materialize games as json.");
        }
    }

    private String getCurrentTurn(final Request request, final Response response) {
        final UUID gameId = UUID.fromString(request.params(":gameId"));

        response.type("application/json");
        try {
            return objectMapper.writeValueAsString(CurrentTurnResponse.fromGame(getRequiredGame(gameId)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to materialize game: " + gameId);
        }
    }

    private String getGame(final Request request, final Response response) {
        final UUID gameId = UUID.fromString(request.params(":gameId"));
        response.type("application/json");
        try {
            return objectMapper.writeValueAsString(OutputGame.fromGame(getRequiredGame(gameId)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to materialize game: " + gameId);
        }
    }

    private String getGames(final Request request, final Response response) {
        response.type("application/json");
        try {
            return objectMapper.writeValueAsString(ListGameResponse.builder()
                .withGames(gameMap.values().stream()
                    .map(OutputGame::fromGame)
                    .collect(Collectors.toList()))
                .build());
        } catch (final JsonProcessingException e) {
            throw new RuntimeException("Unable to materialize games as json.");
        }
    }

    private Game getRequiredGame(final UUID uuid) {
        return Optional.ofNullable(gameMap.get(uuid)).orElseThrow(() -> halt(404, "Missing game: " + uuid));
    }
}
