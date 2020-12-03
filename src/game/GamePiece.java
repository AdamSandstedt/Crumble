package game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GamePiece {
	private final boolean color; // For now, 0=white, 1=black, but a string or enum might be easier to read
	private final BoardPoint bottomLeft;
	private final BoardPoint topRight;
	private final BoardPoint horizontalSplitStart;
	private final BoardPoint verticalSplitStart;
	private final BoardPoint horizontalSplitEnd;
	private final BoardPoint verticalSplitEnd;
	private final String shape;
	private static boolean showNotation = false;
	private static final Map<GamePiece, GamePiece> pieces = new HashMap<>();
	
	public static final int X_OFFSET = 20;
	public static final int Y_OFFSET = 20;
	
	private GamePiece(boolean color, BoardPoint bottomLeft, BoardPoint topRight) {
		this.color = color;
		this.bottomLeft = bottomLeft;
		this.topRight = topRight;
		double width = topRight.getX()-bottomLeft.getX();
		double height = topRight.getY()-bottomLeft.getY();
		if(width == height) {
			shape = "square";
			horizontalSplitStart = BoardPoint.makePoint(bottomLeft.getX(), (bottomLeft.getY()+topRight.getY())/2);
			verticalSplitStart = BoardPoint.makePoint((bottomLeft.getX()+topRight.getX())/2, bottomLeft.getY());
			horizontalSplitEnd = BoardPoint.makePoint(topRight.getX(), (bottomLeft.getY()+topRight.getY())/2);
			verticalSplitEnd = BoardPoint.makePoint((bottomLeft.getX()+topRight.getX())/2, topRight.getY());
		}
		else if(height == 2*width) {
			shape = "tall";
			horizontalSplitStart = BoardPoint.makePoint(bottomLeft.getX(), (bottomLeft.getY()+topRight.getY())/2);
			horizontalSplitEnd = BoardPoint.makePoint(topRight.getX(), (bottomLeft.getY()+topRight.getY())/2);
			verticalSplitEnd = null;
			verticalSplitStart = null;
		}
		else {
			shape = "wide";
			verticalSplitEnd = BoardPoint.makePoint((bottomLeft.getX()+topRight.getX())/2, topRight.getY());
			verticalSplitStart = BoardPoint.makePoint((bottomLeft.getX()+topRight.getX())/2, bottomLeft.getY());
			horizontalSplitStart = null;
			horizontalSplitEnd = null;
		}
	}
	
	public static GamePiece makePiece(boolean color, BoardPoint bottomLeft, BoardPoint topRight, Collection<GamePiece> gamePieces, Map<GamePiece, Set<GamePiece> > pieceNeighbors, Map<GamePiece, Set<GamePiece> > piecesSurrounding, Map<GamePiece, Notation> pieceNotations, Notation notation) {
		GamePiece piece = pieces.get(new GamePiece(color, bottomLeft, topRight));
		if(piece == null) {
			piece = new GamePiece(color, bottomLeft, topRight);
			pieces.put(piece, piece);
		}
		pieceNeighbors.put(piece, new HashSet<>());
		piecesSurrounding.put(piece, new HashSet<>());
		piece.updateNeighbors(gamePieces, pieceNeighbors, piecesSurrounding);
		pieceNotations.put(piece, notation);
		return piece;
	}
	
	public GamePiece oppositeColorPiece(Map<GamePiece, Set<GamePiece>> pieceNeighbors, Map<GamePiece, Set<GamePiece>> piecesSurrounding, Map<GamePiece, Notation> pieceNotations) {
		GamePiece newPiece = pieces.get(new GamePiece(!color, bottomLeft, topRight));
		if(newPiece == null) {
			newPiece = new GamePiece(!color, bottomLeft, topRight);
			pieces.put(newPiece, newPiece);
		}
		Set<GamePiece> neighbors = pieceNeighbors.get(this);
		if(neighbors == null) {
			neighbors = new HashSet<>();
		}
		pieceNeighbors.put(newPiece, neighbors);
		pieceNeighbors.remove(this);
		for(GamePiece g: pieceNeighbors.get(newPiece)) {
			pieceNeighbors.get(g).remove(this);
			pieceNeighbors.get(g).add(newPiece);
		}
		piecesSurrounding.put(newPiece, piecesSurrounding.get(this));
		piecesSurrounding.remove(this);
		for(GamePiece g: piecesSurrounding.get(newPiece)) {
			piecesSurrounding.get(g).remove(this);
			piecesSurrounding.get(g).add(newPiece);
		}
		pieceNotations.put(newPiece, pieceNotations.get(this));
		pieceNotations.remove(this);
		return newPiece;
	}
	
	private void updateNeighbors(Collection<GamePiece> gamePieces, Map<GamePiece, Set<GamePiece> > pieceNeighbors, Map<GamePiece, Set<GamePiece> > piecesSurrounding) {
		Set<GamePiece> neighbors = pieceNeighbors.get(this);
		Set<GamePiece> surrounding = piecesSurrounding.get(this);
		for(GamePiece piece: gamePieces) {
			if(isNeighbor(piece)) {
				neighbors.add(piece);
				pieceNeighbors.get(piece).add(this);
			}
		}
		for(GamePiece piece: neighbors) {
			surrounding.add(piece);
			piecesSurrounding.get(piece).add(this);
		}
		for(GamePiece piece: gamePieces) {
			if(piece.getBottomLeft().getX() == topRight.getX() && piece.getBottomLeft().getY() == topRight.getY() ||
			   piece.getBottomLeft().getX() == topRight.getX() && piece.getTopRight().getY() == bottomLeft.getY() ||
			   piece.getTopRight().getX() == bottomLeft.getX() && piece.getBottomLeft().getY() == topRight.getY() ||
			   piece.getTopRight().getX() == bottomLeft.getX() && piece.getTopRight().getY() == bottomLeft.getY() ) {
				surrounding.add(piece);
				piecesSurrounding.get(piece).add(this);
			}
		}
	}

	private boolean isNeighbor(GamePiece piece) { // check that this and piece share some edge
		return bottomLeft.getX() == piece.getTopRight().getX() && // left edge (of the object: this)
			   bottomLeft.getY() < piece.getTopRight().getY() && topRight.getY() > piece.getBottomLeft().getY() ||
			   topRight.getX() == piece.getBottomLeft().getX() && // right edge
			   bottomLeft.getY() < piece.getTopRight().getY() && topRight.getY() > piece.getBottomLeft().getY() ||
			   bottomLeft.getY() == piece.getTopRight().getY() && // bottom edge
			   bottomLeft.getX() < piece.getTopRight().getX() && topRight.getX() > piece.getBottomLeft().getX() ||
			   topRight.getY() == piece.getBottomLeft().getY() && // top edge
			   bottomLeft.getX() < piece.getTopRight().getX() && topRight.getX() > piece.getBottomLeft().getX() ;
	}

	public String getShape() {
		return shape;
	}
	
	public boolean canSplitHorizontal() {
		return !shape.equals("wide");
	}
	
	public boolean canSplitVertical() {
		return !shape.equals("tall");
	}

	public BoardPoint getHorizontalSplitStart() {
		return horizontalSplitStart;
	}

	public BoardPoint getVerticalSplitStart() {
		return verticalSplitStart;
	}

	public BoardPoint getHorizontalSplitEnd() {
		return horizontalSplitEnd;
	}

	public BoardPoint getVerticalSplitEnd() {
		return verticalSplitEnd;
	}

	public boolean isColor() {
		return color;
	}

	@Override
	public String toString() {
		return "GamePiece [color=" + color + ", bottomLeft=" + bottomLeft + ", topRight=" + topRight + "]";
	}

	public BoardPoint getBottomLeft() {
		return bottomLeft;
	}

	public BoardPoint getTopRight() {
		return topRight;
	}

	public void draw(Graphics g, GameBoard board, Notation notation) {
		Graphics2D g2 = (Graphics2D) g;
		int x1 = (int)(X_OFFSET + board.getxConversion()*bottomLeft.getX());
		int x2 = (int)(X_OFFSET + board.getxConversion()*topRight.getX());
		int y1 = (int)(Y_OFFSET + board.getBoardHeight() - board.getyConversion()*topRight.getY());
		int y2 = (int)(Y_OFFSET + board.getBoardHeight() - board.getyConversion()*bottomLeft.getY());
		if(color) {
			g2.setColor(Color.black);
		}
		else {
			g2.setColor(Color.white);
		}
		g2.fillRect(x1, y1, x2-x1, y2-y1);
		if(color) {
			g2.setColor(Color.white);
		}
		else {
			g2.setColor(Color.black);
		}
		g2.drawRect(x1, y1, x2-x1, y2-y1);
		if(showNotation) {
			float fontSize = 20 * (float)(topRight.getX()-bottomLeft.getX());
			g2.setFont(g2.getFont().deriveFont(fontSize));
			g2.drawString(notation.toString(), x1+5, y2-5);
		}
		
	}

	public static boolean isShowNotation() {
		return showNotation;
	}

	public static void setShowNotation(boolean showNotation) {
		GamePiece.showNotation = showNotation;
	}

	public boolean contains(Point p, GameBoard board) {
		return contains(BoardPoint.makeTempPoint(p, board));
	}

	public boolean contains(BoardPoint p) {
		return p.getX() < topRight.getX() && p.getX() > bottomLeft.getX() &&
		       p.getY() < topRight.getY() && p.getY() > bottomLeft.getY() ;
	}
	
	public boolean contains(GamePiece p) {
		return p.getTopRight().getX() <= topRight.getX() && p.getBottomLeft().getX() >= bottomLeft.getX() &&
		       p.getTopRight().getY() <= topRight.getY() && p.getBottomLeft().getY() >= bottomLeft.getY() ;
	}

	public Set<Character> getWallNeighbors(GameBoard board) {
		Set<Character> set = new HashSet<>();
		if(bottomLeft.getX() == 0) set.add('L'); // left
		if(bottomLeft.getY() == 0) set.add('B'); // bottom
		if(topRight.getX() == board.getNumColumns()) set.add('R'); // right
		if(topRight.getY() == board.getNumRows()) set.add('T'); // top
		return set;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(!o.getClass().equals(GamePiece.class)) return false;
		GamePiece p = (GamePiece) o;
		return this.bottomLeft.equals(p.getBottomLeft()) && this.topRight.equals(p.getTopRight()) && this.color == p.isColor();
	}
	
	@Override
	public int hashCode() { // Not a great hashcode but it's better than the auto-generated hashcode because this one will hash the same pieces to the same value
		return bottomLeft.hashCode() * 29 + topRight.hashCode() * 71 + (color ? 1 : 0);
	}
	
}
