package game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class GameBoard extends JPanel {
	private Set<GamePiece> gamePieces;
	private boolean currentTurn;  // Same color boolean as in GamePiece 0=white, 1=black
	private Set<Button> buttons;
	
	public static final Point TURN_TEXT_POSITION = new Point(650, 100);
	
	GameBoard() {
		gamePieces = new HashSet<GamePiece>();
		buttons = new HashSet<Button>();
		this.initialize();  // Not sure if this is good practice or not, maybe I should make the user call it
	}
	
	public void initialize() {
		currentTurn = true;	// black goes first
		
		GamePiece newPiece;
		String location;
		boolean color = false;
		gamePieces.clear();
		for(double x = 0; x < 6; x++) {
			for(double y = 0; y < 6; y++) {
				if(y == 0) location = "" + (int)x;
				else location = (int)x + "," + (int)y;
				newPiece = new GamePiece(color, new BoardPoint(x,y), new BoardPoint(x+1,y+1), location);
				color = !color;
				gamePieces.add(newPiece);
			}
			color = !color;
		}
		
		Button button = new Button(ButtonStatus.ACTIVE, new Point(650, 250), new Point(770, 200), "Split");
		buttons.add(button);
		button = new Button(ButtonStatus.INACTIVE, new Point(650, 350), new Point(770, 300), "JOIN");
		buttons.add(button);
		button = new Button(ButtonStatus.DISABLED, new Point(650, 450), new Point(770, 400), "Swap");
		buttons.add(button);
		button = new Button(ButtonStatus.DISABLED, new Point(650, 550), new Point(770, 500), "End Turn");
		buttons.add(button);
	}
	
	public void paintComponent(Graphics g) {
		for(GamePiece piece: gamePieces) {
			piece.draw(g);
		}
		for(Button button: buttons) {
			button.draw(g);
		}
		this.draw(g);
	}
	
	public void draw(Graphics g) {
		String turnText;	//Set the turn text to the correct color and display it on the board
		if(currentTurn) turnText = "Black's Turn";
		else turnText = "White's Turn";
		g.setColor(Color.black);
		g.setFont(g.getFont().deriveFont((float)20));
		g.drawString(turnText, TURN_TEXT_POSITION.x, TURN_TEXT_POSITION.y);
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setContentPane(new GameBoard());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 800);
		frame.setVisible(true);
		
		java.awt.Point a = frame.getMousePosition();
		
	}
	
}
