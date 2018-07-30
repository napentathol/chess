package us.sodiumlabs.ai.chess.data.internal;

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
