package us.sodiumlabs.ai.chess.data.external.game;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import us.sodiumlabs.ai.chess.data.SodiumStyle;
import us.sodiumlabs.ai.chess.data.internal.game.Game;

@SodiumStyle
@Value.Immutable
@JsonSerialize(as = ImmutableCurrentTurnResponse.class)
@JsonDeserialize(as = ImmutableCurrentTurnResponse.class)
public abstract class CurrentTurnResponse {
    public abstract String getCurrentPlayer();

    public static CurrentTurnResponse fromGame(final Game game) {
        return new ImmutableCurrentTurnResponse.Builder().withCurrentPlayer(game.getCurrentPlayer()).build();
    }
}
