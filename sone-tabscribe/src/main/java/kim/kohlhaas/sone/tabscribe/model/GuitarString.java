package kim.kohlhaas.sone.tabscribe.model;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import kim.kohlhaas.sone.harmony.Tone;

public class GuitarString {
	
	private Tone emptyString;
	private TreeSet<TabStatement> timeLine = new TreeSet<>(); 
	private TreeMap<Double, TabStatement> timeRange = new TreeMap<>(); // TODO probably unnecessary, treeset sufficient
	private Guitar guitar;
		
	public Tone getEmptyString() {
		return emptyString;
	}

	public void setEmptyString(Tone emptyString) {
		this.emptyString = emptyString;
	}
	
	public TabStatement addTabStatement(TabStatement tabStatement) {
		tabStatement.setGuitar(guitar);
		tabStatement.setGuitarString(this);
		TabStatement replaced = timeRange.put(tabStatement.getMillisecond(), tabStatement);
		
		// only one tab statement at the same time position possible
		if (replaced != null) {
			timeLine.remove(replaced);
		}
		
		// sorted by compareTo of TabStatement
		timeLine.add(tabStatement);
		
		return replaced;
	}
	
	public void removeTabStatement(TabStatement tabStatement) {
		timeRange.remove(tabStatement.getMillisecond(), tabStatement);
		timeLine.remove(tabStatement);
	}
	
	public Iterator<TabStatement> tabIterator() {
		return new Iterator<TabStatement> () {
			Iterator<TabStatement> treeSetIterator = timeLine.iterator();
			
			@Override
			public boolean hasNext() {
				return treeSetIterator.hasNext();

			}

			@Override
			public TabStatement next() {
				return treeSetIterator.next();
			}
			
		};
	}
	
	public Map.Entry<Double, TabStatement> firstStatement(Double milliFrom, boolean fromInclusive, Double milliToExclusive) {
		Map.Entry<Double, TabStatement> entry = null;
		if (fromInclusive) {
			entry = timeRange.ceilingEntry(milliFrom);
		} else {
			entry = timeRange.higherEntry(milliFrom);
		}
		
		if (entry != null) {
			if (entry.getKey().compareTo(milliToExclusive) >= 0) {
				entry = null;
			} 
		}
		
		return entry;
	}
	
	@Override
	public String toString() {
		return "guitar string, empty string tone: " + emptyString + ", tab statements: " + timeLine.size();
	}

	public Guitar getGuitar() {
		return guitar;
	}

	public void setGuitar(Guitar guitar) {
		this.guitar = guitar;
	}
	
	
	
}
