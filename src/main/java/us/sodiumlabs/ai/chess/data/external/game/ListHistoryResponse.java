package us.sodiumlabs.ai.chess.data.external.game;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import us.sodiumlabs.ai.chess.data.SodiumStyle;

import java.util.List;

@SodiumStyle
@Value.Immutable
@JsonSerialize(as = ImmutableListHistoryResponse.class)
@JsonDeserialize(as = ImmutableListHistoryResponse.class)
public abstract class ListHistoryResponse {
    public abstract List<MoveData> getMoveHistory();

    public static ImmutableListHistoryResponse.Builder builder() {
        return new ImmutableListHistoryResponse.Builder();
    }
}
