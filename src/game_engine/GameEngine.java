package game_engine;

import game.BoardState;
import game.CrumbleGame;
import game.GameBoard;
import game.GamePiece;
import game.Notation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameEngine {
	CrumbleGame game = new CrumbleGame(false);
	GameBoard board = game.getBoard();
	boolean playerType = false;
		
	public String GetNextMove() {
		MoveValue moveValue = minMax(-1000000, 1000000, 2, playerType);
		return moveValue.returnMove;
	}

	public void setPlayerType(boolean playerType) {
		this.playerType = playerType;
	}

	public GameBoard getBoard() {
		return board;
	}

	public void doMove(String moveNotation) {
		game.doMove(moveNotation);
	}

	public double evaluateBoard() {
		return 0;
	}
	
	
	private class MoveValue {

	    public double returnValue;
	    public String returnMove;

	    public MoveValue() {
	        returnValue = 0;
	    }

	    public MoveValue(double returnValue) {
	        this.returnValue = returnValue;
	    }

	}


	protected MoveValue minMax(double alpha, double beta, int maxDepth, boolean player) {
	    ArrayList<String> moves = generateLegalMoves(player);
	    double value = 0;
	    boolean isMaximizer = (player == playerType);
	    if(maxDepth == 0 || board.isGameOver()) {
	        value = evaluateBoard();
	        return new MoveValue(value);
	    }
	    MoveValue returnMove;
	    MoveValue bestMove = null;
	    if(isMaximizer) {
	    	for(String currentMove: moves) {
//	            System.out.println(currentMove);
	            game.doMove(currentMove);
	            returnMove = minMax(alpha, beta, maxDepth - 1, !player);
//	            System.out.println("undo " + currentMove);
	            game.loadState("undo");
	            if ((bestMove == null) || (bestMove.returnValue < returnMove.returnValue)) {
	                bestMove = returnMove;
	                bestMove.returnMove = currentMove;
	            }
	            if (returnMove.returnValue > alpha) {
	                alpha = returnMove.returnValue;
	                bestMove = returnMove;
	            }
	            if (beta <= alpha) {
	                bestMove.returnValue = beta;
	                bestMove.returnMove = null;
	                return bestMove;
	            }
	        }
	        return bestMove;
	    } else {
	    	for(String currentMove: moves) {
//	            System.out.println(currentMove);
	            game.doMove(currentMove);
	            returnMove = minMax(alpha, beta, maxDepth - 1, !player);
//	            System.out.println("undo " + currentMove);
	            game.loadState("undo");
	            if ((bestMove == null) || (bestMove.returnValue > returnMove.returnValue)) {
	                bestMove = returnMove;
	                bestMove.returnMove = currentMove;
	            }
	            if (returnMove.returnValue < beta) {
	                beta = returnMove.returnValue;
	                bestMove = returnMove;
	            }
	            if (beta <= alpha) {
	                bestMove.returnValue = alpha;
	                bestMove.returnMove = null;
	                return bestMove;
	            }
	        }
	        return bestMove;
	    }
	}

	private ArrayList<String> generateLegalMoves(boolean player) {
		ArrayList<String> moves = new ArrayList<>();
		Map<GamePiece, Notation> notations = board.getPieceNotations();
		for(GamePiece piece: board.getGamePieces().values()) {
			if(piece.isColor() == player && piece.canSplitHorizontal()) {
				moves.add(notations.get(piece).toString()+"H");
			}
			if(piece.isColor() == player && piece.canSplitVertical()) {
				moves.add(notations.get(piece).toString()+"V");
			}
		}
		return moves;
	}

	public CrumbleGame getGame() {
		return game;
	}
	
}
