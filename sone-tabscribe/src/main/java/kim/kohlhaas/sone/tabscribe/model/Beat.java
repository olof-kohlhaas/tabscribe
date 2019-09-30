package kim.kohlhaas.sone.tabscribe.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kim.kohlhaas.sone.harmony.GenericTone;
import kim.kohlhaas.sone.harmony.Key;
import kim.kohlhaas.sone.harmony.Mode;
import kim.kohlhaas.sone.harmony.Scale;
import kim.kohlhaas.sone.harmony.ScaleThesaurus;
import kim.kohlhaas.sone.harmony.Tone;

public class Beat {
		
	private final static Logger log = LoggerFactory.getLogger(Beat.class);
	
	double leftMilliInclusive;
	double rightMilliExclusive;
	int leftXInclusive;
	int rightXExclusive;
	Session session;
	
	public Beat(Session session, double leftMilliInclusive, double rightMilliExclusive, int leftXInclusive, int rightXExclusive) {
		this.session = session;
		this.leftMilliInclusive = leftMilliInclusive;
		this.rightMilliExclusive = rightMilliExclusive;
		this.leftXInclusive = leftXInclusive;
		this.rightXExclusive = rightXExclusive;
	}
	
	public HashSet<TabStatement> getTabs() {
		HashSet<TabStatement> result = new HashSet<TabStatement>();
		Map.Entry<Double, TabStatement> currentStringEntry;
		TabStatement currentStatement;
		Track currentTrack;
		Guitar currentGuitar;
		GuitarString currentGuitarString;
		double currentIntervalStart;
		double currentIntervalEnd = rightMilliExclusive;
		boolean currentStartInclusive;
		
		for (int i = 0; i < session.getTrackCount(); i++) {
			currentTrack = session.getTrack(i);
			if (currentTrack.getType() == Track.Type.GUITAR) {
				 currentGuitar = (Guitar) currentTrack;
				 for (int s = 0; s < currentGuitar.getStringCount(); s++) {
					 currentIntervalStart = leftMilliInclusive;
					 currentStartInclusive = true;
					 currentGuitarString = currentGuitar.getString(s);
					 while ((currentStringEntry = currentGuitarString.firstStatement(currentIntervalStart, currentStartInclusive, currentIntervalEnd)) != null) {
						 currentStatement = currentStringEntry.getValue();
						 currentIntervalStart = currentStringEntry.getKey();
						 currentStartInclusive = false;
						 result.add(currentStatement);
					 }
				 }
			}
		}
		
		return result;
	}
	
	public HashSet<Tone> getTones() {
		HashSet<Tone> result = new HashSet<>();
		Map.Entry<Double, TabStatement> currentStringEntry;
		TabStatement currentStatement;
		Track currentTrack;
		Guitar currentGuitar;
		GuitarString currentGuitarString;
		double currentIntervalStart;
		double currentIntervalEnd = rightMilliExclusive;
		boolean currentStartInclusive;
		
		log.debug("get generic tones from {} to {}.", leftMilliInclusive, rightMilliExclusive);
		
		for (int i = 0; i < session.getTrackCount(); i++) {
			currentTrack = session.getTrack(i);
			if (currentTrack.getType() == Track.Type.GUITAR) {
				 currentGuitar = (Guitar) currentTrack;
				 for (int s = 0; s < currentGuitar.getStringCount(); s++) {
					 currentIntervalStart = leftMilliInclusive;
					 currentStartInclusive = true;
					 currentGuitarString = currentGuitar.getString(s);
					 while ((currentStringEntry = currentGuitarString.firstStatement(currentIntervalStart, currentStartInclusive, currentIntervalEnd)) != null) {
						 currentStatement = currentStringEntry.getValue();
						 currentIntervalStart = currentStringEntry.getKey();
						 currentStartInclusive = false;
						 for (int t = 0; t < currentStatement.getFretCount(); t++) {
							 result.add(currentStatement.getStatementTone(t)); // hashset removes duplicates
						 }
					 }
				 }
			}
		}
		
		return result;
	}
	
	public Chord getChordArchetype() {		
		return new Chord(getTones());
	}

	public double getLeftMilliInclusive() {
		return leftMilliInclusive;
	}

	public double getRightMilliExclusive() {
		return rightMilliExclusive;
	}

	public int getLeftXInclusive() {
		return leftXInclusive;
	}

	public int getRightXExclusive() {
		return rightXExclusive;
	}
	
	public int getWidth() {
		return rightXExclusive - leftXInclusive;
	}
	
	
		
}
