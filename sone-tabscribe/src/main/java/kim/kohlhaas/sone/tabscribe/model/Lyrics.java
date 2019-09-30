package kim.kohlhaas.sone.tabscribe.model;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Lyrics implements Track {

	private String name;
	private TreeMap<Double, LyricsStatement> timeRange = new TreeMap<>();
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	public LyricsStatement addStatement(LyricsStatement lyricsStatement) {
		lyricsStatement.setTrack(this);
		LyricsStatement replaced = timeRange.put(lyricsStatement.getMillisecond(), lyricsStatement);
				
		return replaced;
	}
	
	public void removeStatement(LyricsStatement lyricsStatement) {
		timeRange.remove(lyricsStatement.getMillisecond(), lyricsStatement);
	}
	
	public Set<Map.Entry<Double, LyricsStatement>> entrySet() {
		return timeRange.entrySet();
	}
	
	public Map.Entry<Double, LyricsStatement> firstStatement(Double milliFrom, boolean fromInclusive, Double milliTo) {
		//TODO generalize this method with generics, see firstStatement-method in GuitarString
		
		Map.Entry<Double, LyricsStatement> entry = null;
		
		if (fromInclusive) {
			entry = timeRange.ceilingEntry(milliFrom);
		} else {
			entry = timeRange.higherEntry(milliFrom);
		}
		
		if (entry != null) {
			if (entry.getKey().compareTo(milliTo) > 0) {
				entry = null;
			} 
		}
		
		return entry;
	}
	
	@Override
	public String toString() {
		return "lyrics: " + name + ", phrases: " + timeRange.size();
	}

	@Override
	public Type getType() {
		return Type.LYRICS;
	}

}
