package us.sodiumlabs.ai.chess.data.internal.game;

import java.util.Objects;

public class PlayerPiece {
    public static final int PLAYER_PIECE_MASK = 0xf;

    public static PlayerPiece EMPTY = new PlayerPiece(Piece.EMPTY, Player.NONE);

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

    public enum Piece {
        EMPTY(0),
        PAWN(1),
        BISHOP(2),
        KNIGHT(3),
        ROOK(4),
        QUEEN(5),
        KING(6);

        public static final int PIECE_MASK = 0x7;

        private static final Piece[] piece = new Piece[Piece.values().length];

        static {
            for( final Piece p : Piece.values() ) {
                piece[p.pieceId] = p;
            }
        }

        private final int pieceId;

        Piece(final int id) {
            pieceId = id;
        }

        public int getId() {
            return pieceId;
        }

        public static Piece pieceById(int id) {
            if(id < 0 || id > 6) throw new IllegalArgumentException("Piece id must be between 0 and 6 inclusive, was " + id);

            return piece[id];
        }
    }

    public enum Player {
        WHITE,
        BLACK,
        NONE;

        public static int PLAYER_MASK = 0x8;

        public static Player playerById(final int id ) {
            if(id < 0 || id > 1) throw new IllegalArgumentException("Player id must be 0 or 1, was: " + id);

            return id == 0 ? WHITE : BLACK;
        }

        public int getId() {
            return this == BLACK ? 1 : 0;
        }
    }
}
