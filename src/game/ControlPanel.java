package game;

import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import game.GameBoard.ButtonListener;

public class ControlPanel extends JPanel {
	private ArrayList<JButton> buttons;
	private ButtonListener buttonListener;
	private GameBoard gameBoard;
	private CrumbleGame crumbleGame;
	private TurnTextPanel turnTextPanel;
	private UndoRedoPanel undoRedoPanel;

	public ControlPanel() {
		buttons = new ArrayList<>();
		this.setLayout(new GridLayout(6,1));
		
		turnTextPanel = new TurnTextPanel();
		add(turnTextPanel);
		
		undoRedoPanel = new UndoRedoPanel();
		add(undoRedoPanel);
		
		JButton button = new JButton("Split");
		button.setActionCommand("split");
		buttons.add(button);
		
		button = new JButton("Join");
		button.setActionCommand("join");
		buttons.add(button);
		
		button = new JButton("Swap");
		button.setActionCommand("swap");
		button.setEnabled(false);
		buttons.add(button);
		
		button = new JButton("End Turn");
		button.setActionCommand("end turn");
		button.setEnabled(false);
		buttons.add(button);
		
		for(int i = 0; i < buttons.size(); i++) {
			add(buttons.get(i));
		}
	}
	
	public ControlPanel(CrumbleGame crumbleGame) {
		this(); // call constructor with no arguments
		this.crumbleGame = crumbleGame;
		this.gameBoard = crumbleGame.getBoard();
		if(gameBoard != null) {
			gameBoard.setControlPanel(this);
			this.buttonListener = gameBoard.getButtonListener();
			for(JButton button: buttons) {
				button.addActionListener(buttonListener);
			}
			undoRedoPanel.getUndoButton().addActionListener(buttonListener);
			undoRedoPanel.getRedoButton().addActionListener(buttonListener);
		}
	}
	
	public void setCurrentTurn(String currentTurn) {
		turnTextPanel.setCurrentTurn(currentTurn);
	}

	public ArrayList<JButton> getButtons() {
		return buttons;
	}

	public void setGameBoard(GameBoard gameBoard) {
		this.gameBoard = gameBoard;
		gameBoard.setControlPanel(this);
		setButtonListener(gameBoard.getButtonListener());
		
	}

	public void setButtonListener(ButtonListener buttonListener) {
		for(JButton button: buttons) {
			button.removeActionListener(this.buttonListener);
		}
		this.buttonListener = buttonListener;
		for(JButton button: buttons) {
			button.addActionListener(buttonListener);
		}
	}
	
	public class TurnTextPanel extends JPanel {
		JTextArea textArea;
		
		public TurnTextPanel() {
			setLayout(new GridLayout());
			textArea = new JTextArea();
			textArea.setText("Black's" + System.lineSeparator() + "Turn");
			textArea.setEditable(false);
			textArea.setFont(getFont().deriveFont((float)30));
			add(textArea);
		}
		
		public void setCurrentTurn(String currentTurn) {
			textArea.setText(currentTurn + System.lineSeparator() + "Turn");
			repaint();
		}
	}
	
	public class UndoRedoPanel extends JPanel {
		private JButton undoButton;
		private JButton redoButton;
		
		public JButton getUndoButton() {
			return undoButton;
		}

		public JButton getRedoButton() {
			return redoButton;
		}

		public UndoRedoPanel() {
			setLayout(new GridLayout(1,2));
			
			undoButton = new JButton("Undo");
			undoButton.setActionCommand("undo");
			undoButton.setEnabled(false);
			add(undoButton);
			
			redoButton = new JButton("Redo");
			redoButton.setActionCommand("redo");
			redoButton.setEnabled(false);
			add(redoButton);

		}
	}
}
