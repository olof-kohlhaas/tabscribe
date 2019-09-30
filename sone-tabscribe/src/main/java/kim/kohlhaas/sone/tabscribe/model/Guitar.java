package kim.kohlhaas.sone.tabscribe.model;

import java.util.ArrayList;
import java.util.List;

import kim.kohlhaas.sone.harmony.Temperament;
import kim.kohlhaas.sone.harmony.Tone;

public class Guitar implements Track {
	
	private String name;
	private List<GuitarString> strings = new ArrayList<>();
	private Temperament temperament;
	
	public Guitar(Temperament temperament) {
		this.temperament = temperament;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public int[] getFittingFrets(Tone tone) {
		int[] frets = new int[strings.size()];
		
		for (GuitarString string : strings) {
			frets[getStringIndex(string)] = temperament.getIndex(tone) - temperament.getIndex(string.getEmptyString()) ;
		}
		
		return frets;
	}
	
	public void addTabStatement(int guitarStringIndex, TabStatement tabStatement) {
		tabStatement.setGuitar(this);
		tabStatement.setGuitarString(strings.get(guitarStringIndex));
		tabStatement.getGuitarString().addTabStatement(tabStatement);
	}
	
	public void addGuitarString(int guitarStringindex, GuitarString guitarString) {
		guitarString.setGuitar(this);
		strings.add(guitarStringindex, guitarString);
	}
	
	public int getStringCount() {
		return strings.size();
	}
	
	public GuitarString getString(int index) {
		return strings.get(index);
	}
	
	public int getStringIndex(GuitarString guitarString) {
		return strings.indexOf(guitarString);
	}
	
	@Override
	public String toString() {
		return "guitar, name: " + name + ", guitar strings: " + strings.size();
	}
	
	@Override
	public Type getType() {
		return Type.GUITAR;
	}

	public Temperament getTemperament() {
		return temperament;
	}

	public void setTemperament(Temperament temperament) {
		this.temperament = temperament;
	}	
	
}
