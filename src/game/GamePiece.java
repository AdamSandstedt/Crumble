package game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class GamePiece {
	private boolean color; // For now, 0=white, 1=black, but a string or enum might be easier to read
	private BoardPoint bottomLeft;
	private BoardPoint topRight;
	private String location;
	private BoardPoint horizontalSplitStart, verticalSplitStart;
	private BoardPoint horizontalSplitEnd, verticalSplitEnd;
	
	public static final int X_OFFSET = 20;
	public static final int Y_OFFSET = 20;
	
	public GamePiece(boolean color, BoardPoint bottomLeft, BoardPoint topRight, String location) {
		this.color = color;
		this.bottomLeft = bottomLeft;
		this.topRight = topRight;
		this.location = location;
		horizontalSplitStart = new BoardPoint(bottomLeft.getX(), (bottomLeft.getY()+topRight.getY())/2);
		verticalSplitStart = new BoardPoint((bottomLeft.getX()+topRight.getX())/2, bottomLeft.getY());
		horizontalSplitEnd = new BoardPoint(topRight.getX(), (bottomLeft.getY()+topRight.getY())/2);
		verticalSplitEnd = new BoardPoint((bottomLeft.getX()+topRight.getX())/2, topRight.getY());
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
				+ location + "]";
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

	public String getLocation() {
		return location;
	}

	public void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		int x1 = X_OFFSET + 100*(int)bottomLeft.getX();
		int x2 = X_OFFSET + 100*(int)topRight.getX();
		int y1 = Y_OFFSET + 600 - 100*(int)topRight.getY();
		int y2 = Y_OFFSET + 600 - 100*(int)bottomLeft.getY();
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
	}
	
}
