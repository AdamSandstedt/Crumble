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
import javax.swing.JPanel;

public class GameBoard extends JPanel {
	private Set<GamePiece> gamePieces;
	private boolean currentTurn;  // Same color boolean as in GamePiece 0=white, 1=black
	private String currentAction;
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
	private int width = 600;
	private int height = 600;
	private ControlPanel controlPanel;
	private CrumbleGame crumbleGame;
	private ButtonListener buttonListener;
	private BoardMouseListener boardMouseListener;
	private BoardMouseMotionListener boardMouseMotionListener;
	
	GameBoard() {
		gamePieces = new HashSet<>();
		swapStartPieces = new HashSet<>();
		chains = new HashSet<>();
		buttonListener = new ButtonListener();
		boardMouseListener = new BoardMouseListener();
		boardMouseMotionListener = new BoardMouseMotionListener();
		
		setPreferredSize(new Dimension(width+GamePiece.X_OFFSET*2, height+GamePiece.Y_OFFSET*2));
		addMouseListener(boardMouseListener);
		addMouseMotionListener(boardMouseMotionListener);
		
		this.initialize();  // Not sure if this is good practice or not, maybe I should make the user call it
	}
	
	public GameBoard(CrumbleGame crumbleGame) {
		this(); // call constructor with no arguments
		this.crumbleGame = crumbleGame;
		this.controlPanel = crumbleGame.getControlPanel();
		if(controlPanel != null) {
			controlPanel.setGameBoard(this);
			controlPanel.setButtonListener(buttonListener);
		}
	}
	
	public GameBoard(GameBoard board) {
		this.gamePieces = new HashSet<>(board.gamePieces);
		this.currentTurn = board.currentTurn;
		this.currentAction = board.currentAction;
		this.boardOutline = board.boardOutline;
		this.firstSelectionPoint = board.firstSelectionPoint;
		this.secondSelectionPoint = board.secondSelectionPoint;
		this.firstSelectionPiece = board.firstSelectionPiece;
		this.secondSelectionPiece = board.secondSelectionPiece;
		this.showSplitLine = board.showSplitLine;
		this.showJoinRect = board.showJoinRect;
		this.mousePosition = board.mousePosition;
		this.splitDirection = board.splitDirection;
		this.showStartPoint = board.showStartPoint;
		this.swapStartPieces = new HashSet<>(board.swapStartPieces);
		this.chains = new HashSet<>(board.chains);
		this.width = board.width;
		this.height = board.height;
		this.controlPanel = board.controlPanel;
		this.crumbleGame = board.crumbleGame;
		this.buttonListener = board.buttonListener;
		
		setPreferredSize(new Dimension(width+GamePiece.X_OFFSET*2, height+GamePiece.Y_OFFSET*2));
		addMouseListener(new BoardMouseListener());
		addMouseMotionListener(new BoardMouseMotionListener());
	}

	public void initialize() {
		currentTurn = true;	// black goes first
		currentAction = "split";
		boardOutline = new Rectangle(GamePiece.X_OFFSET, GamePiece.Y_OFFSET, width, height);
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
				Set<GamePiece> newChain = new HashSet<>();
				newChain.add(newPiece);
				chains.add(newChain);
				color = !color;
				gamePieces.add(newPiece);
			}
			color = !color;
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		for(GamePiece piece: gamePieces) {
			piece.draw(g);
		}
		this.draw(g);
	}
	
	public void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g.drawRect(boardOutline.x, boardOutline.y, boardOutline.width, boardOutline.height);
		if(showStartPoint) {
			BoardPoint p = getNearestPoint(new BoardPoint(mousePosition));
			if(p != null) {
				g2.setColor(Color.red);
				g2.fillRect(p.toPoint().x, p.toPoint().y, 5, 5);
			}
		}
		else if(showSplitLine) {
			int x1 = firstSelectionPoint.toPoint().x;
			int y1 = firstSelectionPoint.toPoint().y;
			BoardPoint p = getNearestPoint(new BoardPoint(mousePosition));
			int x2 = p.toPoint().x;
			int y2 = p.toPoint().y;
			g2.setColor(Color.red);
			g2.setStroke(new BasicStroke(4));
			g2.drawLine(x1, y1, x2, y2);
		}
		else if(showJoinRect) {
			BoardPoint p = getNearestPoint(new BoardPoint(mousePosition));
			if(p != null) {
				int x1 = firstSelectionPoint.toPoint().x;
				int y1 = p.toPoint().y;
				int width = p.toPoint().x - x1;
				int height = firstSelectionPoint.toPoint().y - y1;
				g2.setColor(Color.red);
				g2.setStroke(new BasicStroke(4));
				g2.drawRect(x1, y1, width, height);
			}
		}
		else if(firstSelectionPiece != null) {
			int x1 = firstSelectionPiece.getBottomLeft().toPoint().x;
			int y1 = firstSelectionPiece.getTopRight().toPoint().y;
			int x2 = firstSelectionPiece.getTopRight().toPoint().x;
			int y2 = firstSelectionPiece.getBottomLeft().toPoint().y;
			g2.setColor(Color.red);
			g2.setStroke(new BasicStroke(4));
			g2.drawRect(x1, y1, x2-x1, y2-y1);
		}
	}

	public boolean isCurrentTurn() {
		return currentTurn;
	}

	public void swap() {
		firstSelectionPiece.setColor(!firstSelectionPiece.isColor());
		updateChain(firstSelectionPiece);
		secondSelectionPiece.setColor(!secondSelectionPiece.isColor());
		updateChain(secondSelectionPiece);
		firstSelectionPiece = secondSelectionPiece;
		secondSelectionPiece = null;
		
		checkWin();
		checkCapture();
		
		crumbleGame.saveState();
		this.repaint();
	}

	public void join() {
		Set<GamePiece> piecesToJoin = new HashSet<>();
		GamePiece p1 = null;
		GamePiece p2 = null;
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
			for(GamePiece surrounding: piece.getSurrounding()) {
				surrounding.getSurrounding().remove(piece);
			}
			chain.remove(piece);
		}
		gamePieces.add(newPiece);
		chain.add(newPiece);
		firstSelectionPiece = newPiece;
		
		showJoinRect = false;
		currentAction = "swap";
		ArrayList<JButton> buttons = controlPanel.getButtons();
		buttons.get(0).setEnabled(false);
		buttons.get(1).setEnabled(false);
		buttons.get(2).setEnabled(true);
		buttons.get(3).setEnabled(true);
		firstSelectionPoint = null;
		secondSelectionPoint = null;
		
		crumbleGame.saveState();
		this.repaint();
	}

	public void split() {
		Set<GamePiece> piecesToSplit = new HashSet<>();
		swapStartPieces.clear();
		if(splitDirection) { // horizontal split
			for(GamePiece piece: gamePieces) {
				if(piece.canSplitHorizontal() && firstSelectionPoint.getY() == piece.getHorizontalSplitStart().getY() &&
				   firstSelectionPoint.getX() <= piece.getBottomLeft().getX() && secondSelectionPoint.getX() >= piece.getTopRight().getX()) {
					piecesToSplit.add(piece);
				}
			}
			for(GamePiece piece: piecesToSplit) {
				gamePieces.remove(piece);
				for(GamePiece neighbor: piece.getNeighbors()) {
					neighbor.getNeighbors().remove(piece);
				}
				for(GamePiece surrounding: piece.getSurrounding()) {
					surrounding.getSurrounding().remove(piece);
				}
				Set<GamePiece> chain = findChain(piece);
				chain.remove(piece);
				GamePiece newPieceBottom = new GamePiece(piece.isColor(), piece.getBottomLeft(), piece.getHorizontalSplitEnd(), "????", gamePieces);
				gamePieces.add(newPieceBottom);
				chain.add(newPieceBottom);
				GamePiece newPieceTop = new GamePiece(piece.isColor(), piece.getHorizontalSplitStart(), piece.getTopRight(), "????", gamePieces);
				gamePieces.add(newPieceTop);
				chain.add(newPieceTop);
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
			for(GamePiece piece: piecesToSplit) {
				gamePieces.remove(piece);
				for(GamePiece neighbor: piece.getNeighbors()) {
					neighbor.getNeighbors().remove(piece);
				}
				for(GamePiece surrounding: piece.getSurrounding()) {
					surrounding.getSurrounding().remove(piece);
				}
				Set<GamePiece> chain = findChain(piece);
				chain.remove(piece);
				GamePiece newPieceLeft = new GamePiece(piece.isColor(), piece.getBottomLeft(), piece.getVerticalSplitEnd(), "????", gamePieces);
				gamePieces.add(newPieceLeft);
				chain.add(newPieceLeft);
				GamePiece newPieceRight = new GamePiece(piece.isColor(), piece.getVerticalSplitStart(), piece.getTopRight(), "????", gamePieces);
				gamePieces.add(newPieceRight);
				chain.add(newPieceRight);
				swapStartPieces.add(newPieceRight);
				swapStartPieces.add(newPieceLeft);
			}
		}
		
		showSplitLine = false;
		currentAction = "swap";
		ArrayList<JButton> buttons = controlPanel.getButtons();
		buttons.get(0).setEnabled(false);
		buttons.get(1).setEnabled(false);
		buttons.get(2).setEnabled(true);
		buttons.get(3).setEnabled(true);
		firstSelectionPoint = null;
		secondSelectionPoint = null;
		
		crumbleGame.saveState();
		this.repaint();
	}
	
	public String getCurrentAction() {
		return currentAction;
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
		return !( area != width*height || pieceCount <= 1 ||
				  width != height && 2*width != height && width != 2*height );
	}

	private boolean validateSplit(BoardPoint p1, BoardPoint p2) {
		boolean splitIsValid = true;
		if(splitDirection) {
			for(GamePiece piece: gamePieces) {
				if(p1.getY() < piece.getTopRight().getY() && p1.getY() > piece.getBottomLeft().getY() &&
				   p1.getX() <= piece.getBottomLeft().getX() && p2.getX() >= piece.getTopRight().getX() &&
				   (piece.canSplitHorizontal() && piece.getHorizontalSplitStart().getY() != p1.getY() || (piece.isColor() != currentTurn)) ) {
					splitIsValid = false;
					break;
				}
			}
		}
		else {
			for(GamePiece piece: gamePieces) {
				if(p1.getX() < piece.getTopRight().getX() && p1.getX() > piece.getBottomLeft().getX() &&
				   p1.getY() <= piece.getBottomLeft().getY() && p2.getY() >= piece.getTopRight().getY() &&
				   (piece.canSplitVertical() && piece.getVerticalSplitStart().getX() != p1.getX() || (piece.isColor() != currentTurn)) ) {
					splitIsValid = false;
					break;
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
			Set<Character> edges = new HashSet<>();
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

	private void checkCapture() {
		Set<Set<GamePiece>> isCaptured = new HashSet<>(chains); //Assume that each chain is captured until we figure out that it isn't
		
		for(Set<GamePiece> chain: chains) {
			if(isCaptured.contains(chain)) {
				for(GamePiece piece: chain) {
					if(!piece.getWallNeighbors().isEmpty()) isCaptured.remove(chain);
				}
				if(!isCaptured.contains(chain)) {
					Queue<Set<GamePiece>> notCaptured = new LinkedList<>();
					for(GamePiece piece: chain) {
						for(GamePiece surrounding: piece.getSurrounding()) {
							if(surrounding.isColor() == piece.isColor()) {
								Set<GamePiece> surroundingChain = findChain(surrounding);
								if(isCaptured.contains(surroundingChain) && !notCaptured.contains(surroundingChain)) notCaptured.add(surroundingChain);
							}
						}
					}
					while(!notCaptured.isEmpty()) {
						Set<GamePiece> currentChain = notCaptured.poll();
						isCaptured.remove(currentChain);
						for(GamePiece piece: currentChain) {
							for(GamePiece surrounding: piece.getSurrounding()) {
								if(surrounding.isColor() == piece.isColor()) {
									Set<GamePiece> surroundingChain = findChain(surrounding);
									if(isCaptured.contains(surroundingChain) && !notCaptured.contains(surroundingChain)) notCaptured.add(surroundingChain);
								}
							}
						}
					}
				}
			}
		}
		
		for(Set<GamePiece> capturedChain: isCaptured) {
			Set<GamePiece> copyChain = new HashSet<>(capturedChain);
			for(GamePiece piece: copyChain) {
				piece.setColor(!piece.isColor());
				updateChain(piece);
			}
			this.repaint();
		}
	}

	public void setControlPanel(ControlPanel controlPanel) {
		this.controlPanel = controlPanel;
	}
	
	public ButtonListener getButtonListener() {
		return buttonListener;
	}
	
	class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) { 
			currentAction = e.getActionCommand();
			if(currentAction.equals("end turn")) {
				currentTurn = !currentTurn;
				currentAction = "split";
				firstSelectionPiece = null;
				showStartPoint = true;
				ArrayList<JButton> buttons = controlPanel.getButtons();
				buttons.get(0).setEnabled(true);
				buttons.get(1).setEnabled(true);
				buttons.get(2).setEnabled(false);
				buttons.get(3).setEnabled(false);
				repaint();
				if(currentTurn) controlPanel.setCurrentTurn("Black's");
				else controlPanel.setCurrentTurn("White's");
			}
			else if(currentAction.equals("split") || currentAction.equals("join")) {
				firstSelectionPoint = null;
				secondSelectionPoint = null;
				firstSelectionPiece = null;
				secondSelectionPiece = null;
				showSplitLine = false;
				showJoinRect = false;
				showStartPoint = true;
				repaint();
			}
			else if(currentAction.equals("undo")) {
				crumbleGame.loadState("undo");
			}
			else if(currentAction.equals("redo")) {
				crumbleGame.loadState("redo");
			}
		}
	}
	
	public class BoardMouseListener implements MouseListener {
		@Override
		public void mouseClicked(MouseEvent e) {}

		@Override
		public void mousePressed(MouseEvent e) {
			Point p = e.getPoint();
			if(boardOutline.contains(p)) {
				BoardPoint click = new BoardPoint(p);
				if(currentAction.equals("split") || currentAction.equals("join")) {
					if(firstSelectionPoint == null) {
						firstSelectionPoint = getNearestPoint(click);
						if(currentAction.equals("join") && getNearestPoint(click) == null) firstSelectionPoint = null;
					}
					else {
						secondSelectionPoint = getNearestPoint(click);
						if(currentAction.equals("split")) split();
						else join();
					}
				}
				else if(currentAction.equals("swap")) {
					if(firstSelectionPiece == null) {
						for(GamePiece piece: swapStartPieces) {
							if(piece.contains(p)) {
								firstSelectionPiece = piece;
								repaint();
								swapStartPieces.clear();
								break;
							}
						}
					}
					else {
						for(GamePiece piece: gamePieces) {
							if(piece.contains(p) && validateSwap(firstSelectionPiece, piece)) {
								secondSelectionPiece = piece;
								break;
							}
						}
						if(secondSelectionPiece != null) swap();
					}
				}
			}
			if(firstSelectionPoint != null) {
				if(currentAction.equals("split")) {
					showSplitLine = true;
					showStartPoint = false;
				}
				else if(currentAction.equals("join")) {
					showStartPoint = false;
					showJoinRect = true;
				}
			}
			else {
				if(currentAction.equals("split")) {
					showStartPoint = true;
				}
				else if(currentAction.equals("join")) {
					showJoinRect = false;
					showStartPoint = true;
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {}
	}
	
	public class BoardMouseMotionListener implements MouseMotionListener {
		@Override
		public void mouseDragged(MouseEvent e) {}
		
		@Override
		public void mouseMoved(MouseEvent e) {
			mousePosition = e.getPoint();
			repaint();				
		}
	}
	
}
