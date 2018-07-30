package us.sodiumlabs.ai.chess.data.internal.user;

import org.immutables.value.Value;
import us.sodiumlabs.ai.chess.data.SodiumStyle;

import java.util.UUID;

@SodiumStyle
@Value.Immutable
public abstract class User {
    public abstract UUID getUserId();

    public abstract String getSecret();

    public abstract String getUsername();
}
