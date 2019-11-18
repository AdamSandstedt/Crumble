package game;

import java.awt.Point;

public class BoardPoint {
	private double x;
	private double y;
	private GameBoard board;
	
	BoardPoint(double x, double y, GameBoard board) {
		this.x = x;
		this.y = y;
		this.board = board;
	}
	
	BoardPoint(Point p, GameBoard board) {
		this.board = board;
		if(p != null) {
			this.x = ((double)p.x - GamePiece.X_OFFSET)/board.getxConversion();
			this.y = ((double)GamePiece.Y_OFFSET + board.getBoardHeight() - p.y)/board.getyConversion();
		}
		else {
			this.x = 0;
			this.y = 0;
		}
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	@Override
	public String toString() {
		return "Point [x=" + x + ", y=" + y + "]";
	}

	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(!o.getClass().equals(BoardPoint.class)) return false;
		BoardPoint p = (BoardPoint) o;
		return this.x == p.getX() && this.y == p.getY();
	}
	
	@Override
	public int hashCode() { // Not a great hashcode but it's better than the auto-generated hashcode because this one will hash the same points to the same value
		return Double.hashCode(x) * 37 + Double.hashCode(y) * 89;
	}
	
	public double distanceSq(BoardPoint p) {
		double dx = p.x - this.x;
		double dy = p.y - this.y;
		return dx*dx + dy*dy;
	}
	
	public Point toPoint() {
		return new Point((int)(GamePiece.X_OFFSET+board.getxConversion()*x), (int)(board.getBoardHeight()+GamePiece.Y_OFFSET-board.getyConversion()*y));
	}
	
}
