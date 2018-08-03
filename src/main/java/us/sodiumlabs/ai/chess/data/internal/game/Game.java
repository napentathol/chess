package us.sodiumlabs.ai.chess.data.internal.game;

import com.google.common.collect.ImmutableList;
import us.sodiumlabs.ai.chess.data.internal.user.User;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Game {
    private final User whitePlayer;

    private final User blackPlayer;

    private PlayerPiece.Player currentPlayer = PlayerPiece.Player.WHITE;

    private Board currentBoard = Board.INITIAL_BOARD;

    private final List<Move> moveHistory = new LinkedList<>();

    private final Object synchronizer = new Object();

    private final UUID gameId;

    public Game(final UUID gameId, final User whitePlayer, final User blackPlayer) {
        this.gameId = Objects.requireNonNull(gameId);
        this.whitePlayer = Objects.requireNonNull(whitePlayer);
        this.blackPlayer = Objects.requireNonNull(blackPlayer);
    }

    public Board getCurrentBoard() {
        synchronized (synchronizer) {
            return currentBoard;
        }
    }

    public String getCurrentPlayer() {
        synchronized (synchronizer) {
            return currentPlayer.name();
        }
    }

    public User getCurrentPlayerUser() {
        synchronized (synchronizer) {
            return currentPlayer == PlayerPiece.Player.WHITE ? whitePlayer : blackPlayer;
        }
    }

    public void updateBoard(final Move move) {
        synchronized (synchronizer) {
            currentBoard = currentBoard.move(move);
            currentPlayer = PlayerPiece.Player.WHITE == currentPlayer
                ? PlayerPiece.Player.BLACK : PlayerPiece.Player.WHITE;
            moveHistory.add(move);
        }
    }

    public List<Move> getMoveHistory() {
        synchronized(synchronizer) {
            return ImmutableList.copyOf(moveHistory);
        }
    }

    public UUID getGameId() {
        return gameId;
    }

    public User getBlackPlayer() {
        return blackPlayer;
    }

    public User getWhitePlayer() {
        return whitePlayer;
    }
}
