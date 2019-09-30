package kim.kohlhaas.sone.tabscribe.model;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kim.kohlhaas.sone.harmony.Temperament;
import kim.kohlhaas.sone.harmony.Tone;
import kim.kohlhaas.sone.tabscribe.javafx.MainController;

public class TabStatement implements Comparable<TabStatement> {
	final static Logger log = LoggerFactory.getLogger(TabStatement.class);
	
	private GuitarString guitarString;
	private Guitar guitar;
	private String statement;
	private Double millisecond;
	private double velocity = 1.0;
	private ArrayList<Integer> frets = new ArrayList<>();
	private ArrayList<String> techniques = new ArrayList<>();
	private String initTechnique = "";
	public static final int MAX_FRET = 24;
	
	public TabStatement(String statement, Double millisecond) {
		setStatement(statement);
		this.millisecond = millisecond;		
	}
	
	public GuitarString getGuitarString() {
		return guitarString;
	}
	
	public void setGuitarString(GuitarString guitarString) {
		this.guitarString = guitarString;
	}
	
	public Guitar getGuitar() {
		return guitar;
	}
	
	public void setGuitar(Guitar guitar) {
		this.guitar = guitar;
	}
	
	public String getStatement() {
		return statement;
	}
	
	public void setStatement(String statement) {
		this.statement = statement;
		String prefix;
		String fret;
		String suffix;
		Pattern pattern = Pattern.compile("([\\D]*)([0-9]*)([\\D]*)");
		Matcher matcher = pattern.matcher(statement);
		while (matcher.find()) {
			prefix = matcher.group(1).trim();
			fret = matcher.group(2);
			suffix = matcher.group(3).trim();
			if (prefix.length() > 0) {
				initTechnique = prefix;
			}
			if (fret.length() > 0) {
				frets.add(Integer.valueOf(fret));
			}
			if (suffix.length() > 0) {
				techniques.add(suffix);
			}
			
			log.debug("statement {} created: prefix {}, fret {}, suffix {}", statement, prefix, fret, suffix);
		}
		
		//TODO interpret complex statements
	}
	
	public Double getMillisecond() {
		return millisecond;
	}
	
	public void setMillisecond(Double millisecond) { // TODO mechanism for reinitialize in guitarstring data structures
		this.millisecond = millisecond;
	}
	
	public boolean isSemiDecreasable() {
		return frets.size() > 0 && frets.stream().allMatch(f -> f.intValue() > 0);
	}
	
	public boolean isSemiIncreasable() {
		return frets.size() > 0 && frets.stream().allMatch(f -> f.intValue() < 24);
	}
	
	public boolean isOctaveDecreasable() {
		return frets.size() > 0 && frets.stream().allMatch(f -> f.intValue() >= 12);
	}
	
	public boolean isOctaveIncreasable() {
		return frets.size() > 0 && frets.stream().allMatch(f -> f.intValue() <= 12);
	}
	
	
	public double getVelocity() {
		return velocity;
	}

	public void setVelocity(double velocity) {
		this.velocity = velocity;
	}

	public void release() {
		guitarString = null;
		guitar = null;
		statement = null;
		millisecond = null;
	}

	@Override
	public int compareTo(TabStatement tabStatement) {
		// it is explicitly wanted that the equals-method returns true on the same object-reference 
		
		if (tabStatement == null) return 1;
		return getMillisecond().compareTo(tabStatement.getMillisecond());
	}
	
	@Override
	public String toString() {
		return "tab:" + statement + " fretcount: " + frets.size() + " techcount: " + techniques.size()
			+ ", millisecond:" + millisecond + ", guitar:" + guitar + " guitarstring:" + guitarString;
	}
	
	public int getStatementFret(int index) {
		return frets.get(index);
	}
	
	public int getFretCount() {
		return frets.size();
	}
	
	public boolean isStopTab() {
		if (frets.size() == 0 && initTechnique.toLowerCase().equals("x")) {
			return true;
		} else {
			return false;
		}
	}
	
	public Tone getStatementTone(int index) {
		Temperament temperament = guitarString.getGuitar().getTemperament();
		
		return temperament.getTone(temperament.getIndex(guitarString.getEmptyString()) + getStatementFret(index));	
	}
	
	public String getPitchShiftedStatement(int semitoneSteps) {
		String result = initTechnique;
		int currentFret;
		
		for (int i=0; i < frets.size(); i++) {
			currentFret = (frets.get(i) + semitoneSteps);
			if (currentFret < 0) {
				currentFret = 0;
			} else if (currentFret > MAX_FRET) {
				currentFret = MAX_FRET;
			}
			result += currentFret;
			if (i < techniques.size()) {
				result += techniques.get(i);
			}
		}
		
		return result;
	}
	
	
}
