package us.sodiumlabs.ai.chess.data.internal;

import org.junit.Test;
import us.sodiumlabs.ai.chess.data.internal.game.PlayerPiece;

import static org.junit.Assert.assertEquals;

public class PlayerPieceTest {

    @Test
    public void testSerialization() {
        for(final PlayerPiece.Player pl : PlayerPiece.Player.values()) {
            for (final PlayerPiece.Piece pi : PlayerPiece.Piece.values()) {
                final PlayerPiece initial = new PlayerPiece(pi, pl);
                final PlayerPiece deserialized = PlayerPiece.deserialize(initial.serialize());

                if( PlayerPiece.Player.NONE == pl ^ PlayerPiece.Piece.EMPTY == pi ) {
                    if( PlayerPiece.Piece.EMPTY == pi ) {
                        assertEquals(PlayerPiece.Player.NONE, deserialized.player);
                        assertEquals(PlayerPiece.Piece.EMPTY, deserialized.piece);
                    } else {
                        assertEquals(PlayerPiece.Player.WHITE, deserialized.player);
                        assertEquals(pi, deserialized.piece);
                    }
                } else {
                    assertEquals(initial, deserialized);
                }
            }
        }
    }

}