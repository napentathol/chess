package us.sodiumlabs.ai.chess.data.internal.game;

import org.immutables.value.Value;
import us.sodiumlabs.ai.chess.data.SodiumStyle;

@SodiumStyle
@Value.Immutable
public abstract class Move {

    @Value.Check
    protected void check() {
        if(getStartX() < 0 || getStartX() > 7)
            throw new IllegalArgumentException( "Start X must be between 0 and 7 (inclusive). Was: " + getStartX() );
        if(getStartY() < 0 || getStartY() > 7)
            throw new IllegalArgumentException( "Start Y must be between 0 and 7 (inclusive). Was: " + getStartY() );
        if(getEndX() < 0 || getEndX() > 7)
            throw new IllegalArgumentException( "End X must be between 0 and 7 (inclusive). Was: " + getEndX() );
        if(getEndY() < 0 || getEndY() > 7)
            throw new IllegalArgumentException( "End Y must be between 0 and 7 (inclusive). Was: " + getEndY() );
    }

    public abstract int getStartX();

    public abstract int getStartY();

    public abstract int getEndX();

    public abstract int getEndY();

    public static ImmutableMove.Builder builder() {
        return new ImmutableMove.Builder();
    }
}
