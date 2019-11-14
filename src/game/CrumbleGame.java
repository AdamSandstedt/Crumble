package game;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;

/**
 * @author Adam Sandstedt
 *
 */
public class CrumbleGame extends JFrame {
	GameBoard board;
	ControlPanel controlPanel;

	public CrumbleGame() {
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		
		board  = new GameBoard(this);
		add(board, BorderLayout.CENTER);
		
		controlPanel = new ControlPanel(this);
		add(controlPanel, BorderLayout.EAST);
		
		setVisible(true);
		this.pack();
	}

	public static void main(String[] args) {
		CrumbleGame game = new CrumbleGame();
	}

	public GameBoard getBoard() {
		return board;
	}

	public ControlPanel getControlPanel() {
		return controlPanel;
	}

}
