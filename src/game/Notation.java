package game;

import java.util.ArrayList;

public class Notation {
	private ArrayList<Integer> location;

	public Notation(int x) {
		location = new ArrayList<>();
		location.add(x);
	}
	
	public Notation(int x, int y) {
		this(x);
		location.add(y);
	}
	
	public Notation(Notation n) {
		this.location = new ArrayList<>(n.location);
	}
	
	public Notation notationRight() {
		Notation newNotation = new Notation(this);
		ArrayList<Integer> newLocation = newNotation.location;
		if(newLocation.size() % 2 == 0) {
			newNotation.location.add(1);
		}
		else {
			newLocation.set(newLocation.size() - 1, newLocation.get(newLocation.size() - 1) + 1);
		}
		return newNotation;
	}
	
	public Notation notationUp() {
		Notation newNotation = new Notation(this);
		ArrayList<Integer> newLocation = newNotation.location;
		if(newLocation.size() % 2 == 0) {
			newLocation.set(newLocation.size() - 1, newLocation.get(newLocation.size() - 1) + 1);
		}
		else {			
			newNotation.location.add(1);
		}
		return newNotation;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(location.get(0));
		for(int i = 1; i < location.size(); i++) builder.append("," + location.get(i));
		return builder.toString();
	}

	public int notationSize() {
		return location.size();
	}

}
