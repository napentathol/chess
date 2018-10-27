package us.sodiumlabs.ai.chess.data.internal.game;

import java.util.Arrays;
import java.util.Objects;

public class Board {
    private static final int BOARD_DIMENSION = 8;

    private static final int[] INIT_VECTOR = {
        0x4325_6234,
        0x1111_1111,
        0,
        0,
        0,
        0,
        0x9999_9999,
        0xcbad_eabc
    };

    public static final Board INITIAL_BOARD = Board.deserialize(INIT_VECTOR);

    private static class Row {
        private final PlayerPiece[] playerPieces;

        private Row(final PlayerPiece[] playerPieces) {
            this.playerPieces = Objects.requireNonNull(playerPieces);
        }

        int serialize() {
            int r = 0;
            for(int i = 0; i < BOARD_DIMENSION; i++) {
                r <<= 4;
                r += playerPieces[i].serialize();
            }
            return r;
        }

        static Row deserialize(final int representation) {
            final PlayerPiece[] playerPieces = new PlayerPiece[BOARD_DIMENSION];

            int r = representation;
            for(int i = 0; i < BOARD_DIMENSION; i++) {
                playerPieces[BOARD_DIMENSION - i - 1] = PlayerPiece.deserialize(r & PlayerPiece.PLAYER_PIECE_MASK);
                r >>= 4;
            }

            return new Row(playerPieces);
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && obj instanceof Row && Arrays.equals(((Row) obj).playerPieces, playerPieces);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(playerPieces);
        }

        @Override
        public String toString() {
            return Arrays.toString(playerPieces);
        }

        PlayerPiece get(int x) {
            if(x < 0 || x > 7) throw new IllegalArgumentException("X must be between 0 and 7 (inclusive); was: " + x);
            return playerPieces[x];
        }

        Row put(final PlayerPiece toPut, final int x) {
            if(x < 0 || x > 7) throw new IllegalArgumentException("X must be between 0 and 7 (inclusive); was: " + x);

            final PlayerPiece[] newPieces = new PlayerPiece[BOARD_DIMENSION];

            for(int i = 0; i < BOARD_DIMENSION; i++) {
                if(i == x) {
                    newPieces[i] = Objects.requireNonNull(toPut);
                } else {
                    newPieces[i] = playerPieces[i];
                }
            }

            return new Row(newPieces);
        }
    }

    private final Row[] rows;

    private Board(Row[] rows) {
        this.rows = Objects.requireNonNull(rows);
    }

    public int[] serialize() {
        final int[] out = new int[BOARD_DIMENSION];

        for(int i = 0; i < BOARD_DIMENSION; i++) {
            out[i] = rows[i].serialize();
        }

        return out;
    }

    public static Board deserialize(final int[] in) {
        if(in.length != BOARD_DIMENSION)
            throw new IllegalArgumentException(String.format("Improper number of rows for board. Had %d rows, needed 8.", in.length));

        final Row[] rows = new Row[BOARD_DIMENSION];

        for(int i = 0; i < BOARD_DIMENSION; i++) {
            rows[i] = Row.deserialize(in[i]);
        }

        return new Board(rows);
    }

    public PlayerPiece getPieceAtPosition(final int x, final int y) {
        if(x < 0 || x > 7) throw new IllegalArgumentException("X must be between 0 and 7 (inclusive); was: " + x);
        if(y < 0 || y > 7) throw new IllegalArgumentException("Y must be between 0 and 7 (inclusive); was: " + y);

        return rows[y].get(x);
    }

    private Board put(final PlayerPiece toPut, final int x, final int y) {
        if(x < 0 || x > 7) throw new IllegalArgumentException("X must be between 0 and 7 (inclusive); was: " + x);
        if(y < 0 || y > 7) throw new IllegalArgumentException("Y must be between 0 and 7 (inclusive); was: " + y);

        Objects.requireNonNull(toPut);

        final Row[] newRows = new Row[BOARD_DIMENSION];

        for(int i = 0; i < BOARD_DIMENSION; i++) {
            if(i == y) {
                newRows[i] = rows[i].put(toPut, x);
            } else {
                newRows[i] = rows[i];
            }
        }

        return new Board(newRows);
    }

    public Board move(final Move move) {
        return put(getPieceAtPosition(move.getStartX(), move.getStartY()), move.getEndX(), move.getEndY())
            .put(PlayerPiece.EMPTY, move.getStartX(), move.getStartY());
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof Board && Arrays.equals(((Board)obj).rows, rows);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(rows);
    }

    @Override
    public String toString() {
        return Arrays.toString(rows);
    }
}
