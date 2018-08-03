package us.sodiumlabs.ai.chess.data.external.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import us.sodiumlabs.ai.chess.data.SodiumStyle;

import java.util.List;

@SodiumStyle
@Value.Immutable
@JsonSerialize(as = ImmutableListUserResponse.class)
@JsonDeserialize(as = ImmutableListUserResponse.class)
public abstract class ListUserResponse {
    public abstract List<OutputUser> getUsers();

    public static ImmutableListUserResponse.Builder builder() {
        return new ImmutableListUserResponse.Builder();
    }
}
