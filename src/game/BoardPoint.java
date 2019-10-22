package game;

import java.awt.Point;

public class BoardPoint {
	private double x;
	private double y;
	
	BoardPoint(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	BoardPoint(Point p) {
		if(p != null) {
			this.x = ((double)p.x - GamePiece.X_OFFSET)/100;
			this.y = ((double)GamePiece.Y_OFFSET + 600 - p.y)/100;
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

	public boolean equals(BoardPoint p) {
		if(this.x == p.getX() && this.y == p.getY()) return true;
		else return false;
	}
	
	public double distanceSq(BoardPoint p) {
		double dx = p.x - this.x;
		double dy = p.y - this.y;
		return dx*dx + dy*dy;
	}
	
	public Point toPoint() {
		return new Point((int)(GamePiece.X_OFFSET+100*x), (int)(600+GamePiece.Y_OFFSET-100*y));
	}
	
}
