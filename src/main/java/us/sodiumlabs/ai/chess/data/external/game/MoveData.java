package us.sodiumlabs.ai.chess.data.external.game;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import us.sodiumlabs.ai.chess.data.SodiumStyle;
import us.sodiumlabs.ai.chess.data.internal.game.ImmutableMove;
import us.sodiumlabs.ai.chess.data.internal.game.Move;

@SodiumStyle
@Value.Immutable
@JsonSerialize(as = ImmutableMoveData.class)
@JsonDeserialize(as = ImmutableMoveData.class)
public abstract class MoveData {
    public abstract int getStartX();

    public abstract int getStartY();

    public abstract int getEndX();

    public abstract int getEndY();

    public Move toMove() {
        return new ImmutableMove.Builder()
            .withEndX(getEndX())
            .withEndY(getEndY())
            .withStartX(getStartX())
            .withStartY(getStartY())
            .build();
    }

    public static MoveData fromMove(final Move move) {
        return new ImmutableMoveData.Builder()
            .withStartX(move.getStartX())
            .withStartY(move.getStartY())
            .withEndX(move.getEndX())
            .withEndY(move.getEndY())
            .build();
    }
}
