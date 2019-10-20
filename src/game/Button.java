package game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

public class Button {
	private ButtonStatus status;
	private Point bottomLeft;
	private Point topRight;
	private String text;
	
	public Button(ButtonStatus status, Point bottomLeft, Point topRight, String text) {
		this.status = status;
		this.bottomLeft = bottomLeft;
		this.topRight = topRight;
		this.text = text;
	}
	
	public void draw(Graphics g) {
		int x1 = bottomLeft.x;
		int x2 = topRight.x;
		int y1 = topRight.y;
		int y2 = bottomLeft.y;
		g.setColor(Color.black);
		g.drawRect(x1, y1, x2-x1, y2-y1);
		g.setFont(g.getFont().deriveFont((float)20));
		switch(status) {
		case ACTIVE:
			g.setColor(Color.blue);
			g.fillRect(x1, y1, x2-x1, y2-y1);
			g.setColor(Color.white);
			break;
		case INACTIVE:
			g.setColor(Color.white);
			g.fillRect(x1, y1, x2-x1, y2-y1);
			g.setColor(Color.black);
			break;
		case DISABLED:
			g.setColor(Color.lightGray);
			g.fillRect(x1, y1, x2-x1, y2-y1);
			g.setColor(Color.black);
			break;
		}
		g.drawString(text, (x1+x2)/2-5*text.length(), (y1+2*y2)/3);
	}
	
}
