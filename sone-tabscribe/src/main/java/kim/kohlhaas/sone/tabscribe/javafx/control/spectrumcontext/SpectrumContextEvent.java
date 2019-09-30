package kim.kohlhaas.sone.tabscribe.javafx.control.spectrumcontext;

import java.util.LinkedHashSet;
import java.util.TreeSet;

import javafx.event.Event;
import javafx.event.EventType;
import kim.kohlhaas.sone.tabscribe.model.Chord;
import kim.kohlhaas.sone.tabscribe.model.Guitar;
import kim.kohlhaas.sone.tabscribe.model.GuitarString;
import kim.kohlhaas.sone.tabscribe.model.TabStatement;

public class SpectrumContextEvent extends Event {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4587282777204857963L;
	public static final EventType<SpectrumContextEvent> ANY = new EventType<>("SpectrumContextEvent - any");
	public static final EventType<SpectrumContextEvent> FRET_SELECTED = new EventType<>(ANY, "SpectrumContextEvent - fret selected");
	public static final EventType<SpectrumContextEvent> CHORD_SELECTED = new EventType<>(ANY, "SpectrumContextEvent - chord selected");
	public static final EventType<SpectrumContextEvent> BEAT_DELETED = new EventType<>(ANY, "SpectrumContextEvent - beat deleted");
	public static final EventType<SpectrumContextEvent> BEAT_ARRANGE_UPSTROKE = new EventType<>(ANY, "SpectrumContextEvent - beat arrange as upstroke");
	public static final EventType<SpectrumContextEvent> BEAT_ARRANGE_DOWNSTROKE = new EventType<>(ANY, "SpectrumContextEvent - beat arrange as downstroke");
	public static final EventType<SpectrumContextEvent> BEAT_ARRANGE_STRAIGHT = new EventType<>(ANY, "SpectrumContextEvent - arrange tabs straight");
	public static final EventType<SpectrumContextEvent> ADD_MUTE_TABS = new EventType<>(ANY, "SpectrumContextEvent - add mute tabs");
	public static final EventType<SpectrumContextEvent> BEAT_LOWER_SEMI = new EventType<>(ANY, "SpectrumContextEvent - beat lower semi");
	public static final EventType<SpectrumContextEvent> BEAT_RAISE_SEMI = new EventType<>(ANY, "SpectrumContextEvent - beat raise semi");
	public static final EventType<SpectrumContextEvent> BEAT_LOWER_OCTAVE = new EventType<>(ANY, "SpectrumContextEvent - beat lower octave");
	public static final EventType<SpectrumContextEvent> BEAT_RAISE_OCTAVE = new EventType<>(ANY, "SpectrumContextEvent - beat raise octave");
	
	private double millisecond;
	private double beatMilliLeft;
	private double beatMilliRight;
	private int fret;
	private GuitarString guitarString;
	private Chord chord;
	private Guitar guitar;
	private LinkedHashSet<TabStatement> triggeredTabs = null;

	public SpectrumContextEvent(EventType<SpectrumContextEvent> eventType, double millisecond, int fret, GuitarString guitarString) {
		super(eventType);
		this.millisecond = millisecond;
		this.fret = fret;
		this.guitarString = guitarString;
	}
	
	public SpectrumContextEvent(EventType<SpectrumContextEvent> eventType, double millisecond, Chord chord, Guitar guitar) {
		super(eventType);
		this.millisecond = millisecond;
		this.fret = fret;
		this.chord = chord;
		this.guitar = guitar;
	}
	
	public SpectrumContextEvent(EventType<SpectrumContextEvent> eventType, double beatMilliLeft, double beatMilliRight, LinkedHashSet<TabStatement> triggeredTabs, Guitar guitar) {
		super(eventType);
		this.beatMilliLeft = beatMilliLeft;
		this.beatMilliRight = beatMilliRight;
		this.triggeredTabs = triggeredTabs;
		this.guitar = guitar;
	}
	
	public double getMillisecond() {
		return millisecond;
	}

	public void setMillisecond(double millisecond) {
		this.millisecond = millisecond;
	}

	public int getFret() {
		return fret;
	}

	public void setFret(int fret) {
		this.fret = fret;
	}

	public GuitarString getGuitarString() {
		return guitarString;
	}

	public void setGuitarString(GuitarString guitarString) {
		this.guitarString = guitarString;
	}

	public Chord getChord() {
		return chord;
	}

	public void setChord(Chord chord) {
		this.chord = chord;
	}

	public Guitar getGuitar() {
		return guitar;
	}

	public void setGuitar(Guitar guitar) {
		this.guitar = guitar;
	}

	public double getBeatMilliLeft() {
		return beatMilliLeft;
	}

	public void setBeatMilliLeft(double beatMilliLeft) {
		this.beatMilliLeft = beatMilliLeft;
	}

	public double getBeatMilliRight() {
		return beatMilliRight;
	}

	public void setBeatMilliRight(double beatMilliRight) {
		this.beatMilliRight = beatMilliRight;
	}

	public LinkedHashSet<TabStatement> getTriggeredTabs() {
		return triggeredTabs;
	}

	public void setTriggeredTabs(LinkedHashSet<TabStatement> triggeredTabs) {
		this.triggeredTabs = triggeredTabs;
	}
	
}
