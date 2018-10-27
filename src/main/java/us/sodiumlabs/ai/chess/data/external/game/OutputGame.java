package us.sodiumlabs.ai.chess.data.external.game;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import us.sodiumlabs.ai.chess.data.SodiumStyle;
import us.sodiumlabs.ai.chess.data.internal.game.Game;

@SodiumStyle
@Value.Immutable
@JsonSerialize(as = ImmutableOutputGame.class)
@JsonDeserialize(as = ImmutableOutputGame.class)
public abstract class OutputGame {
    public abstract String getGameId();

    public abstract int[] getBoard();

    public abstract String getCurrentPlayer();

    public abstract String getGameState();

    public static OutputGame fromGame(final Game game) {
        return new ImmutableOutputGame.Builder()
            .withGameId(game.getGameId().toString())
            .withBoard(game.getCurrentBoard().serialize())
            .withCurrentPlayer(game.getCurrentPlayer())
            .withGameState(game.getGameState().name())
            .build();
    }
}
