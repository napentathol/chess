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
            .withStartX(6)
            .withStartY(7)
            .withEndX(5)
            .withEndY(5)
            .build());

        final Board expected = Board.deserialize( new int[] {
            0xcbad_eabc,
            0x9999_9999,
            0,
            0,
            0,
            0x0000_0300,
            0x1111_1111,
            0x4325_6204
        });

        assertEquals(expected, afterMove);
        assertEquals(Board.deserialize(initVector), Board.INITIAL_BOARD);
    }

    @Test
    public void testMove_weird() {
        final int[] initVector = Board.INITIAL_BOARD.serialize();

        final Board afterMove = Board.INITIAL_BOARD.move(Move.builder()
            .withStartX(4)
            .withStartY(7)
            .withEndX(3)
            .withEndY(5)
            .build());

        final Board expected = Board.deserialize( new int[] {
            0xcbad_eabc,
            0x9999_9999,
            0,
            0,
            0,
            0x0006_0000,
            0x1111_1111,
            0x4325_0234
        });

        assertEquals(expected, afterMove);
        assertEquals(Board.deserialize(initVector), Board.INITIAL_BOARD);
    }
}