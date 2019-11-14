package game;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;

import game.GameBoard.ButtonListener;

public class ControlPanel extends JPanel {
	private ArrayList<JButton> buttons;
	private ButtonListener buttonListener;
	private GameBoard gameBoard;
	private CrumbleGame crumbleGame;

	public ControlPanel() {
		buttons = new ArrayList<>();
		this.setLayout(new GridLayout(4,1));
		
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
		}
	}
	
	public ArrayList<JButton> getButtons() {
		return buttons;
	}

	public void setGameBoard(GameBoard gameBoard) {
		this.gameBoard = gameBoard;
	}

	public void setButtonListener(ButtonListener buttonListener) {
		this.buttonListener = buttonListener;
	}
}
