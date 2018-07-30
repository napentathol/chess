package us.sodiumlabs.ai.chess.data.internal;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BoardTest {
    @Test
    public void testInitialVector() {
        assertEquals(Board.INITIAL_BOARD, Board.deserialize(Board.INITIAL_BOARD.serialize()));
        System.out.println(Board.INITIAL_BOARD);
    }
}