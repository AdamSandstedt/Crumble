package game;

import java.awt.BorderLayout;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;;

/**
 * @author Adam Sandstedt
 *
 */
public class CrumbleGame extends JFrame {
	GameBoard board;
	ControlPanel controlPanel;
	MenuBar menu;
	ArrayList<String> moveNotations;
	JFileChooser fileChooser;

	public CrumbleGame() {
		moveNotations = new ArrayList<>();
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		
		fileChooser = new JFileChooser(){
		    @Override
		    public void approveSelection(){
		        File f = getSelectedFile();
		        if(f.exists() && getDialogType() == SAVE_DIALOG){
		            int result = JOptionPane.showConfirmDialog(this,"The file exists, overwrite?","Existing file",JOptionPane.YES_NO_CANCEL_OPTION);
		            switch(result){
		                case JOptionPane.YES_OPTION:
		                    super.approveSelection();
		                    return;
		                case JOptionPane.NO_OPTION:
		                    return;
		                case JOptionPane.CLOSED_OPTION:
		                    return;
		                case JOptionPane.CANCEL_OPTION:
		                    cancelSelection();
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
		MenuBarListener menuBarListener;
		Menu file;
		Menu view;
		
		public CrumbleMenuBar() {
			menuBarListener = new MenuBarListener();
			file = createFileMenu();
			add(file);
			
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
		}

	}

	public void addMove(String currentMoveNotation) {
		moveNotations.add(currentMoveNotation);
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

}
