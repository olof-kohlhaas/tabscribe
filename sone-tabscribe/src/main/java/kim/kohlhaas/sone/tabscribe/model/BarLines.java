package kim.kohlhaas.sone.tabscribe.model;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class BarLines {
	private TreeMap<Double, BarLine> barLines = new TreeMap<>();

	public BarLines() {
		addBarLine(new BarLine(0.0));
	}
	
	public void addBarLine(BarLine barLine) {
		if (barLines.isEmpty() || barLine.getMillisecond() > 0.0) {
			barLines.put(barLine.getMillisecond(), barLine);
		}
	}
	
	public int getBarLineCount() {
		return barLines.size();
	}
	
	public void removeBarLine(BarLine barLine) {
		barLines.remove(barLine.getMillisecond(), barLine);
	}
	
	public BarLine getDefaultBarLine() {
		return barLines.firstEntry().getValue();
	}
	
	BarLine getHigherBarLine(Double millisecond) {
		Map.Entry<Double, BarLine> result = barLines.higherEntry(millisecond);
		
		if (result != null) {
			return result.getValue();
		} else {
			return null;
		}
	}
	
	public Iterator<BarLine> barLineIterator() {
		return new Iterator<BarLine> () {
			Iterator<BarLine> treeSetIterator = barLines.values().iterator();
			
			@Override
			public boolean hasNext() {
				return treeSetIterator.hasNext();

			}

			@Override
			public BarLine next() {
				return treeSetIterator.next();
			}
			
		};
	}	
}
