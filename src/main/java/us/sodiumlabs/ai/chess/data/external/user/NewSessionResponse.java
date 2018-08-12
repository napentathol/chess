package us.sodiumlabs.ai.chess.data.external.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import us.sodiumlabs.ai.chess.data.SodiumStyle;

@Value.Immutable
@SodiumStyle
@JsonSerialize(as = ImmutableNewSessionResponse.class)
@JsonDeserialize(as = ImmutableNewSessionResponse.class)
public abstract class NewSessionResponse {
    public abstract String getUserId();

    public abstract String getSecret();

    public static ImmutableNewSessionResponse.Builder builder() {
        return new ImmutableNewSessionResponse.Builder();
    }
}
