package game;

import java.awt.BorderLayout;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.*;

import game.CrumbleGame.MenuBarListener;

/**
 * @author Adam Sandstedt
 *
 */
public class CrumbleGame extends JFrame {
	GameBoard board;
	ControlPanel controlPanel;
	MenuBar menu;
	ArrayList<String> moveNotations;

	public CrumbleGame() {
		moveNotations = new ArrayList<>();
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		
		menu = new CrumbleMenuBar();
		this.setMenuBar(menu);
		
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
	
	public class CrumbleMenuBar extends MenuBar {
		Menu file;
		Menu view;
		
		public CrumbleMenuBar() {
			file = createFileMenu();
			add(file);
			
			view = createViewMenu();
			add(view);
		}
		
		private Menu createFileMenu() {
			Menu menu = new Menu("File");
			
			MenuItem item = new MenuItem("Save");
			menu.add(item);
			
			item = new MenuItem("Load");
			menu.add(item);
			
			return menu;
		}
		
		private Menu createViewMenu() {
			Menu menu = new Menu("View");
			
			MenuItem item = new MenuItem("Cell Notations");
			item.setActionCommand("view_notations");
			item.addActionListener(new MenuBarListener());
			menu.add(item);
			
			return menu;
		}
	}
	
	public class MenuBarListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String action = e.getActionCommand();
			if(action.equals("view_notations")) {
				board.toggleNotations();
			}
		}

	}

	public void addMove(String currentMoveNotation) {
		moveNotations.add(currentMoveNotation);
		System.out.println(currentMoveNotation);
	}

}
