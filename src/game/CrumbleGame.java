package game;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;

/**
 * @author Adam Sandstedt
 *
 */
public class CrumbleGame extends JFrame {
	private GameBoard board;
	private ControlPanel controlPanel;
	private ArrayList<GameBoard> history;
	private int historyIndex;

	public CrumbleGame() {		
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
				
		board  = new GameBoard(this);
		add(board, BorderLayout.CENTER);
		
		controlPanel = new ControlPanel(this);
		add(controlPanel, BorderLayout.EAST);
		
		history = new ArrayList<>();
		history.add(new GameBoard(board));
		historyIndex = 0;
		
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

	public void saveState() {
		historyIndex += 1;
		while(historyIndex != history.size()) history.remove(history.size()-1);
		history.add(new GameBoard(board));
		controlPanel.enableUndo(true);
		controlPanel.enableRedo(false);
	}
	
	public void loadState(int index) {
		if(index >= history.size()) historyIndex = history.size() - 1;
		else if(index < 0) historyIndex = 0;
		else historyIndex = index;
		
		remove(board);
		board = history.get(historyIndex);
		add(board, BorderLayout.CENTER);
		revalidate();
		
		String currentAction = board.getCurrentAction();
		ArrayList<JButton> buttons = controlPanel.getButtons();
		if(currentAction.equals("split") || currentAction.equals("join")) {
			buttons.get(0).setEnabled(true);
			buttons.get(1).setEnabled(true);
			buttons.get(2).setEnabled(false);
			buttons.get(3).setEnabled(false);
		}
		else {
			buttons.get(0).setEnabled(false);
			buttons.get(1).setEnabled(false);
			buttons.get(2).setEnabled(true);
			buttons.get(3).setEnabled(true);
		}
		controlPanel.enableUndo(historyIndex > 0);
		controlPanel.enableRedo(historyIndex < history.size() - 1);
		repaint();
		board.repaint();
		controlPanel.repaint();
	}
	
	public void loadState(String action) {
		if(action.equals("undo")) {
			loadState(historyIndex - 1);
		}
		else if(action.equals("redo")) {
			loadState(historyIndex + 1);
		}
	}

}
