package us.sodiumlabs.ai.chess.data.external.game;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import us.sodiumlabs.ai.chess.data.SodiumStyle;

import java.util.List;

@SodiumStyle
@Value.Immutable
@JsonSerialize(as = ImmutableListGameResponse.class)
@JsonDeserialize(as = ImmutableListGameResponse.class)
public abstract class ListGameResponse {
    public abstract List<OutputGame> getGames();

    public static ImmutableListGameResponse.Builder builder() {
        return new ImmutableListGameResponse.Builder();
    }
}
