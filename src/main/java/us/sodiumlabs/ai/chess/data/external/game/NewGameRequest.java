package us.sodiumlabs.ai.chess.data.external.game;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import us.sodiumlabs.ai.chess.data.SodiumStyle;

@SodiumStyle
@Value.Immutable
@JsonSerialize(as = ImmutableNewGameRequest.class)
@JsonDeserialize(as = ImmutableNewGameRequest.class)
public abstract class NewGameRequest {
    public abstract String getWhiteUserId();
    public abstract String getBlackUserId();
}
