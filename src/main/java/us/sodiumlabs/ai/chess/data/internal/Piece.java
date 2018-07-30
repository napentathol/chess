package us.sodiumlabs.ai.chess.data.internal;

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
