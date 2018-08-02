package us.sodiumlabs.ai.chess.data.external;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import us.sodiumlabs.ai.chess.data.SodiumStyle;
import us.sodiumlabs.ai.chess.data.internal.user.User;

@SodiumStyle
@Value.Immutable
@JsonSerialize(as = ImmutableOutputUser.class)
@JsonDeserialize(as = ImmutableOutputUser.class)
public abstract class OutputUser {
    public abstract String getUserId();

    public abstract String getUserName();

    public static OutputUser fromUser(final User user) {
        return new ImmutableOutputUser.Builder()
            .withUserId(user.getUserId().toString())
            .withUserName(user.getUsername())
            .build();
    }
}
