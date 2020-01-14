package game;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
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
		Menu game;
		Menu edit;
		Menu view;
		Menu help;

		public CrumbleMenuBar() {
			menuBarListener = new MenuBarListener();
			game = createGameMenu();
			add(game);

			edit = createEditMenu();
			add(edit);

			view = createViewMenu();
			add(view);
			
			help = createHelpMenu();
			add(help);
		}

		private Menu createGameMenu() {
			Menu menu = new Menu("Game");
			
			MenuItem item = new MenuItem("New");
			item.setActionCommand("new");
			item.addActionListener(menuBarListener);
			menu.add(item);

			item = new MenuItem("Save");
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
		
		private Menu createHelpMenu() {
			Menu menu = new Menu("Help");

			MenuItem item = new MenuItem("How to play");
			item.setActionCommand("how to play");
			item.addActionListener(menuBarListener);
			menu.add(item);

			item = new MenuItem("Keyboard shortcuts");
			item.setActionCommand("keyboard shortcuts");
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
			else if(action.equals("how to play")) {
				showGameInstructions();
			}
			else if(action.equals("keyboard shortcuts")) {
				showKeyboardShortcuts();
			}
			else if(action.equals("new")) {
				newGameDialog();
			}
		}

	}
	
	public void newGameDialog() {
		NewGameDialog dialog = new NewGameDialog(this, "New Game", Dialog.ModalityType.DOCUMENT_MODAL);
		dialog.setVisible(true);
	}
	
	private class NewGameDialog extends JDialog {
		private JSpinner crumbleLevel, columnSpinner, rowSpinner;
		private JComboBox gameType;

		public NewGameDialog(CrumbleGame crumbleGame, String title, ModalityType modality) {
			super(crumbleGame, title, modality);
			this.setLayout(new GridLayout(3,1));
			
			JPanel panel = new JPanel();
			panel.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.NONE;
			c.weightx = 0.25;
			c.gridx = 0;
			c.gridy = 0;
			crumbleLevel = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
			panel.add(crumbleLevel, c);
			
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 0.75;
			c.gridx = 1;
			c.gridy = 0;
			JTextField textField = new JTextField("0 = standard infinite level crumble");
			textField.setEditable(false);
			textField.setBackground(getBackground());
			panel.add(textField, c);
			panel.setBorder(new TitledBorder(new EtchedBorder(), "Crumble Level"));
			add(panel);
			
			gameType = new JComboBox();
			gameType.addItem("Human vs Human");
			gameType.addItem("Human vs Computer");
			gameType.addItem("Computer vs Computer");
			gameType.setBorder(new TitledBorder(new EtchedBorder(), "Game Type"));
			add(gameType);
			
			JPanel resizePanel = new JPanel();
			resizePanel.setLayout(new GridBagLayout());
			columnSpinner = new JSpinner(new SpinnerNumberModel(6, 2, 16, 1));
			columnSpinner.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					crumbleGame.setNumColumns((int)((JSpinner)e.getSource()).getValue());
					crumbleGame.setNumColumns((int)((JSpinner)e.getSource()).getValue());
					rowSpinner.setValue((int)((JSpinner)e.getSource()).getValue());
				}
			});
			resizePanel.add(columnSpinner);

			rowSpinner = new JSpinner(new SpinnerNumberModel(6, 2, 16, 1));
			rowSpinner.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					crumbleGame.setNumRows((int)((JSpinner)e.getSource()).getValue());
				}
			});
			resizePanel.add(rowSpinner);
			resizePanel.setBorder(new TitledBorder(new EtchedBorder(), "Gameboard dimensions"));
			textField = new JTextField("Standard crumble is 6x6");
			textField.setEditable(false);
			textField.setBackground(getBackground());
			resizePanel.add(textField);
			add(resizePanel);
			
			pack();
		}
		
	}

	public void addMove(String currentMoveNotation) {
		while(historyIndex != moveNotations.size()) moveNotations.remove(moveNotations.size()-1);
		historyIndex += 1;
		moveNotations.add(currentMoveNotation);
		controlPanel.enableUndo(true);
		controlPanel.enableRedo(false);

		controlPanel.setNotations(moveNotations);
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
	            moveNotations = newNotations;
	            historyIndex = newNotations.size();
	            loadGameFromNotations(newNotations);
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
		for(int i = 0; i < notations.size() && i < originalSize; i++) {
			board.doMove(notations.get(i));
		}
		while(moveNotations.size() > originalSize) {
			moveNotations.remove(moveNotations.size() - 1);
			historyIndex -= 1;
		}
		
		controlPanel.enableUndo(historyIndex > 0);
		controlPanel.enableRedo(historyIndex < moveNotations.size());
		controlPanel.setNotations(notations);
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
	
	public void showGameInstructions() {
		InstructionsDialog dialog = new InstructionsDialog();
		dialog.setVisible(true);
	}
	
	public class InstructionsDialog extends JDialog {
		public InstructionsDialog() {
			JTextArea rules = new JTextArea();
			rules.setLineWrap(true);
			try {
				InputStream is = this.getClass().getResourceAsStream("/data/rules.txt");
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
				try {
					String line;
					while((line=reader.readLine()) != null) {
						rules.append(line + System.lineSeparator());
					}
				} finally {
					reader.close();
				}
			} catch(IOException e) {
				System.out.println(e.getMessage());
			}
			add(rules);
			
			this.setSize(900, 700);
		}
	}
	
	public void showKeyboardShortcuts() {
		KeyboardShortcutsDialog dialog = new KeyboardShortcutsDialog();
		dialog.setVisible(true);
	}
	
	public class KeyboardShortcutsDialog extends JDialog {
		public KeyboardShortcutsDialog() {
			JTextArea shortcuts = new JTextArea();
			shortcuts.setLineWrap(true);
			try {
				InputStream is = this.getClass().getResourceAsStream("/data/keyboard_shortcuts.txt");
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
				try {
					String line;
					while((line=reader.readLine()) != null) {
						shortcuts.append(line + System.lineSeparator());
					}
				} finally {
					reader.close();
				}
			} catch(IOException e) {
				System.out.println(e.getMessage());
			}
			add(shortcuts);
			
			this.setSize(300, 300);
		}
	}

}
