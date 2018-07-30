package us.sodiumlabs.ai.chess.data.internal;

import java.util.Arrays;
import java.util.Objects;

public class Board {
    private static final int BOARD_DIMENSION = 8;

    private static final int[] INIT_VECTOR = {
        0xcbae_dabc,
        0x9999_9999,
        0,
        0,
        0,
        0,
        0x1111_1111,
        0x4325_6234
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
                r += playerPieces[BOARD_DIMENSION - i - 1].serialize();
            }
            return r;
        }

        static Row deserialize(final int representation) {
            final PlayerPiece[] playerPieces = new PlayerPiece[BOARD_DIMENSION];

            int r = representation;
            for(int i = 0; i < BOARD_DIMENSION; i++) {
                playerPieces[i] = PlayerPiece.deserialize(r & PlayerPiece.PLAYER_PIECE_MASK);
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
