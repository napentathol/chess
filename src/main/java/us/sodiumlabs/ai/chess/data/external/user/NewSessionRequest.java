package us.sodiumlabs.ai.chess.data.external.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import us.sodiumlabs.ai.chess.data.SodiumStyle;

@SodiumStyle
@Value.Immutable
@JsonSerialize(as = ImmutableNewSessionRequest.class)
@JsonDeserialize(as = ImmutableNewSessionRequest.class)
public abstract class NewSessionRequest {
    public abstract String getUsername();
}
