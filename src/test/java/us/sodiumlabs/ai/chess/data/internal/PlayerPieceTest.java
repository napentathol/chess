package us.sodiumlabs.ai.chess.data.internal;

import org.junit.Test;

import static org.junit.Assert.*;

public class PlayerPieceTest {

    @Test
    public void testSerialization() {
        for(final Player pl : Player.values()) {
            for (final Piece pi : Piece.values()) {
                final PlayerPiece initial = new PlayerPiece(pi, pl);
                final PlayerPiece deserialized = PlayerPiece.deserialize(initial.serialize());

                if( Player.NONE == pl ^ Piece.EMPTY == pi ) {
                    if( Piece.EMPTY == pi ) {
                        assertEquals(Player.NONE, deserialized.player);
                        assertEquals(Piece.EMPTY, deserialized.piece);
                    } else {
                        assertEquals(Player.WHITE, deserialized.player);
                        assertEquals(pi, deserialized.piece);
                    }
                } else {
                    assertEquals(initial, deserialized);
                }
            }
        }
    }

}