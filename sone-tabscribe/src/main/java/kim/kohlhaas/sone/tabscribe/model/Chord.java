package kim.kohlhaas.sone.tabscribe.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kim.kohlhaas.sone.harmony.GenericTone;
import kim.kohlhaas.sone.harmony.Interval;
import kim.kohlhaas.sone.harmony.Key;
import kim.kohlhaas.sone.harmony.Mode;
import kim.kohlhaas.sone.harmony.Scale;
import kim.kohlhaas.sone.harmony.ScaleThesaurus;
import kim.kohlhaas.sone.harmony.Tone;

public class Chord {
	
	private final static Logger log = LoggerFactory.getLogger(Chord.class);
	
	private static String subscriptDigits = "\u2080\u2081\u2082\u2083\u2084\u2085\u2086\u2087\u2088\u2089";
	private static String flat = "\u266D";
    private static String sharp = "\u266F";	
	private static String superscriptDigits = "\u2070\u00B9\u00B2\u00B3\u2074\u2075\u2076\u2077\u2078\u2079";
	private static String superscriptPlus = "\u207A";
	private static String superscriptSus = "\u02E2\u1D58\u02E2";
	private static String superscriptAdd = "\u1D43\u1D48\u1D48";
	private static String superscriptMaj = "\u1D50\u1D43\u02B2";
	private static String thinSpace = "\u2009";
	
	public static final Interval PERFECT_UNISON = new Interval(Interval.Name.UNISON, Interval.Type.PERFECT);
	public static final Interval MAJOR_SECOND = new Interval(Interval.Name.SECOND, Interval.Type.MAJOR);
	public static final Interval MAJOR_THIRD = new Interval(Interval.Name.THIRD, Interval.Type.MAJOR);
	public static final Interval MINOR_THIRD = new Interval(Interval.Name.THIRD, Interval.Type.MINOR);
	public static final Interval PERFECT_FOURTH = new Interval(Interval.Name.FOURTH, Interval.Type.PERFECT);
	public static final Interval PERFECT_FIFTH = new Interval(Interval.Name.FIFTH, Interval.Type.PERFECT);
	public static final Interval MAJOR_SEVENTH = new Interval(Interval.Name.SEVENTH, Interval.Type.MAJOR);
	public static final Interval MINOR_SEVENTH = new Interval(Interval.Name.SEVENTH, Interval.Type.MINOR);
	public static final Interval MAJOR_NINTH = new Interval(Interval.Name.NINTH, Interval.Type.MAJOR);
	
	//TODO make frets, tones and tuning more generic. this one is meant to be temporarily quick and dirty, for guitar standard tuning only
	private int[] frets;
	private GenericTone keynote = null;
	private Mode mode;
	private String intervalString = "";
	private String keyString = "";
	private HashSet<Tone> tones; // TODO unify with frets and tabstatements
	private HashSet<Interval> intervals; // TODO unify with interval-String
	
	// TODO this is the chord collection constructor, to be unified with the constructor chord detection beneath
	public Chord(GenericTone.Name tone, GenericTone.Semitone semitone, Mode mode, String intervalString, String fretsDelimitedByMinus) { 
		this.keynote = new GenericTone(tone, semitone);
		this.mode = mode;
		this.intervalString = intervalString;
				
		if (fretsDelimitedByMinus != null && fretsDelimitedByMinus.length() > 0) {
			String[] fretsAsString = fretsDelimitedByMinus.split("\\-");
			frets = new int[fretsAsString.length];
			int index = 0;
			for (String fret : fretsAsString) {
				if (fret.equals("x")) {
					frets[index] = -1;
				} else {
					frets[index] = Integer.parseInt(fret);
				}
				index++;
			}
		} else {
			frets = null;
		}
	}
	
	// TODO this is the chord detection constructor, to be unified with the constructor chord collection above
	public Chord(HashSet<Tone> tones) {
		this.tones = tones;
		Chord result = null;
		Chord resultAlt = null;
		ArrayList<Chord> triads = null;
		GenericTone keynote;
		Key key;
		Iterator<Scale> scaleIterator;		
		Scale currentScale;
		Interval.Type type;
		
		if (tones.size() == 1) {
			keynote = tones.iterator().next().getGenericTone();
			result = new Chord(keynote, Mode.NONE, "1");
			result.addInterval(PERFECT_UNISON);
			result.setTones(tones);
		} else {
		
			HashSet<Scale> unisonScales = ScaleThesaurus.getInstance().getScalesByUnison(tones);
			HashSet<Scale> thirdScales = ScaleThesaurus.getInstance().getScalesByThird(tones);
			HashSet<Scale> fifthScales = ScaleThesaurus.getInstance().getScalesByFifth(tones);
									
			unisonScales.retainAll(fifthScales);
			
			log.debug("potential power chord scales: {}", unisonScales);
					
			if (unisonScales.size() == 2 && tones.size() >= 2) {
				keynote = unisonScales.iterator().next().getKey().getKeynote();
				if (keynote.equals(unisonScales.iterator().next().getKey().getKeynote())) {
					result = new Chord(keynote, Mode.NONE, "" + superscriptDigits.charAt(5)); 
					result.addInterval(PERFECT_FIFTH);
				}
			} 
					
			unisonScales.retainAll(thirdScales);
			
			log.debug("triad scales: {}", unisonScales);
			
			if (unisonScales.size() > 0) {
				if (unisonScales.size() > 1) {
					triads = new ArrayList<>();
				}
				
				scaleIterator = unisonScales.iterator();
				while (scaleIterator.hasNext()) {
					currentScale = scaleIterator.next();
					key = currentScale.getKey();
					result = new Chord(key.getKeynote(), key.getMode(), "");				
					
					if (key.getMode() == Mode.MAJOR) {
						result.addInterval(MAJOR_THIRD);
					} else if (key.getMode() == Mode.MINOR) {
						result.addInterval(MINOR_THIRD);
					}
					
					type = ScaleThesaurus.getInstance().getIntervalType(Interval.Name.SEVENTH, key.getKeynote(), tones);
					
					if (type == Interval.Type.MAJOR) {
						result.addInterval(MAJOR_SEVENTH);
					} else if (type == Interval.Type.MINOR) {
						result.addInterval(MINOR_SEVENTH);
					}
					
					type = ScaleThesaurus.getInstance().getIntervalType(Interval.Name.NINTH, key.getKeynote(), tones);
					
					if (type != null) {
						result.addInterval(MAJOR_NINTH);
					}
					
					if (result.containsInterval(Chord.MINOR_SEVENTH)) {
						if (result.containsInterval(Chord.MAJOR_NINTH)) {
							result.setIntervalString("" + superscriptDigits.charAt(9));
						} else {
							result.setIntervalString("" + superscriptDigits.charAt(7));
						}
						
					} else if (result.containsInterval(Chord.MAJOR_SEVENTH)) {
						if (result.containsInterval(Chord.MAJOR_NINTH)) {
							result.setIntervalString("" + superscriptMaj + thinSpace + superscriptDigits.charAt(9));
						} else {
							result.setIntervalString("" + superscriptDigits.charAt(7) + superscriptPlus);
						}	
					} else {
						if (result.containsInterval(Chord.MAJOR_NINTH)) {
							result.setIntervalString("" + superscriptAdd + thinSpace + superscriptDigits.charAt(9));
						}
					}
					
					if (triads != null) {
						triads.add(result);
					}	
				}
				
				if (triads != null) {
					log.debug("triad chords: {}" + triads);
					result = triads.stream().max(Comparator.comparing(Chord::getIntervalComprehensiveness)).get();
				}
			} else {
				unisonScales = ScaleThesaurus.getInstance().getScalesByUnison(tones, Mode.MAJOR);
				unisonScales.retainAll(fifthScales);
				HashSet<Scale> unisonScalesAlt = ScaleThesaurus.getInstance().getScalesByUnison(tones, Mode.MAJOR);
				unisonScalesAlt.retainAll(fifthScales);
				
				HashSet<Scale> secondScales = ScaleThesaurus.getInstance().getScalesBySecond(tones, Mode.MAJOR);
				HashSet<Scale> fourthScales = ScaleThesaurus.getInstance().getScalesByFourth(tones, Mode.MAJOR);
									
				unisonScales.retainAll(secondScales);
				unisonScalesAlt.retainAll(fourthScales);				
				
				
				log.debug("chord tones: {}", tones);
				log.debug("sus2 scales: {}", unisonScales);
				log.debug("sus4 scales: {}", unisonScalesAlt);
				
				if (unisonScales.size() == 1 && unisonScalesAlt.size() == 1) {
				
					TreeSet<Tone> sortedTones = new TreeSet<>();
					
					for (Tone tone : tones) {
						sortedTones.add(tone);
					}
					
					if (unisonScales.size() == 1) {
						keynote = unisonScales.iterator().next().getKey().getKeynote(); 
						result = new Chord(keynote, Mode.NONE, superscriptSus + thinSpace + superscriptDigits.charAt(2));
						result.addInterval(MAJOR_SECOND); 
					} 
						
					if (unisonScalesAlt.size() == 1) {			
						keynote = unisonScalesAlt.iterator().next().getKey().getKeynote(); 
						resultAlt = new Chord(keynote, Mode.NONE, superscriptSus + thinSpace + superscriptDigits.charAt(4));
						resultAlt.addInterval(Chord.PERFECT_FOURTH);
					}
					
					for (Tone tone : sortedTones) {
						if (tone.getGenericTone().equals(result.getKeynote())) {
							break;
						} else if (tone.getGenericTone().equals(resultAlt.getKeynote())) {
							result = resultAlt;
							break;
						}
					}
				}
			}
		}
		
		if (result != null) {
			parseTmpChord(result);
			assembleKeyString();
		}
	}	
	
	// tmp chord value object constructor
	private Chord(GenericTone keynote, Mode mode, String intervalString) {
		this.keynote = keynote;
		this.mode = mode;
		this.intervalString = intervalString;
		intervals = new HashSet<>();
	}
	
	private void parseTmpChord(Chord tmpChord) {
		this.keynote = tmpChord.getKeynote();
		this.mode = tmpChord.getMode();
		this.intervalString = tmpChord.getIntervalString();
		this.intervals = tmpChord.getIntervals();
	}
	
	private void assembleKeyString() {
		if (intervalString.equals("1")) {
			keyString = getTone().name().toLowerCase();
			intervalString = "" + subscriptDigits.charAt(tones.iterator().next().getOctave());
		} else {
			keyString = getTone().name();
		}
		
		if (getSemitone() == GenericTone.Semitone.FLAT) {
			keyString += flat;
		} else if (getSemitone() == GenericTone.Semitone.SHARP) {
			keyString += sharp;
		}
		
		if (getMode() == Mode.MINOR) {
			keyString += "m";
		} 
	}
	
	public boolean containsInterval(Interval interval) {
		return intervals.contains(interval);
	}
	
	HashSet<Interval> getIntervals() {
		return intervals;
	}
	
	int getIntervalComprehensiveness() {
		if (containsInterval(Chord.MINOR_SEVENTH)) {
			if (containsInterval(Chord.MAJOR_NINTH)) {
				return 6;
			} else {
				return 3;
			}
		} else if (containsInterval(Chord.MAJOR_SEVENTH)) {
			if (containsInterval(Chord.MAJOR_NINTH)) {
				return 6;
			} else {
				return 3;
			}	
		} else {
			if (containsInterval(Chord.MAJOR_NINTH)) {
				return 2;
			} else {
				return 1;
			}
		}
	}

	public GenericTone.Name getTone() {
		return keynote.getName();
	}

	public GenericTone.Semitone getSemitone() {
		return keynote.getSemitone();
	}

	public Mode getMode() {
		return mode;
	}
	
	public void addInterval(Interval interval) {
		intervals.add(interval);
	}

	public String getIntervalString() {
		return intervalString;
	}
			
	public String getKeyString() {
		return keyString;
	}
	
	public String getChordString () {
		return keyString + intervalString;
	}

	public void setIntervalString(String intervalString) {
		this.intervalString = intervalString;
	}

	public void deployTabStatements(Guitar guitar, double millisecond) {
		// TODO make it generic for different tunings other amounts of guitar strings
		TabStatement statement;
		for (int i = 0; i < frets.length && i < guitar.getStringCount(); i++) {
			if (frets[i] != -1) {
				statement = new TabStatement("" + frets[i], millisecond);
				statement.setVelocity(1.0);
				guitar.addTabStatement(i, statement);
			}
		}
	}

	public HashSet<Tone> getTones() {
		return tones;
	}
	
	public void setTones(HashSet<Tone> tones) {
		this.tones = tones;
	}

	public GenericTone getKeynote() {
		return keynote;
	}

	@Override
	public String toString() {
		return "Chord [frets=" + Arrays.toString(frets) + ", tone=" + keynote.getName() + ", semitone=" + keynote.getSemitone() + ", mode=" + mode
				+ ", interval=" + intervalString + ", tones=" + tones + ", intervals=" + intervals + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(frets);
		result = prime * result + ((intervalString == null) ? 0 : intervalString.hashCode());
		result = prime * result + ((intervals == null) ? 0 : intervals.hashCode());
		result = prime * result + ((keynote == null) ? 0 : keynote.hashCode());
		result = prime * result + ((mode == null) ? 0 : mode.hashCode());
		result = prime * result + ((tones == null) ? 0 : tones.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Chord other = (Chord) obj;
		if (!Arrays.equals(frets, other.frets))
			return false;
		if (intervalString == null) {
			if (other.intervalString != null)
				return false;
		} else if (!intervalString.equals(other.intervalString))
			return false;
		if (intervals == null) {
			if (other.intervals != null)
				return false;
		} else if (!intervals.equals(other.intervals))
			return false;
		if (keynote == null) {
			if (other.keynote != null)
				return false;
		} else if (!keynote.equals(other.keynote))
			return false;
		if (mode != other.mode)
			return false;
		if (tones == null) {
			if (other.tones != null)
				return false;
		} else if (!tones.equals(other.tones))
			return false;
		return true;
	}

}
