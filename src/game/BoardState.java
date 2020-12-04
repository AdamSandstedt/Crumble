package game;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BoardState {
	private final Map<BoardPoint, GamePiece> gamePieces;
	private final boolean currentTurn;
	private final Set<Set<GamePiece>> chains;
	private final Map<BoardPoint, BoardPoint> boardPoints;
	private final Map<GamePiece, Notation> pieceNotations;
	private final Map<GamePiece, Set<GamePiece> > pieceNeighbors;
	private final Map<GamePiece, Set<GamePiece> > piecesSurrounding;
	
	public Map<BoardPoint, GamePiece> getGamePieces() {
		return gamePieces;
	}

	public boolean isCurrentTurn() {
		return currentTurn;
	}

	public Set<Set<GamePiece>> getChains() {
		Set<Set<GamePiece> > newChains = new HashSet<>();
		for(Set<GamePiece> s: chains) {
			newChains.add(new HashSet<>(s));
		}
		return newChains;
	}

	public Map<BoardPoint, BoardPoint> getBoardPoints() {
		return boardPoints;
	}

	public Map<GamePiece, Notation> getPieceNotations() {
		return pieceNotations;
	}

	public Map<GamePiece, Set<GamePiece>> getPieceNeighbors() {
		Map<GamePiece, Set<GamePiece> > newPieceNeighbors = new HashMap<>();
		for(GamePiece g: pieceNeighbors.keySet()) {
			newPieceNeighbors.put(g, new HashSet<>(pieceNeighbors.get(g)));
		}
		return newPieceNeighbors;
	}

	public Map<GamePiece, Set<GamePiece>> getPiecesSurrounding() {
		Map<GamePiece, Set<GamePiece> > newPiecesSurrounding = new HashMap<>();
		for(GamePiece g: piecesSurrounding.keySet()) {
			newPiecesSurrounding.put(g, new HashSet<>(piecesSurrounding.get(g)));
		}
		return newPiecesSurrounding;
	}

	public BoardState(Map<BoardPoint, GamePiece> gamePieces, boolean currentTurn, Set<Set<GamePiece>> chains, Map<BoardPoint, BoardPoint> boardPoints, Map<GamePiece, Notation> pieceNotations, Map<GamePiece, Set<GamePiece>> pieceNeighbors, Map<GamePiece, Set<GamePiece>> piecesSurrounding) {
		Set<Set<GamePiece> > newChains = new HashSet<>();
		for(Set<GamePiece> s: chains) {
			newChains.add(new HashSet<>(s));
		}
		Map<GamePiece, Set<GamePiece> > newPieceNeighbors = new HashMap<>();
		for(GamePiece g: pieceNeighbors.keySet()) {
			newPieceNeighbors.put(g, new HashSet<>(pieceNeighbors.get(g)));
		}
		Map<GamePiece, Set<GamePiece> > newPiecesSurrounding = new HashMap<>();
		for(GamePiece g: piecesSurrounding.keySet()) {
			newPiecesSurrounding.put(g, new HashSet<>(piecesSurrounding.get(g)));
		}
		this.gamePieces = gamePieces;
		this.currentTurn = currentTurn;
		this.chains = newChains;
		this.boardPoints = boardPoints;
		this.pieceNotations = pieceNotations;
		this.pieceNeighbors = newPieceNeighbors;
		this.piecesSurrounding = newPiecesSurrounding;
	}
}
