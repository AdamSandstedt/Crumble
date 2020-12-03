package game;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import game.GameBoard.ButtonListener;

public class ControlPanel extends JPanel {
	private ButtonListener buttonListener;
	private GameBoard gameBoard;
	private CrumbleGame crumbleGame;
	private TurnTextPanel turnTextPanel;
	private UndoRedoPanel undoRedoPanel;
	private JButton endTurnButton;
	private JTextArea notationTextArea;

	public ControlPanel() {
		this.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 0.05;
		c.gridx = 0;
		c.gridy = 0;
		turnTextPanel = new TurnTextPanel();
		add(turnTextPanel, c);

		c.gridy = 1;
		c.weighty = 0.1;
		undoRedoPanel = new UndoRedoPanel();
		add(undoRedoPanel, c);

		c.gridy = 2;
		c.weighty = 0.2;
		endTurnButton = new JButton("End Turn");
		endTurnButton.setActionCommand("end turn");
		endTurnButton.setEnabled(false);
		add(endTurnButton, c);
		
		c.gridy = 3;
		c.weighty = 1.0;
		notationTextArea = new JTextArea();
		notationTextArea.setEditable(false);
		add(notationTextArea, c);
	}

	public UndoRedoPanel getUndoRedoPanel() {
		return undoRedoPanel;
	}

	public ControlPanel(CrumbleGame crumbleGame) {
		this(); // call constructor with no arguments
		this.crumbleGame = crumbleGame;
		this.gameBoard = crumbleGame.getBoard();
		if(gameBoard != null) {
			gameBoard.setControlPanel(this);
			this.buttonListener = gameBoard.getButtonListener();
			endTurnButton.addActionListener(buttonListener);
			undoRedoPanel.getUndoButton().addActionListener(buttonListener);
			undoRedoPanel.getRedoButton().addActionListener(buttonListener);
		}
	}

	public void setCurrentTurn(String currentTurn) {
		turnTextPanel.setCurrentTurn(currentTurn);
	}

	public JButton getEndTurnButton() {
		return endTurnButton;
	}

	public void setGameBoard(GameBoard gameBoard) {
		this.gameBoard = gameBoard;
		gameBoard.setControlPanel(this);
		setButtonListener(gameBoard.getButtonListener());

	}

	public void setButtonListener(ButtonListener buttonListener) {
		endTurnButton.removeActionListener(this.buttonListener);
		this.buttonListener = buttonListener;
		endTurnButton.addActionListener(buttonListener);
	}

	public void enableUndo(Boolean b) {
		undoRedoPanel.getUndoButton().setEnabled(b);
	}

	public void enableRedo(Boolean b) {
		undoRedoPanel.getRedoButton().setEnabled(b);
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

	public void reset() {
		setCurrentTurn("Black's");
		endTurnButton.setEnabled(false);
		enableUndo(false);
		enableRedo(false);
	}

	public void setNotations(ArrayList<String> moveNotations) {
		notationTextArea.setText("");
		for(String notation: moveNotations) {
			notationTextArea.append(notation + System.lineSeparator());
		}
		repaint();
	}
}
