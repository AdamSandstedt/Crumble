package game;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

public class BoardPoint {
	private final double x;
	private final double y;
	private static final Map<BoardPoint, BoardPoint> boardPoints = new HashMap<>();
	
	private BoardPoint(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	private BoardPoint(Point p, GameBoard board) {
		if(p != null) {
			this.x = ((double)p.x - GamePiece.X_OFFSET)/board.getxConversion();
			this.y = ((double)GamePiece.Y_OFFSET + board.getBoardHeight() - p.y)/board.getyConversion();
		}
		else {
			this.x = 0;
			this.y = 0;
		}
	}
	
	public static BoardPoint makePoint(double x, double y) {
		BoardPoint tmp = new BoardPoint(x, y);
		BoardPoint p = boardPoints.get(tmp);
		if(p == null) {
			boardPoints.put(tmp, tmp);
			return tmp;
		} else {
			return p;
		}
	}
	
	public static BoardPoint makePoint(Point p, GameBoard board) {
		if(p != null) {
			return makePoint(((double)p.x - GamePiece.X_OFFSET)/board.getxConversion(), ((double)GamePiece.Y_OFFSET + board.getBoardHeight() - p.y)/board.getyConversion());
		}
		else {
			return makePoint(0, 0);
		}
	}
	
	public static BoardPoint makeTempPoint(double x, double y) {
		return new BoardPoint(x, y);
	}
	
	public static BoardPoint makeTempPoint(Point p, GameBoard board) {
		return new BoardPoint(p, board);
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
	
	public Point toPoint(GameBoard board) {
		return new Point((int)(GamePiece.X_OFFSET+board.getxConversion()*x), (int)(board.getBoardHeight()+GamePiece.Y_OFFSET-board.getyConversion()*y));
	}
	
}
