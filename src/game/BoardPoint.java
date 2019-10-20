package game;

public class BoardPoint {
	private double x;
	private double y;
	
	BoardPoint(double x, double y) {
		this.x = x;
		this.y = y;
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
	
}
