package us.sodiumlabs.ai.chess.data.internal;

import java.util.Objects;

public class PlayerPiece {
    public static final int PLAYER_PIECE_MASK = 0xf;

    public final Piece piece;
    public final Player player;

    public PlayerPiece(final Piece piece, final Player player) {
        this.piece = Objects.requireNonNull(piece);
        this.player = Objects.requireNonNull(player);
    }

    public static PlayerPiece deserialize(final int id) {
        final Piece piece = Piece.pieceById(id & Piece.PIECE_MASK);
        final Player player = piece == Piece.EMPTY
            ? Player.NONE
            : Player.playerById((id & Player.PLAYER_MASK) >> 3);

        return new PlayerPiece(piece, player);
    }

    public int serialize() {
        return player.getId() * Player.PLAYER_MASK + piece.getId();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof PlayerPiece)) return false;
        final PlayerPiece other = (PlayerPiece) obj;

        return Objects.equals(other.piece, piece)
            && Objects.equals(other.player, player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, piece);
    }

    @Override
    public String toString() {
        return String.format("%s %s", player, piece);
    }
}
