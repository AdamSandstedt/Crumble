package game;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * @author Adam Sandstedt
 *
 */
public class CrumbleGame extends JFrame {
	private GameBoard board;
	private ControlPanel controlPanel;
	private MenuBar menu;
	private ArrayList<String> moveNotations;
	private JFileChooser fileChooser;
	private int numRows = 0;
	private int numColumns = 0;
  private int historyIndex;

	public CrumbleGame() {
		moveNotations = new ArrayList<>();
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		fileChooser = new JFileChooser(){
		    @Override
		    public void approveSelection(){
		        File f = getSelectedFile();
		        if(f.exists() && getDialogType() == SAVE_DIALOG){
		            int result = JOptionPane.showConfirmDialog(this,"The file exists, overwrite?","Existing file",JOptionPane.YES_NO_OPTION);
		            switch(result){
		                case JOptionPane.YES_OPTION:
		                    super.approveSelection();
		                    return;
		                case JOptionPane.NO_OPTION:
		                    return;
		                case JOptionPane.CLOSED_OPTION:
		                    return;
		            }
		        }
		        super.approveSelection();
		    }
		};
		fileChooser.setDialogTitle("Select text file to save game to:");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setFileFilter(new FileNameExtensionFilter("Text file","txt"));

		menu = new CrumbleMenuBar();
		this.setMenuBar(menu);

		board  = new GameBoard(this);
		add(board, BorderLayout.CENTER);

		controlPanel = new ControlPanel(this);
		add(controlPanel, BorderLayout.EAST);

    historyIndex = 0;
    
		this.pack();
		setVisible(true);
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
		MenuBarListener menuBarListener;
		Menu file;
		Menu edit;
		Menu view;

		public CrumbleMenuBar() {
			menuBarListener = new MenuBarListener();
			file = createFileMenu();
			add(file);

			edit = createEditMenu();
			add(edit);

			view = createViewMenu();
			add(view);
		}

		private Menu createFileMenu() {
			Menu menu = new Menu("File");

			MenuItem item = new MenuItem("Save");
			item.setActionCommand("save");
			item.addActionListener(menuBarListener);
			menu.add(item);

			item = new MenuItem("Load");
			item.setActionCommand("load");
			item.addActionListener(menuBarListener);
			menu.add(item);

			return menu;
		}

		private Menu createEditMenu() {
			Menu menu = new Menu("Edit");

			MenuItem item = new MenuItem("Board Size");
			item.setActionCommand("edit_board_size");
			item.addActionListener(menuBarListener);
			menu.add(item);

			return menu;
		}

		private Menu createViewMenu() {
			Menu menu = new Menu("View");

			MenuItem item = new MenuItem("Cell Notations");
			item.setActionCommand("view_notations");
			item.addActionListener(menuBarListener);
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
			else if(action.equals("save")) {
				saveFile();
			}
			else if(action.equals("load")) {
				loadFile();
			}
			else if(action.equals("edit_board_size")) {
				editBoardSize();
			}
		}

	}

	public void addMove(String currentMoveNotation) {
		while(historyIndex != moveNotations.size()) moveNotations.remove(moveNotations.size()-1);
		historyIndex += 1;
		moveNotations.add(currentMoveNotation);
		controlPanel.enableUndo(true);
		controlPanel.enableRedo(false);

		System.out.println(currentMoveNotation);
	}

	public void saveFile() {
		if(fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			try {
	            if (!file.exists()) {
	                file.createNewFile();
	            }
	            FileWriter fw = new FileWriter(file);

	            if(!moveNotations.isEmpty()) fw.write(moveNotations.get(0));
	            for(int i = 1; i < moveNotations.size(); i++) {
	            	fw.write(System.lineSeparator() + moveNotations.get(i));
	            }
	            fw.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
	}

	public void loadFile() {
		if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			try {
	            if (!file.exists()) {
	                return;
	            }
	            BufferedReader br = new BufferedReader(new FileReader(file));

	            ArrayList<String> newNotations = new ArrayList<>();
	            String line = br.readLine();
	            while(line != null) {
	            	newNotations.add(line);
	            	line = br.readLine();
	            }
	            loadGameFromNotations(newNotations);
	            moveNotations = newNotations;
	            historyIndex = newNotations.size();
	            br.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
	}

	private void loadGameFromNotations(ArrayList<String> notations) {
		setGameBoard(new GameBoard(this));
		controlPanel.reset();

		int originalSize = moveNotations.size();
		for(int i = 0; i < notations.size(); i++) {
			board.doMove(notations.get(i));
		}
		while(moveNotations.size() > originalSize) {
			moveNotations.remove(moveNotations.size() - 1);
			historyIndex -= 1;
		}
	}

	public int getNumRows() {
		return numRows;
	}

	public int getNumColumns() {
		return numColumns;
	}

	public void editBoardSize() {
		SizeDialog dialog = new SizeDialog(board, this);
		dialog.setVisible(true);
	}

	public void setNumRows(int numRows) {
		this.numRows = numRows;
		setGameBoard(new GameBoard(this));
		moveNotations.clear();
		historyIndex = 0;
		controlPanel.reset();
	}

	public void setNumColumns(int numColumns) {
		this.numColumns = numColumns;
		setGameBoard(new GameBoard(this));
		moveNotations.clear();
		historyIndex = 0;
		controlPanel.reset();
	}

	private void setGameBoard(GameBoard gameBoard) {
		remove(board);
		board = gameBoard;
		add(board);
		controlPanel.setGameBoard(board);
		this.revalidate();
	}

	public class SizeDialog extends JDialog {
		private GameBoard board;
		private CrumbleGame game;
		private JSpinner columnSpinner, rowSpinner;

		public SizeDialog(GameBoard board, CrumbleGame game) {
			this.board = board;
			this.game = game;

			setLayout(new GridLayout(1,2));
			columnSpinner = new JSpinner(new SpinnerNumberModel(6, 2, 16, 1));
			columnSpinner.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					game.setNumColumns((int)((JSpinner)e.getSource()).getValue());
					game.setNumColumns((int)((JSpinner)e.getSource()).getValue());
					rowSpinner.setValue((int)((JSpinner)e.getSource()).getValue());
				}
			});
			add(columnSpinner);

			rowSpinner = new JSpinner(new SpinnerNumberModel(6, 2, 16, 1));
			rowSpinner.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					game.setNumRows((int)((JSpinner)e.getSource()).getValue());
				}
			});
			add(rowSpinner);
			this.setSize(100, 100);
		}

	}

	public void loadState(int index) {
		if(index > moveNotations.size()) historyIndex = moveNotations.size();
		else if(index < 0) historyIndex = 0;
		else historyIndex = index;
		
		ArrayList<String> tempNotations = new ArrayList<>(moveNotations.subList(0, historyIndex));
		loadGameFromNotations(tempNotations);
		
		controlPanel.enableUndo(historyIndex > 0);
		controlPanel.enableRedo(historyIndex < moveNotations.size());
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
