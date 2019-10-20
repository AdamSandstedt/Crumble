package game;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GameBoard extends JPanel {
	private Set<GamePiece> gamePieces;
	private boolean currentTurn;  // Same color boolean as in GamePiece 0=white, 1=black
	private Set<JButton> buttons;
	
	public static final Point TURN_TEXT_POSITION = new Point(650, 100);
	
	GameBoard() {
		gamePieces = new HashSet<GamePiece>();
		buttons = new HashSet<JButton>();
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
		
		JButton button = new JButton("Split");
		button.setBounds(650, 200, 120, 50);
		button.setActionCommand("split");
		buttons.add(button);
		button = new JButton("Join");
		button.setBounds(650, 300, 120, 50);
		button.setActionCommand("join");
		buttons.add(button);
		button = new JButton("Swap");
		button.setBounds(650, 400, 120, 50);
		button.setActionCommand("swap");
		button.setEnabled(false);
		buttons.add(button);
		button = new JButton("End Turn");
		button.setBounds(650, 500, 120, 50);
		button.setActionCommand("end turn");
		button.setEnabled(false);
		buttons.add(button);
	}
	
	public void paintComponent(Graphics g) {
		for(GamePiece piece: gamePieces) {
			piece.draw(g);
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
		GameBoard board = new GameBoard();
		JFrame frame = new JFrame();
		frame.setContentPane(board);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 800);
		frame.setVisible(true);
		
		for(JButton button: board.buttons) {
			frame.add(button);
		}
		
	}
	
}
