package game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class GamePiece {
	private boolean color; // For now, 0=white, 1=black, but a string or enum might be easier to read
	private BoardPoint bottomLeft;
	private BoardPoint topRight;
	private Notation notation;
	private BoardPoint horizontalSplitStart, verticalSplitStart;
	private BoardPoint horizontalSplitEnd, verticalSplitEnd;
	private String shape;
	private Set<GamePiece> neighbors;
	private Set<Character> wallNeighbors;
	private Set<GamePiece> surrounding;
	private static boolean showNotation = false;
	
	public static final int X_OFFSET = 20;
	public static final int Y_OFFSET = 20;
	
	public GamePiece(boolean color, BoardPoint bottomLeft, BoardPoint topRight, Notation notation, Set<GamePiece> gamePieces) {
		this.color = color;
		this.bottomLeft = bottomLeft;
		this.topRight = topRight;
		this.notation = notation;
		double width = topRight.getX()-bottomLeft.getX();
		double height = topRight.getY()-bottomLeft.getY();
		if(width == height) {
			shape = "square";
			horizontalSplitStart = new BoardPoint(bottomLeft.getX(), (bottomLeft.getY()+topRight.getY())/2);
			verticalSplitStart = new BoardPoint((bottomLeft.getX()+topRight.getX())/2, bottomLeft.getY());
			horizontalSplitEnd = new BoardPoint(topRight.getX(), (bottomLeft.getY()+topRight.getY())/2);
			verticalSplitEnd = new BoardPoint((bottomLeft.getX()+topRight.getX())/2, topRight.getY());
		}
		else if(height == 2*width) {
			shape = "tall";
			horizontalSplitStart = new BoardPoint(bottomLeft.getX(), (bottomLeft.getY()+topRight.getY())/2);
			horizontalSplitEnd = new BoardPoint(topRight.getX(), (bottomLeft.getY()+topRight.getY())/2);
		}
		else {
			shape = "wide";
			verticalSplitEnd = new BoardPoint((bottomLeft.getX()+topRight.getX())/2, topRight.getY());
			verticalSplitStart = new BoardPoint((bottomLeft.getX()+topRight.getX())/2, bottomLeft.getY());
		}
		neighbors = new HashSet<GamePiece>();
		for(GamePiece piece: gamePieces) {
			if(isNeighbor(piece)) {
				neighbors.add(piece);
				piece.getNeighbors().add(this);
			}
		}
		surrounding = new HashSet<GamePiece>();
		for(GamePiece piece: neighbors) {
			surrounding.add(piece);
			piece.getSurrounding().add(this);
		}
		for(GamePiece piece: gamePieces) {
			if(piece.getBottomLeft().getX() == topRight.getX() && piece.getBottomLeft().getY() == topRight.getY() ||
			   piece.getBottomLeft().getX() == topRight.getX() && piece.getTopRight().getY() == bottomLeft.getY() ||
			   piece.getTopRight().getX() == bottomLeft.getX() && piece.getBottomLeft().getY() == topRight.getY() ||
			   piece.getTopRight().getX() == bottomLeft.getX() && piece.getTopRight().getY() == bottomLeft.getY() ) {
				surrounding.add(piece);
				piece.getSurrounding().add(this);
			}
		}
		wallNeighbors = new HashSet<>();
		if(bottomLeft.getX() == 0) wallNeighbors.add('L'); // left
		if(bottomLeft.getY() == 0) wallNeighbors.add('B'); // bottom
		if(topRight.getX() == 6) wallNeighbors.add('R'); // right
		if(topRight.getY() == 6) wallNeighbors.add('T'); // top
	}

	public Set<GamePiece> getSurrounding() {
		return surrounding;
	}

	public Set<GamePiece> getNeighbors() {
		return neighbors;
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

	public void setHorizontalSplitStart(BoardPoint horizontalSplitStart) {
		this.horizontalSplitStart = horizontalSplitStart;
	}

	public BoardPoint getVerticalSplitStart() {
		return verticalSplitStart;
	}

	public void setVerticalSplitStart(BoardPoint verticalSplitStart) {
		this.verticalSplitStart = verticalSplitStart;
	}

	public BoardPoint getHorizontalSplitEnd() {
		return horizontalSplitEnd;
	}

	public void setHorizontalSplitEnd(BoardPoint horizontalSplitEnd) {
		this.horizontalSplitEnd = horizontalSplitEnd;
	}

	public BoardPoint getVerticalSplitEnd() {
		return verticalSplitEnd;
	}

	public void setVerticalSplitEnd(BoardPoint verticalSplitEnd) {
		this.verticalSplitEnd = verticalSplitEnd;
	}

	public boolean isColor() {
		return color;
	}

	@Override
	public String toString() {
		return "GamePiece [color=" + color + ", bottomLeft=" + bottomLeft + ", topRight=" + topRight + ", location="
				+ notation + "]";
	}

	public void setColor(boolean color) {
		this.color = color;
	}

	public BoardPoint getBottomLeft() {
		return bottomLeft;
	}

	public BoardPoint getTopRight() {
		return topRight;
	}

	public Notation getNotation() {
		return notation;
	}

	public void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		int x1 = (int)(X_OFFSET + 100*bottomLeft.getX());
		int x2 = (int)(X_OFFSET + 100*topRight.getX());
		int y1 = (int)(Y_OFFSET + 600 - 100*topRight.getY());
		int y2 = (int)(Y_OFFSET + 600 - 100*bottomLeft.getY());
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
			g2.drawString(notation.toString(), x1+5, y2-5);
		}
		
	}

	public static boolean isShowNotation() {
		return showNotation;
	}

	public static void setShowNotation(boolean showNotation) {
		GamePiece.showNotation = showNotation;
	}

	public boolean contains(Point p) {
		BoardPoint bp = new BoardPoint(p);
		return contains(bp);
	}

	public boolean contains(BoardPoint p) {
		return p.getX() < topRight.getX() && p.getX() > bottomLeft.getX() &&
		       p.getY() < topRight.getY() && p.getY() > bottomLeft.getY() ;
	}
	
	public boolean contains(GamePiece p) {
		return p.getTopRight().getX() <= topRight.getX() && p.getBottomLeft().getX() >= bottomLeft.getX() &&
		       p.getTopRight().getY() <= topRight.getY() && p.getBottomLeft().getY() >= bottomLeft.getY() ;
	}

	public Set<Character> getWallNeighbors() {
		return wallNeighbors;
	}

	public void setNotation(Notation notation) {
		this.notation = notation;
	}
	
}
