package game;

import java.util.HashSet;
import java.util.Set;

public class GameBoard {
	private Set<GamePiece> gamePieces;
	
	GameBoard() {
		gamePieces = new HashSet<GamePiece>();
		this.initialize();  // Not sure if this is good practice or not, maybe I should make the user call it
	}
	
	public void initialize() {
		GamePiece newPiece;
		String location;
		gamePieces.clear();
		for(double x = 0; x < 6; x++) {
			for(double y = 0; y < 6; y++) {
				if(y == 0) location = "" + (int)x;
				else location = (int)x + "," + (int)y;
				newPiece = new GamePiece(false, new Point(x,y), new Point(x+1,y+1), location);
				gamePieces.add(newPiece);
			}
		}
	}
	
}
