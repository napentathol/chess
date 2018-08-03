package us.sodiumlabs.ai.chess.rules;

import us.sodiumlabs.ai.chess.data.internal.game.Game;
import us.sodiumlabs.ai.chess.data.internal.game.Move;

import java.util.Objects;

public class GameRuleValidator {
    public boolean validateMove(final Game game, final Move move) {
        Objects.requireNonNull(game);
        Objects.requireNonNull(move);

        // Implement more checks
        return true;
    }
}
