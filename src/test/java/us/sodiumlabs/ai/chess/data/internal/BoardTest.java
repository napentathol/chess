package us.sodiumlabs.ai.chess.data.internal;

import org.junit.Test;
import us.sodiumlabs.ai.chess.data.internal.game.Board;
import us.sodiumlabs.ai.chess.data.internal.game.Move;

import static org.junit.Assert.assertEquals;

public class BoardTest {
    @Test
    public void testInitialVector() {
        assertEquals(Board.INITIAL_BOARD, Board.deserialize(Board.INITIAL_BOARD.serialize()));
    }

    @Test
    public void testMove() {
        final int[] initVector = Board.INITIAL_BOARD.serialize();

        final Board afterMove = Board.INITIAL_BOARD.move(Move.builder()
            .withStartX(6).withStartY(0)
            .withEndX(5).withEndY(2)
            .build());

        final Board expected = Board.deserialize( new int[] {
            0x4325_6204,
            0x1111_1111,
            0x0000_0300,
            0,
            0,
            0,
            0x9999_9999,
            0xcbad_eabc
        });

        assertEquals(expected, afterMove);
        assertEquals(Board.deserialize(initVector), Board.INITIAL_BOARD);
    }

    @Test
    public void testMove_weird() {
        final int[] initVector = Board.INITIAL_BOARD.serialize();

        final Board afterMove = Board.INITIAL_BOARD.move(Move.builder()
            .withStartX(4).withStartY(0)
            .withEndX(3).withEndY(2)
            .build());

        final Board expected = Board.deserialize( new int[] {
            0x4325_0234,
            0x1111_1111,
            0x0006_0000,
            0,
            0,
            0,
            0x9999_9999,
            0xcbad_eabc,
        });

        assertEquals(expected, afterMove);
        assertEquals(Board.deserialize(initVector), Board.INITIAL_BOARD);
    }
}