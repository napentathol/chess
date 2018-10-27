package us.sodiumlabs.ai.chess.it;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.utils.IOUtils;
import us.sodiumlabs.ai.chess.data.external.game.CurrentTurnResponse;
import us.sodiumlabs.ai.chess.data.external.game.ImmutableMoveData;
import us.sodiumlabs.ai.chess.data.external.game.ImmutableNewGameRequest;
import us.sodiumlabs.ai.chess.data.external.game.ListGameResponse;
import us.sodiumlabs.ai.chess.data.external.game.MoveData;
import us.sodiumlabs.ai.chess.data.external.game.OutputGame;
import us.sodiumlabs.ai.chess.it.helpers.SessionHelper;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GameServiceTests {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Test
    public void test_HappyCase() {
        try(final SessionHelper whiteHelper = new SessionHelper(); final SessionHelper blackHelper = new SessionHelper()) {
            // Create the game
            final OutputGame game = whiteHelper.execute(HttpPost.METHOD_NAME, "http://localhost:4567/game",
                new ImmutableNewGameRequest.Builder()
                    .withWhiteUserId(whiteHelper.getUserId())
                    .withBlackUserId(blackHelper.getUserId())
                    .build(),
                OutputGame.class);

            // List the games
            final ListGameResponse gameResponse = whiteHelper.get("http://localhost:4567/game", ListGameResponse.class);
            assertTrue(gameResponse.getGames().contains(game));

            final ListGameResponse whiteGameResponse =
                whiteHelper.get("http://localhost:4567/game?userId=" + whiteHelper.getUserId(), ListGameResponse.class);
            assertTrue(whiteGameResponse.getGames().contains(game));

            final ListGameResponse blackGameResponse =
                blackHelper.get("http://localhost:4567/game?userId=" + blackHelper.getUserId(), ListGameResponse.class);
            assertTrue(blackGameResponse.getGames().contains(game));

            move(whiteHelper, blackHelper, game, "WHITE",
                new ImmutableMoveData.Builder()
                    .withStartX(5).withStartY(1)
                    .withEndX(5).withEndY(2)
                    .build(),
                new int[] {
                    0x4325_6234,
                    0x1111_1011,
                    0x0000_0100,
                    0,
                    0,
                    0,
                    0x9999_9999,
                    0xcbad_eabc
                });

            move(whiteHelper, blackHelper, game, "BLACK",
                new ImmutableMoveData.Builder()
                    .withStartX(4).withStartY(6)
                    .withEndX(4).withEndY(4)
                    .build(),
                new int[] {
                    0x4325_6234,
                    0x1111_1011,
                    0x0000_0100,
                    0,
                    0x0000_9000,
                    0,
                    0x9999_0999,
                    0xcbad_eabc
                });

            move(whiteHelper, blackHelper, game, "WHITE",
                new ImmutableMoveData.Builder()
                    .withStartX(6).withStartY(1)
                    .withEndX(6).withEndY(3)
                    .build(),
                new int[] {
                    0x4325_6234,
                    0x1111_1001,
                    0x0000_0100,
                    0x0000_0010,
                    0x0000_9000,
                    0,
                    0x9999_0999,
                    0xcbad_eabc
                });

            move(whiteHelper, blackHelper, game, "BLACK",
                new ImmutableMoveData.Builder()
                    .withStartX(3).withStartY(7)
                    .withEndX(7).withEndY(3)
                    .build(),
                new int[] {
                    0x4325_6234,
                    0x1111_1001,
                    0x0000_0100,
                    0x0000_001d,
                    0x0000_9000,
                    0,
                    0x9999_0999,
                    0xcba0_eabc
                });
        }
    }

    private void move(
        final SessionHelper whiteHelper, final SessionHelper blackHelper,
        final OutputGame game, final String expectedTurn, final MoveData moveData, final int[] board
    ) {
        // Get the turn
        final CurrentTurnResponse currentTurnResponse = whiteHelper.get(
            String.format("http://localhost:4567/game/%s/turn", game.getGameId()), CurrentTurnResponse.class);
        assertEquals(expectedTurn, currentTurnResponse.getCurrentPlayer());

        sendMoveOnWrongTurn("WHITE".equals(expectedTurn) ? blackHelper : whiteHelper, game, moveData);
        validateMove("WHITE".equals(expectedTurn) ? whiteHelper : blackHelper, game, moveData, board);
    }

    private void validateMove(final SessionHelper sessionHelper, final OutputGame game, final MoveData moveData, final int[] board) {
        final OutputGame outputGame =
            sessionHelper.execute(HttpPut.METHOD_NAME, "http://localhost:4567/game/" + game.getGameId(), moveData, OutputGame.class);

        assertEquals(board, outputGame.getBoard());
    }

    private void sendMoveOnWrongTurn(final SessionHelper sessionHelper, final OutputGame game, final MoveData moveData) {
        final HttpResponse httpResponse =
            sessionHelper.execute(HttpPut.METHOD_NAME, "http://localhost:4567/game/" + game.getGameId(), moveData);

        assertEquals(403, httpResponse.getStatusLine().getStatusCode());
        try {
            assertEquals("Not your turn.", IOUtils.toString(httpResponse.getEntity().getContent()));
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }
}
