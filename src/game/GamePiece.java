package game;

public class GamePiece {
	private boolean color; // For now, 0=white, 1=black, but a string or enum might be easier to read
	private Point bottomLeft;
	private Point topRight;
	private String location;
	
	public GamePiece(boolean color, Point bottomLeft, Point topRight, String location) {
		this.color = color;
		this.bottomLeft = bottomLeft;
		this.topRight = topRight;
		this.location = location;
	}

	public boolean isColor() {
		return color;
	}

	public void setColor(boolean color) {
		this.color = color;
	}

	public Point getBottomLeft() {
		return bottomLeft;
	}

	public Point getTopRight() {
		return topRight;
	}

	public String getLocation() {
		return location;
	}
	
}
