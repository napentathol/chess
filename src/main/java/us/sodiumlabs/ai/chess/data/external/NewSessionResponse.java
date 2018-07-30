package us.sodiumlabs.ai.chess.data.external;

import org.immutables.value.Value;
import us.sodiumlabs.ai.chess.data.SodiumStyle;

@Value.Immutable
@SodiumStyle
public abstract class NewSessionResponse {
    public abstract String getUserId();

    public abstract String getSecret();
}
