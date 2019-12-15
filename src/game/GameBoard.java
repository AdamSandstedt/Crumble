package game;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JOptionPane;
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
	private int boardWidth = 800;
	private int boardHeight = 800;
	private int numRows = 0;
	private int numColumns = 0;
	private ControlPanel controlPanel;
	private CrumbleGame crumbleGame;
	private ButtonListener buttonListener;
	private Map<BoardPoint, BoardPoint> boardPoints;
	private String currentMoveNotation;
	private Set<GamePiece> piecesToSplit;
	private Map<BoardPoint, GamePiece> gamePieceAt;
	private final double smallestWidth = Math.pow(2,-10); // In theory, a piece could get smaller than this, but I can't imagine it actually happening in a real game
	private final double smallestHeight = Math.pow(2,-10);
	private double xConversion;
	private double yConversion;

	GameBoard() {
		gamePieces = new HashSet<>();
		swapStartPieces = new HashSet<>();
		chains = new HashSet<>();
		buttonListener = new ButtonListener();
		boardPoints = new HashMap<>();
		piecesToSplit = new HashSet<>();
		gamePieceAt = new HashMap<>();

		setPreferredSize(new Dimension(boardWidth+GamePiece.X_OFFSET*2, boardHeight+GamePiece.Y_OFFSET*2));
		addMouseListener(new BoardMouseListener(this));
		addMouseMotionListener(new BoardMouseMotionListener());
		
		addComponentListener(new ComponentAdapter() {
			@Override
		    public void componentResized(ComponentEvent e) {
				GameBoard board = (GameBoard)e.getSource();
		        board.setBoardWidth(board.getBounds().width - GamePiece.X_OFFSET*2);
		        board.setBoardHeight(board.getBounds().height - GamePiece.Y_OFFSET*2);
		        xConversion = (double)boardWidth / numColumns;
				yConversion = (double)boardHeight / numRows;
		        board.boardOutline = new Rectangle(GamePiece.X_OFFSET, GamePiece.Y_OFFSET, boardWidth, boardHeight);
		    }
		});

	}

	public GameBoard(CrumbleGame crumbleGame) {
		this(); // call constructor with no arguments
		this.crumbleGame = crumbleGame;
		this.controlPanel = crumbleGame.getControlPanel();
		if(crumbleGame.getNumRows() != 0) numRows = crumbleGame.getNumRows();
		if(crumbleGame.getNumColumns() != 0) numColumns = crumbleGame.getNumColumns();
		this.initialize();
		if(controlPanel != null) {
			controlPanel.setGameBoard(this);
			controlPanel.setButtonListener(buttonListener);
		}
	}

	public void initialize() {
		currentTurn = true;	// black goes first
		currentAction = "split";
		boardOutline = new Rectangle(GamePiece.X_OFFSET, GamePiece.Y_OFFSET, boardWidth, boardHeight);
		firstSelectionPoint = null;
		secondSelectionPoint = null;
		showSplitLine = false;
		showJoinRect = false;
		splitDirection = false;
		showStartPoint = true;
		if(numRows == 0) numRows = 6;
		if(numColumns == 0) numColumns = 6;
		xConversion = (double)boardWidth / numColumns;
		yConversion = (double)boardHeight / numRows;

		GamePiece newPiece;
		Notation notation;
		boolean color = numRows % 2 == 1;
		gamePieces.clear();
		for(int x = 0; x <= numColumns; x++) {
			for(int y = 0; y <= numRows; y++) {
				BoardPoint newPoint = new BoardPoint(x, y, this);
				boardPoints.put(newPoint, newPoint);
			}
		}
		for(int x = 0; x < numColumns; x++) {
			for(int y = 0; y < numRows; y++) {
				if(y == 0) notation = new Notation(x);
				else notation = new Notation(x, y);
				newPiece = new GamePiece(color, getPointAt(x, y), getPointAt(x+1,y+1), notation, gamePieces, this);
				gamePieceAt.put(getPointAt(x, y), newPiece);
				Set<GamePiece> newChain = new HashSet<>();
				newChain.add(newPiece);
				chains.add(newChain);
				color = !color;
				gamePieces.add(newPiece);
			}
			if(numRows % 2 == 0) color = !color;
		}
	}

	public int getBoardWidth() {
		return boardWidth;
	}

	public void setBoardWidth(int width) {
		this.boardWidth = width;
	}

	public int getBoardHeight() {
		return boardHeight;
	}

	public void setBoardHeight(int height) {
		this.boardHeight = height;
	}

	private BoardPoint getPointAt(double x, double y) {
		return boardPoints.get(new BoardPoint(x, y, this));
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		for(GamePiece piece: gamePieces) {
			piece.draw(g);
		}
		this.draw(g);
	}

	public void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g.drawRect(boardOutline.x, boardOutline.y, boardOutline.width, boardOutline.height);
		if(showStartPoint) {
			BoardPoint p = getNearestPoint(new BoardPoint(mousePosition, this));
			if(p != null) {
				g2.setColor(Color.red);
				g2.fillRect(p.toPoint().x, p.toPoint().y, 5, 5);
			}
		}
		else if(showSplitLine) {
			int x1 = firstSelectionPoint.toPoint().x;
			int y1 = firstSelectionPoint.toPoint().y;
			BoardPoint p = getNearestPoint(new BoardPoint(mousePosition, this));
			int x2 = p.toPoint().x;
			int y2 = p.toPoint().y;
			g2.setColor(Color.red);
			g2.setStroke(new BasicStroke(4));
			g2.drawLine(x1, y1, x2, y2);
		}
		else if(showJoinRect) {
			BoardPoint p = getNearestPoint(new BoardPoint(mousePosition, this));
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
		if(currentMoveNotation.matches("[0-9]+(,[0-9]+)*V[0-9]*")) { //First swap after a vertical split
			GamePiece originalSplitPiece = null;
			if(piecesToSplit.size() > 1) {
				int piecesSplitBelow = 0;
				for(GamePiece piece: piecesToSplit) {
					if(firstSelectionPiece.getBottomLeft().getY() > piece.getBottomLeft().getY()) piecesSplitBelow++;
					else if(firstSelectionPiece.getBottomLeft().getY() == piece.getBottomLeft().getY()) originalSplitPiece = piece;
				}
				currentMoveNotation += "-" + (piecesSplitBelow + 1);
			}
			else {
				originalSplitPiece = piecesToSplit.iterator().next();
			}
			if(firstSelectionPiece.getBottomLeft().getY() != secondSelectionPiece.getBottomLeft().getY() || piecesToSplit.size() > 1) { // This means it's either a N or S swap or multiple pieces were split
				if(originalSplitPiece.getBottomLeft().getX() == firstSelectionPiece.getBottomLeft().getX()) currentMoveNotation += "W-";
				else currentMoveNotation += "E-";
			}
		}
		else if(currentMoveNotation.matches(".(,.)*H[0-9]*")) { //First swap after a horizontal split
			GamePiece originalSplitPiece = null;
			if(piecesToSplit.size() > 1) {
				int piecesSplitLeft = 0;
				for(GamePiece piece: piecesToSplit) {
					if(firstSelectionPiece.getBottomLeft().getX() > piece.getBottomLeft().getX()) piecesSplitLeft++;
					else if(firstSelectionPiece.getBottomLeft().getX() == piece.getBottomLeft().getX()) originalSplitPiece = piece;
				}
				currentMoveNotation += "-" + (piecesSplitLeft + 1);
			}
			else {
				originalSplitPiece = piecesToSplit.iterator().next();
			}
			if(firstSelectionPiece.getBottomLeft().getX() != secondSelectionPiece.getBottomLeft().getX() || piecesToSplit.size() > 1) { // This means it's either a E or W swap or multiple pieces were split
				if(originalSplitPiece.getBottomLeft().getY() == firstSelectionPiece.getBottomLeft().getY()) currentMoveNotation += "S-";
				else currentMoveNotation += "N-";
			}
		}
		if(firstSelectionPiece.getBottomLeft().getX() < secondSelectionPiece.getBottomLeft().getX()) {
			currentMoveNotation += 'E';
		}
		if(firstSelectionPiece.getBottomLeft().getX() > secondSelectionPiece.getBottomLeft().getX()) {
			currentMoveNotation += 'W';
		}
		if(firstSelectionPiece.getBottomLeft().getY() < secondSelectionPiece.getBottomLeft().getY()) {
			currentMoveNotation += 'N';
		}
		if(firstSelectionPiece.getBottomLeft().getY() > secondSelectionPiece.getBottomLeft().getY()) {
			currentMoveNotation += 'S';
		}
		firstSelectionPiece.setColor(!firstSelectionPiece.isColor());
		updateChain(firstSelectionPiece);
		secondSelectionPiece.setColor(!secondSelectionPiece.isColor());
		updateChain(secondSelectionPiece);

		firstSelectionPiece = secondSelectionPiece;
		secondSelectionPiece = null;
		this.repaint();

		checkWin();
		checkCapture();
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
		BoardPoint bottomRight = getPointAt(p2.getTopRight().getX(), p1.getBottomLeft().getY());
		currentMoveNotation = p1.getNotation().toString() + 'J' + (1 + getNumPointsBetween(p1.getBottomLeft(), bottomRight)) + "," + (1 + getNumPointsBetween(bottomRight, p2.getTopRight()));
		GamePiece newPiece = new GamePiece(p1.isColor(), p1.getBottomLeft(), p2.getTopRight(), p1.getNotation(), gamePieces, this);
		for(GamePiece piece: gamePieces) {
			if(newPiece.contains(piece)) {
				piecesToJoin.add(piece);
			}
		}
		Set<GamePiece> chain = findChain(p1);
		for(GamePiece piece: piecesToJoin) {
			gamePieces.remove(piece);
			gamePieceAt.remove(piece.getBottomLeft());
			for(GamePiece neighbor: piece.getNeighbors()) {
				neighbor.getNeighbors().remove(piece);
			}
			for(GamePiece surrounding: piece.getSurrounding()) {
				surrounding.getSurrounding().remove(piece);
			}
			chain.remove(piece);
		}
		gamePieces.add(newPiece);
		gamePieceAt.put(newPiece.getBottomLeft(), newPiece);
		chain.add(newPiece);
		firstSelectionPiece = newPiece;
		updateBoardPoints();
		updateNotations();

		showJoinRect = false;
		currentAction = "swap";
		ArrayList<JButton> buttons = controlPanel.getButtons();
		buttons.get(0).setEnabled(false);
		buttons.get(1).setEnabled(false);
		buttons.get(2).setEnabled(true);
		buttons.get(3).setEnabled(true);
		firstSelectionPoint = null;
		secondSelectionPoint = null;
		this.repaint();
	}

	public void split() {
		piecesToSplit.clear();
		swapStartPieces.clear();
		if(splitDirection) { // horizontal split
			for(GamePiece piece: gamePieces) {
				if(piece.canSplitHorizontal() && firstSelectionPoint.getY() == piece.getHorizontalSplitStart().getY() &&
				   firstSelectionPoint.getX() <= piece.getBottomLeft().getX() && secondSelectionPoint.getX() >= piece.getTopRight().getX()) {
					piecesToSplit.add(piece);
					if(firstSelectionPoint == piece.getHorizontalSplitStart()) currentMoveNotation = piece.getNotation().toString() + "H";
				}
			}
			if(piecesToSplit.size() > 1) currentMoveNotation += piecesToSplit.size();
			for(GamePiece piece: piecesToSplit) {
				gamePieces.remove(piece);
				gamePieceAt.remove(piece.getBottomLeft());
				for(GamePiece neighbor: piece.getNeighbors()) {
					neighbor.getNeighbors().remove(piece);
				}
				for(GamePiece surrounding: piece.getSurrounding()) {
					surrounding.getSurrounding().remove(piece);
				}
				Set<GamePiece> chain = findChain(piece);
				chain.remove(piece);
				GamePiece newPieceBottom = new GamePiece(piece.isColor(), piece.getBottomLeft(), piece.getHorizontalSplitEnd(), piece.getNotation(), gamePieces, this);
				gamePieces.add(newPieceBottom);
				gamePieceAt.put(newPieceBottom.getBottomLeft(), newPieceBottom);
				chain.add(newPieceBottom);
				GamePiece newPieceTop = new GamePiece(piece.isColor(), piece.getHorizontalSplitStart(), piece.getTopRight(), piece.getNotation().notationUp(0), gamePieces, this);
				gamePieces.add(newPieceTop);
				gamePieceAt.put(newPieceTop.getBottomLeft(), newPieceTop);
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
					if(firstSelectionPoint == piece.getVerticalSplitStart()) currentMoveNotation = piece.getNotation().toString() + "V";
				}
			}
			if(piecesToSplit.size() > 1) currentMoveNotation += piecesToSplit.size();
			for(GamePiece piece: piecesToSplit) {
				gamePieces.remove(piece);
				gamePieceAt.remove(piece.getBottomLeft());
				for(GamePiece neighbor: piece.getNeighbors()) {
					neighbor.getNeighbors().remove(piece);
				}
				for(GamePiece surrounding: piece.getSurrounding()) {
					surrounding.getSurrounding().remove(piece);
				}
				Set<GamePiece> chain = findChain(piece);
				chain.remove(piece);
				GamePiece newPieceLeft = new GamePiece(piece.isColor(), piece.getBottomLeft(), piece.getVerticalSplitEnd(), piece.getNotation(), gamePieces, this);
				gamePieces.add(newPieceLeft);
				gamePieceAt.put(newPieceLeft.getBottomLeft(), newPieceLeft);
				chain.add(newPieceLeft);
				GamePiece newPieceRight = new GamePiece(piece.isColor(), piece.getVerticalSplitStart(), piece.getTopRight(), piece.getNotation().notationRight(0), gamePieces, this);
				gamePieces.add(newPieceRight);
				gamePieceAt.put(newPieceRight.getBottomLeft(), newPieceRight);
				chain.add(newPieceRight);
				swapStartPieces.add(newPieceRight);
				swapStartPieces.add(newPieceLeft);
			}
		}
		updateBoardPoints();
		updateNotations();

		showSplitLine = false;
		currentAction = "swap";
		ArrayList<JButton> buttons = controlPanel.getButtons();
		buttons.get(0).setEnabled(false);
		buttons.get(1).setEnabled(false);
		buttons.get(2).setEnabled(true);
		buttons.get(3).setEnabled(true);
		firstSelectionPoint = null;
		secondSelectionPoint = null;
		this.repaint();
	}

	private void updateBoardPoints() {
		boardPoints.clear();
		for(GamePiece piece: gamePieces) {
			boardPoints.put(piece.getBottomLeft(), piece.getBottomLeft());
			boardPoints.put(piece.getTopRight(), piece.getTopRight());
		}
	}
	
	private int getNumPointsBetween(GamePiece bottomLeft, GamePiece topRight) {
		BoardPoint p1 = bottomLeft.getBottomLeft();
		BoardPoint p2 = topRight.getBottomLeft();
		return getNumPointsBetween(p1, p2);
	}
	
	private int getNumPointsBetween(BoardPoint p1, BoardPoint p2) {
		int count = 0;
		double x, y;
		if(p1.getX() == p2.getX()) { // vertical line
			x = p1.getX();
			for(y = p1.getY()+smallestHeight; y < p2.getY(); y += smallestHeight)
				if(getPointAt(x, y) != null) count++;
		}
		else { // horizontal line
			y = p1.getY();
			for(x = p1.getX()+smallestWidth; x < p2.getX(); x += smallestWidth)
				if(getPointAt(x, y) != null) count++;
		}
		return count;
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
				String message;
				if(color) message = "Black wins!";
				else message = "White wins!";
				JOptionPane.showMessageDialog(this, message, "Winner!", JOptionPane.INFORMATION_MESSAGE);
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
				crumbleGame.addMove(currentMoveNotation);
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
		}
	}

	public class BoardMouseListener implements MouseListener {
		private GameBoard board;
		
		public BoardMouseListener(GameBoard board) {
			this.board = board;
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {}

		@Override
		public void mousePressed(MouseEvent e) {
			Point p = e.getPoint();
			if(boardOutline.contains(p)) {
				BoardPoint click = new BoardPoint(p, board);
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

	public void toggleNotations() {
		GamePiece.setShowNotation(!GamePiece.isShowNotation());
		repaint();
	}

	private void updateNotations() {
		LinkedList<GamePiece> queue = new LinkedList<>();
		for(GamePiece piece: gamePieces) {
			if(piece.getBottomLeft().equals(new BoardPoint(0, 0, this))) queue.add(piece);
			piece.setNotation(null);
		}
		queue.peek().setNotation(new Notation(0));
		GamePiece currentPiece;
		while(!queue.isEmpty()) {
			currentPiece = queue.pop();
			int notationUpSize = currentPiece.getNotation().notationUp(0).notationSize();
			int notationRightSize = currentPiece.getNotation().notationRight(0).notationSize();
			for(GamePiece piece: currentPiece.getNeighbors()) {
				if(piece.getBottomLeft().getX() == currentPiece.getBottomLeft().getX() && piece.getBottomLeft().getY() > currentPiece.getBottomLeft().getY()
				  && (piece.getNotation() == null || notationUpSize <= piece.getNotation().notationSize())) { // up
					piece.setNotation(currentPiece.getNotation().notationUp(getNumPointsBetween(currentPiece, piece)));
					queue.add(piece);
				}
				if(piece.getBottomLeft().getY() == currentPiece.getBottomLeft().getY() && piece.getBottomLeft().getX() > currentPiece.getBottomLeft().getX()
				  && (piece.getNotation() == null || notationRightSize <= piece.getNotation().notationSize())) { // right
					piece.setNotation(currentPiece.getNotation().notationRight(getNumPointsBetween(currentPiece, piece)));
					queue.add(piece);
				}
			}
		}
	}

	public int getNumRows() {
		return numRows;
	}

	public int getNumColumns() {
		return numColumns;
	}

	public double getxConversion() {
		return xConversion;
	}

	public double getyConversion() {
		return yConversion;
	}

	public void doMove(String moveNotation) {
		if(moveNotation.matches("[0-9]+(,[0-9]+)*V.*")) { // vertical split
			String startPieceNotation = moveNotation.substring(0, moveNotation.indexOf('V')); // The notation of the first piece split
			GamePiece startPiece = getPieceWithNotation(startPieceNotation);
			firstSelectionPoint = startPiece.getVerticalSplitStart();
			if(moveNotation.matches("[0-9]+(,[0-9]+)*V[0-9].*")) { // The split was through multiple pieces
				int numberOfSplits = Integer.parseInt(moveNotation.split("[0-9]+(,[0-9]+)*V")[1].split("-")[0]);
				secondSelectionPoint = getSplitPointAbove(firstSelectionPoint, numberOfSplits);
			}
			else { // Only split one piece
				secondSelectionPoint = startPiece.getVerticalSplitEnd();
			}
			split();
		}
		else if(moveNotation.matches("[0-9]+(,[0-9]+)*H.*")) { // horizontal split
			String startPieceNotation = moveNotation.substring(0, moveNotation.indexOf('H')); // The notation of the first piece split
			GamePiece startPiece = getPieceWithNotation(startPieceNotation);
			firstSelectionPoint = startPiece.getHorizontalSplitStart();
			if(moveNotation.matches("[0-9]+(,[0-9]+)*H[0-9].*")) { // The split was through multiple pieces
				int numberOfSplits = Integer.parseInt(moveNotation.split("[0-9]+(,[0-9]+)*H")[1].split("-")[0]);
				secondSelectionPoint = getSplitPointRight(firstSelectionPoint, numberOfSplits);
			}
			else { // Only split one piece
				secondSelectionPoint = startPiece.getHorizontalSplitEnd();
			}
			split();
		}
		else if(moveNotation.matches("[0-9]+(,[0-9]+)*J.*")) { // join
			String startPieceNotation = moveNotation.substring(0, moveNotation.indexOf('J')); // The notation of the first piece split
			GamePiece startPiece = getPieceWithNotation(startPieceNotation);
			firstSelectionPoint = startPiece.getBottomLeft();
			String endJoinNotation = moveNotation.split("[0-9]+(,[0-9]+)*J")[1].split("[NESW]")[0];
			secondSelectionPoint = getEndJoinPoint(firstSelectionPoint, endJoinNotation);
			join();
		}
	}

	private BoardPoint getEndJoinPoint(BoardPoint firstPoint, String notation) {
		// TODO Auto-generated method stub
		return null;
	}

	private BoardPoint getSplitPointRight(BoardPoint firstSelectionPoint2, int numberOfSplits) {
		// TODO Auto-generated method stub
		return null;
	}

	private BoardPoint getSplitPointAbove(BoardPoint firstPoint, int numberOfSplits) {
		// TODO Auto-generated method stub
		return null;
	}

	private GamePiece getPieceWithNotation(String startPieceNotation) {
		// TODO Auto-generated method stub
		return null;
	}

}
