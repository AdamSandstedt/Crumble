package game;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
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
	private GamePiece firstSelectionPiece, secondSelectionPiece;
	private boolean showSplitLine;
	private boolean showJoinRect;
	private Point mousePosition;
	private boolean splitDirection; // 0=vertical, 1=horizontal
	private boolean showStartPoint;
	private Set<GamePiece> swapStartPieces;
	private Set<Set<GamePiece>> chains;
	
	public static final Point TURN_TEXT_POSITION = new Point(650, 100);
	
	private ActionListener buttonListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) { 
			currentAction = e.getActionCommand();
			if(currentAction.equals("end turn")) {
				currentTurn = !currentTurn;
				currentAction = "split";
				firstSelectionPiece = null;
				showStartPoint = true;
				buttons.get(0).setEnabled(true);
				buttons.get(1).setEnabled(true);
				buttons.get(2).setEnabled(false);
				buttons.get(3).setEnabled(false);
				frame.repaint();
			}
			else if(currentAction.equals("split") || currentAction.equals("join")) {
				firstSelectionPoint = null;
				secondSelectionPoint = null;
				firstSelectionPiece = null;
				secondSelectionPiece = null;
				showSplitLine = false;
				showJoinRect = false;
				showStartPoint = true;
				frame.repaint();
			}
		}
	};
	
	GameBoard() {
		gamePieces = new HashSet<GamePiece>();
		buttons = new ArrayList<JButton>();
		swapStartPieces = new HashSet<GamePiece>();
		chains = new HashSet<Set<GamePiece>>();
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
		showStartPoint = true;
		
		GamePiece newPiece;
		String location;
		boolean color = false;
		gamePieces.clear();
		for(double x = 0; x < 6; x++) {
			for(double y = 0; y < 6; y++) {
				if(y == 0) location = "" + (int)x;
				else location = (int)x + "," + (int)y;
				newPiece = new GamePiece(color, new BoardPoint(x,y), new BoardPoint(x+1,y+1), location, gamePieces);
				Set<GamePiece> newChain = new HashSet<GamePiece>();
				newChain.add(newPiece);
				chains.add(newChain);
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
		g.setColor(getBackground());
		g.fillRect(0, 0, 800, 800);
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
		if(showStartPoint) {
			BoardPoint p = getNearestPoint(new BoardPoint(mousePosition));
			if(p != null) {
				g2.setColor(Color.red);
				g2.fillRect(p.toPoint().x, p.toPoint().y, 5, 5);
			}
		}
		else if(showSplitLine) {
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
		else if(showJoinRect) {
			int x1, y1, width, height;
			BoardPoint p = getNearestPoint(new BoardPoint(mousePosition));
			if(p != null) {
				x1 = firstSelectionPoint.toPoint().x;
				y1 = p.toPoint().y;
				width = p.toPoint().x - x1;
				height = firstSelectionPoint.toPoint().y - y1;
				g2.setColor(Color.red);
				g2.setStroke(new BasicStroke(4));
				g2.drawRect(x1, y1, width, height);
			}
		}
		else if(firstSelectionPiece != null) {
			int x1, y1, x2, y2;
			x1 = firstSelectionPiece.getBottomLeft().toPoint().x;
			y1 = firstSelectionPiece.getTopRight().toPoint().y;
			x2 = firstSelectionPiece.getTopRight().toPoint().x;
			y2 = firstSelectionPiece.getBottomLeft().toPoint().y;
			g2.setColor(Color.red);
			g2.setStroke(new BasicStroke(4));
			g2.drawRect(x1, y1, x2-x1, y2-y1);
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
			public void mouseClicked(MouseEvent e) {}

			@Override
			public void mousePressed(MouseEvent e) {
				Point p = e.getPoint();
				p.translate(0, -22);
				if(board.boardOutline.contains(p)) {
					BoardPoint click = new BoardPoint(p);
					if(board.currentAction.equals("split") || board.currentAction.equals("join")) {
						if(board.firstSelectionPoint == null) {
							board.firstSelectionPoint = board.getNearestPoint(click);
							if(board.currentAction.equals("join") && board.getNearestPoint(click) == null) board.firstSelectionPoint = null;
						}
						else {
							board.secondSelectionPoint = board.getNearestPoint(click);
							if(board.currentAction.equals("split")) board.split();
							else board.join();
						}
					}
					else if(board.currentAction.equals("swap")) {
						if(board.firstSelectionPiece == null) {
							for(GamePiece piece: board.swapStartPieces) {
								if(piece.contains(p)) {
									board.firstSelectionPiece = piece;
									board.frame.repaint();
									board.swapStartPieces.clear();
									break;
								}
							}
						}
						else {
							for(GamePiece piece: board.gamePieces) {
								if(piece.contains(p) && board.validateSwap(board.firstSelectionPiece, piece)) {
									board.secondSelectionPiece = piece;
									break;
								}
							}
							if(board.secondSelectionPiece != null) board.swap();
						}
					}
				}
				if(board.firstSelectionPoint != null) {
					if(board.currentAction.equals("split")) {
						board.showSplitLine = true;
						board.showStartPoint = false;
					}
					else if(board.currentAction.equals("join")) {
						board.showStartPoint = false;
						board.showJoinRect = true;
					}
				}
				else {
					if(board.currentAction.equals("split")) {
						board.showStartPoint = true;
					}
					else if(board.currentAction.equals("join")) {
						board.showJoinRect = false;
						board.showStartPoint = true;
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}
		});
		
		board.frame.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {}

			@Override
			public void mouseMoved(MouseEvent e) {
				board.mousePosition = e.getPoint();
				board.mousePosition.translate(0, -22);
				
				board.frame.repaint();				
			}
			
		});
	}

	public void swap() {
		firstSelectionPiece.setColor(!firstSelectionPiece.isColor());
		secondSelectionPiece.setColor(!secondSelectionPiece.isColor());
		updateChain(firstSelectionPiece);
		updateChain(secondSelectionPiece);
		
		firstSelectionPiece = secondSelectionPiece;
		secondSelectionPiece = null;
		frame.repaint();
	}

	public void join() {
		Set<GamePiece> piecesToJoin = new HashSet<GamePiece>();
		GamePiece p1 = null, p2 = null;
		for(GamePiece piece: gamePieces) {
			if(piece.getBottomLeft().equals(firstSelectionPoint)) p1 = piece;
			if(piece.getTopRight().equals(secondSelectionPoint)) p2 = piece;
		}
		GamePiece newPiece = new GamePiece(p1.isColor(), p1.getBottomLeft(), p2.getTopRight(), "????", gamePieces);
		for(GamePiece piece: gamePieces) {
			if(newPiece.contains(piece)) {
				piecesToJoin.add(piece);
			}
		}
		Set<GamePiece> chain = findChain(p1);
		for(GamePiece piece: piecesToJoin) {
			gamePieces.remove(piece);
			for(GamePiece neighbor: piece.getNeighbors()) {
				neighbor.getNeighbors().remove(piece);
			}
			chain.remove(piece);
		}
		gamePieces.add(newPiece);
		chain.add(newPiece);
		firstSelectionPiece = newPiece;
		
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
		Set<GamePiece> piecesToSplit = new HashSet<GamePiece>();
		if(splitDirection) { // horizontal split
			for(GamePiece piece: gamePieces) {
				if(piece.canSplitHorizontal() && firstSelectionPoint.getY() == piece.getHorizontalSplitStart().getY() &&
				   firstSelectionPoint.getX() <= piece.getBottomLeft().getX() && secondSelectionPoint.getX() >= piece.getTopRight().getX()) {
					piecesToSplit.add(piece);
				}
			}
			GamePiece newPieceBottom, newPieceTop;
			for(GamePiece piece: piecesToSplit) {
				gamePieces.remove(piece);
				for(GamePiece neighbor: piece.getNeighbors()) {
					neighbor.getNeighbors().remove(piece);
				}
				Set<GamePiece> chain = findChain(piece);
				chain.remove(piece);
				newPieceBottom = new GamePiece(piece.isColor(), piece.getBottomLeft(), piece.getHorizontalSplitEnd(), "????", gamePieces);
				newPieceTop = new GamePiece(piece.isColor(), piece.getHorizontalSplitStart(), piece.getTopRight(), "????", gamePieces);
				chain.add(newPieceTop);
				chain.add(newPieceBottom);
				gamePieces.add(newPieceBottom);
				gamePieces.add(newPieceTop);
				swapStartPieces.add(newPieceTop);
				swapStartPieces.add(newPieceBottom);
			}
		}
		else { // vertical
			for(GamePiece piece: gamePieces) {
				if(piece.canSplitVertical() && firstSelectionPoint.getX() == piece.getVerticalSplitStart().getX() &&
				   firstSelectionPoint.getY() <= piece.getBottomLeft().getY() && secondSelectionPoint.getY() >= piece.getTopRight().getY()) {
					piecesToSplit.add(piece);
				}
			}
			GamePiece newPieceLeft, newPieceRight;
			for(GamePiece piece: piecesToSplit) {
				gamePieces.remove(piece);
				for(GamePiece neighbor: piece.getNeighbors()) {
					neighbor.getNeighbors().remove(piece);
				}
				Set<GamePiece> chain = findChain(piece);
				chain.remove(piece);
				newPieceLeft = new GamePiece(piece.isColor(), piece.getBottomLeft(), piece.getVerticalSplitEnd(), "????", gamePieces);
				newPieceRight = new GamePiece(piece.isColor(), piece.getVerticalSplitStart(), piece.getTopRight(), "????", gamePieces);
				chain.add(newPieceRight);
				chain.add(newPieceLeft);
				gamePieces.add(newPieceRight);
				gamePieces.add(newPieceLeft);
				swapStartPieces.add(newPieceRight);
				swapStartPieces.add(newPieceLeft);
			}
		}
		
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
						if(piece.canSplitHorizontal() && point.distanceSq(piece.getHorizontalSplitStart()) < minSquareDistance) {
							splitDirection = true;
							nearestPoint = piece.getHorizontalSplitStart();
							minSquareDistance = point.distanceSq(piece.getHorizontalSplitStart());
						}
						if(piece.canSplitVertical() && point.distanceSq(piece.getVerticalSplitStart()) < minSquareDistance) {
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
							firstSelectionPoint = piece.getBottomLeft();
							if(getNearestPoint(point) != null) {
								nearestPoint = piece.getBottomLeft();
								minSquareDistance = point.distanceSq(piece.getBottomLeft());
							}
							firstSelectionPoint = null;
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
						if(splitDirection && piece.canSplitHorizontal()) {
							BoardPoint end = piece.getHorizontalSplitEnd();
							if(end.getY() == firstSelectionPoint.getY() && end.getX() > firstSelectionPoint.getX())
								if(point.distanceSq(end) < minSquareDistance && validateSplit(firstSelectionPoint, end)) {
									nearestPoint = end;
									minSquareDistance = point.distanceSq(end);
								}
						}
						else if(!splitDirection && piece.canSplitVertical()) {
							BoardPoint end = piece.getVerticalSplitEnd();
							if(end.getX() == firstSelectionPoint.getX() && end.getY() > firstSelectionPoint.getY())
								if(point.distanceSq(end) < minSquareDistance && validateSplit(firstSelectionPoint, end)) {
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
							if(point.distanceSq(piece.getTopRight()) < minSquareDistance && validateJoin(firstSelectionPoint, piece.getTopRight())) {
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

	private boolean validateJoin(BoardPoint p1, BoardPoint p2) {
		double area = 0;
		int pieceCount = 0;
		double width = p2.getX()-p1.getX();
		double height = p2.getY()-p1.getY();
		for(GamePiece piece: gamePieces) {
			if(p1.getX() <= piece.getBottomLeft().getX() && p2.getX() >= piece.getTopRight().getX() &&
			   p1.getY() <= piece.getBottomLeft().getY() && p2.getY() >= piece.getTopRight().getY() && piece.isColor() == currentTurn) {
				area += (piece.getTopRight().getX()-piece.getBottomLeft().getX())*(piece.getTopRight().getY()-piece.getBottomLeft().getY());
				pieceCount++;
			}
		}
		if(area != width*height) return false;
		else if(pieceCount <= 1) return false;
		else if(width != height && 2*width != height && width != 2*height) return false;
		return true;
	}

	private boolean validateSplit(BoardPoint p1, BoardPoint p2) {
		boolean splitIsValid = true;
		if(splitDirection) {
			for(GamePiece piece: gamePieces) {
				if(p1.getY() < piece.getTopRight().getY() && p1.getY() > piece.getBottomLeft().getY() &&
				   p1.getX() <= piece.getBottomLeft().getX() && p2.getX() >= piece.getTopRight().getX() ) {
					if(piece.canSplitHorizontal() && piece.getHorizontalSplitStart().getY() != p1.getY() || (piece.isColor() != currentTurn)) {
						splitIsValid = false;
						break;
					}
				}
			}
		}
		else {
			for(GamePiece piece: gamePieces) {
				if(p1.getX() < piece.getTopRight().getX() && p1.getX() > piece.getBottomLeft().getX() &&
				   p1.getY() <= piece.getBottomLeft().getY() && p2.getY() >= piece.getTopRight().getY() ) {
					if(piece.canSplitVertical() && piece.getVerticalSplitStart().getX() != p1.getX() || (piece.isColor() != currentTurn)) {
						splitIsValid = false;
						break;
					}
				}
			}
		}
		return splitIsValid;
	}
	
	private boolean validateSwap(GamePiece p1, GamePiece p2) {
		if(p1.getBottomLeft().getY() == p2.getBottomLeft().getY() && p1.getTopRight().getY() == p2.getTopRight().getY() &&
		   (p1.getBottomLeft().getX() == p2.getTopRight().getX() || p1.getTopRight().getX() == p2.getBottomLeft().getX() ) ||
		   p1.getBottomLeft().getX() == p2.getBottomLeft().getX() && p1.getTopRight().getX() == p2.getTopRight().getX() &&
		   (p1.getBottomLeft().getY() == p2.getTopRight().getY() || p1.getTopRight().getY() == p2.getBottomLeft().getY()) ) {
			if(p1.isColor() != p2.isColor()) return true;
			else return false;
		}
		else {					
			return false;
		}
	}
	
	private Set<GamePiece> findChain(GamePiece piece) {
		for(Set<GamePiece> chain: chains) {
			if(chain.contains(piece)) return chain;
		}
		return null;
	}

	private void updateChain(GamePiece piece) {
		Set<GamePiece> oldChain = findChain(piece);
		oldChain.remove(piece);
		if(oldChain.isEmpty()) chains.remove(oldChain);
		else validateChain(oldChain);
		Set<GamePiece> chain = null;
		for(GamePiece neighbor: piece.getNeighbors()) {
			if(neighbor.isColor() == piece.isColor()) {
				chain = findChain(neighbor);
				chain.add(piece);
				break;
			}
		}
		if(chain != null) {
			for(GamePiece neighbor: piece.getNeighbors()) {
				if(neighbor.isColor() == piece.isColor() && findChain(neighbor) != chain) {
					Set<GamePiece> neighborChain = findChain(neighbor);
					for(GamePiece chainPiece: neighborChain) {
						chain.add(chainPiece);
					}
					neighborChain.clear();
				}
			}
			Iterator<Set<GamePiece>> iterator = chains.iterator();
			while(iterator.hasNext()) {
				if(iterator.next().isEmpty()) iterator.remove();
			}
		}
		else {
			Set<GamePiece> newChain = new HashSet<GamePiece>();
			newChain.add(piece);
			chains.add(newChain);
		}
	}

	private void validateChain(Set<GamePiece> chain) {
		Set<Set<GamePiece>> newChains = new HashSet<Set<GamePiece>>();
		for(GamePiece piece: chain) {
			Set<GamePiece> visited = new HashSet<GamePiece>();
			Queue<GamePiece> queue = new LinkedList<GamePiece>();
			queue.add(piece);
			visited.add(piece);
			while(!queue.isEmpty()) {
				GamePiece p = queue.poll();
				for(GamePiece neighbor: p.getNeighbors()) {
					if(neighbor.isColor() == piece.isColor() && !visited.contains(neighbor)) {
						visited.add(neighbor);
						queue.add(neighbor);
					}
				}
			}
			boolean isNewChain = true;
			for(Set<GamePiece> newChain: newChains) {
				if(visited.equals(newChain)) isNewChain = false;
			}
			if(isNewChain) newChains.add(visited);
		}
		Iterator<Set<GamePiece>> iterator = chains.iterator();
		while(iterator.hasNext())
			if(iterator.next().equals(chain)) iterator.remove();
		chains.addAll(newChains);
	}

	private void checkWin() {
		for(Set<GamePiece> chain: chains) {
			Set<Character> edges = new HashSet<Character>();
			boolean color = false;
			for(GamePiece piece: chain) {
				edges.addAll(piece.getWallNeighbors());
				color = piece.isColor();
			}
			if(edges.size() == 4) {
				if(color) System.out.println("Black wins!");
				else System.out.println("White wins!");
			}
		}
	}
	
}
