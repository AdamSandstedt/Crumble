package game;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GameBoard extends JPanel {
	private Set<GamePiece> gamePieces;
	private boolean currentTurn;  // Same color boolean as in GamePiece 0=white, 1=black
	private ArrayList<JButton> buttons;
	private String currentAction;
	private JFrame frame = new JFrame();
	private Rectangle boardOutline;
	private BoardPoint firstSelectionPoint, secondSelectionPoint;
	private boolean showSplitLine;
	private boolean showJoinRect;
	private Point mousePosition;
	private boolean splitDirection; // 0=vertical, 1=horizontal
	
	public static final Point TURN_TEXT_POSITION = new Point(650, 100);
	
	private ActionListener buttonListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) { 
			currentAction = e.getActionCommand();
			if(currentAction.equals("end turn")) {
				currentTurn = !currentTurn;
				currentAction = "split";
				buttons.get(0).setEnabled(true);
				buttons.get(1).setEnabled(true);
				buttons.get(2).setEnabled(false);
				buttons.get(3).setEnabled(false);
				frame.repaint();
			}
			else if(currentAction.equals("split") || currentAction.equals("join")) {
				firstSelectionPoint = null;
				secondSelectionPoint = null;
				showSplitLine = false;
				showJoinRect = false;
				frame.repaint();
			}
		}
	};
	
	GameBoard() {
		gamePieces = new HashSet<GamePiece>();
		buttons = new ArrayList<JButton>();
		this.initialize();  // Not sure if this is good practice or not, maybe I should make the user call it
	}
	
	public void initialize() {
		currentTurn = true;	// black goes first
		currentAction = "split";
		boardOutline = new Rectangle(GamePiece.X_OFFSET, GamePiece.Y_OFFSET, 600, 600);
		firstSelectionPoint = null;
		secondSelectionPoint = null;
		showSplitLine = false;
		showJoinRect = false;
		splitDirection = false;
		
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
		button.addActionListener(buttonListener);
		buttons.add(button);
		
		button = new JButton("Join");
		button.setBounds(650, 300, 120, 50);
		button.setActionCommand("join");
		button.addActionListener(buttonListener);
		buttons.add(button);
		
		button = new JButton("Swap");
		button.setBounds(650, 400, 120, 50);
		button.setActionCommand("swap");
		button.setEnabled(false);
		button.addActionListener(buttonListener);
		buttons.add(button);
		
		button = new JButton("End Turn");
		button.setBounds(650, 500, 120, 50);
		button.setActionCommand("end turn");
		button.setEnabled(false);
		button.addActionListener(buttonListener);
		buttons.add(button);
	}
	
	public void paintComponent(Graphics g) {
		for(GamePiece piece: gamePieces) {
			piece.draw(g);
		}
		this.draw(g);
	}
	
	public void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		String turnText;	//Set the turn text to the correct color and display it on the board
		if(currentTurn) turnText = "Black's Turn";
		else turnText = "White's Turn";
		g.setColor(Color.black);
		g.setFont(g.getFont().deriveFont((float)20));
		g.drawString(turnText, TURN_TEXT_POSITION.x, TURN_TEXT_POSITION.y);
		g.drawRect(boardOutline.x, boardOutline.y, boardOutline.width, boardOutline.height);
		if(showSplitLine) {
			int x1, y1, x2, y2;
			x1 = firstSelectionPoint.toPoint().x;
			y1 = firstSelectionPoint.toPoint().y;
			BoardPoint p = getNearestPoint(new BoardPoint(mousePosition));
			x2 = p.toPoint().x;
			y2 = p.toPoint().y;
			g2.setColor(Color.red);
			g2.setStroke(new BasicStroke(4));
			g2.drawLine(x1, y1, x2, y2);
		}
		if(showJoinRect) {
			int x1, y1, width, height;
			BoardPoint p = getNearestPoint(new BoardPoint(mousePosition));
			x1 = firstSelectionPoint.toPoint().x;
			y1 = p.toPoint().y;
			width = p.toPoint().x - x1;
			height = firstSelectionPoint.toPoint().y - y1;
			g2.setColor(Color.red);
			g2.setStroke(new BasicStroke(4));
			g2.drawRect(x1, y1, width, height);
		}
	}

	public static void main(String[] args) {
		GameBoard board = new GameBoard();
		
		board.frame.setContentPane(board);
		board.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		board.frame.setSize(800, 800);
		board.frame.setVisible(true);
		
		for(JButton button: board.buttons) {
			board.frame.add(button);
		}
		
		board.frame.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Point p = e.getPoint();
				p.translate(0, -22);
				if(board.boardOutline.contains(p)) {
					BoardPoint click = new BoardPoint(p);
					if(board.currentAction.equals("split") || board.currentAction.equals("join")) {
						if(board.firstSelectionPoint == null) {
							board.firstSelectionPoint = board.getNearestPoint(click);
						}
						else {
							board.secondSelectionPoint = board.getNearestPoint(click);
							if(board.currentAction.equals("split")) board.split();
							else board.join();
						}
					}
					else if(board.currentAction.equals("swap")) {
						
					}
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}
		});
		
		board.frame.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				if(board.firstSelectionPoint != null) {
					if(board.currentAction.equals("split")) {
						board.mousePosition = e.getPoint();
						board.mousePosition.translate(0, -22);
						board.showSplitLine = true;
						board.frame.repaint();
					}
					else if(board.currentAction.equals("join")) {
						board.mousePosition = e.getPoint();
						board.mousePosition.translate(0, -22);
						board.showJoinRect = true;
						board.frame.repaint();
					}
				}
				
			}
			
		});
	}

	public void join() {
		showJoinRect = false;
		currentAction = "swap";
		buttons.get(0).setEnabled(false);
		buttons.get(1).setEnabled(false);
		buttons.get(2).setEnabled(true);
		buttons.get(3).setEnabled(true);
		firstSelectionPoint = null;
		secondSelectionPoint = null;
		frame.repaint();
	}

	public void split() {
		showSplitLine = false;
		currentAction = "swap";
		buttons.get(0).setEnabled(false);
		buttons.get(1).setEnabled(false);
		buttons.get(2).setEnabled(true);
		buttons.get(3).setEnabled(true);
		firstSelectionPoint = null;
		secondSelectionPoint = null;
		frame.repaint();
	}
	
	public BoardPoint getNearestPoint(BoardPoint point) {
		double minSquareDistance = 100;
		BoardPoint nearestPoint = null;
		if(firstSelectionPoint == null) {
			secondSelectionPoint = null;
			switch(currentAction) {
			case "split":
				for(GamePiece piece: gamePieces) {
					if(piece.isColor() == currentTurn) {
						if(point.distanceSq(piece.getHorizontalSplitStart()) < minSquareDistance) {
							splitDirection = true;
							nearestPoint = piece.getHorizontalSplitStart();
							minSquareDistance = point.distanceSq(piece.getHorizontalSplitStart());
						}
						if(point.distanceSq(piece.getVerticalSplitStart()) < minSquareDistance) {
							splitDirection = false;
							nearestPoint = piece.getVerticalSplitStart();
							minSquareDistance = point.distanceSq(piece.getVerticalSplitStart());
						}
					}
				}
				break;
			case "join":
				for(GamePiece piece: gamePieces) {
					if(piece.isColor() == currentTurn) {
						if(point.distanceSq(piece.getBottomLeft()) < minSquareDistance) {
							nearestPoint = piece.getBottomLeft();
							minSquareDistance = point.distanceSq(piece.getBottomLeft());
						}
						if(point.distanceSq(piece.getBottomLeft()) < minSquareDistance) {
							nearestPoint = piece.getBottomLeft();
							minSquareDistance = point.distanceSq(piece.getBottomLeft());
						}
					}
				}
				break;
			}
		}
		else {
			switch(currentAction) {
			case "split":
				for(GamePiece piece: gamePieces) {
					if(piece.isColor() == currentTurn) {
						if(splitDirection) {
							BoardPoint end = piece.getHorizontalSplitEnd();
							if(end.getY() == firstSelectionPoint.getY() && end.getX() > firstSelectionPoint.getX())
								if(point.distanceSq(end) < minSquareDistance) {
									nearestPoint = end;
									minSquareDistance = point.distanceSq(end);
								}
						}
						else {
							BoardPoint end = piece.getVerticalSplitEnd();
							if(end.getX() == firstSelectionPoint.getX() && end.getY() > firstSelectionPoint.getY())
								if(point.distanceSq(end) < minSquareDistance) {
									nearestPoint = end;
									minSquareDistance = point.distanceSq(end);
								}
						}
							
					}
				}
				break;
			case "join":
				for(GamePiece piece: gamePieces) {
					if(piece.isColor() == currentTurn) {
						if(piece.getTopRight().getX() > firstSelectionPoint.getX() && piece.getTopRight().getY() > firstSelectionPoint.getY()) {
							if(point.distanceSq(piece.getTopRight()) < minSquareDistance) {
								nearestPoint = piece.getTopRight();
								minSquareDistance = point.distanceSq(piece.getTopRight());
							}
						}
					}
				}
				break;
			}
		}
		return nearestPoint;
	}
	
}
