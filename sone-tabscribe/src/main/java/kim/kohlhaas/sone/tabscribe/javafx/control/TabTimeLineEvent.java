package kim.kohlhaas.sone.tabscribe.javafx.control;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.TreeSet;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.input.MouseEvent;
import kim.kohlhaas.sone.tabscribe.model.GuitarString;
import kim.kohlhaas.sone.tabscribe.model.Lyrics;
import kim.kohlhaas.sone.tabscribe.model.LyricsStatement;
import kim.kohlhaas.sone.tabscribe.model.TabStatement;

/**
 * @author Olof Kohlhaas
 *
 */
public class TabTimeLineEvent extends Event {
	// TODO reorganize Events: make event types and parameters consistent.
	/**
	 * 
	 */
	private static final long serialVersionUID = 8583493656568091056L;
	public static final EventType<TabTimeLineEvent> ANY = new EventType<>("TabTimeLineEvent - any");
	// TODO group event types, e.g. lyrics, tabs, string
	public static final EventType<TabTimeLineEvent> TAB_TRIGGERED = new EventType<>(ANY, "TabTimeLineEvent - tab triggered");
	public static final EventType<TabTimeLineEvent> BEAT_TRIGGERED = new EventType<>(ANY, "TabTimeLineEvent - beat triggered");
	public static final EventType<TabTimeLineEvent> BEAT_CONTEXT = new EventType<>(ANY, "TabTimeLineEvent - beat context");
	public static final EventType<TabTimeLineEvent> TAB_CLICKED = new EventType<>(ANY, "TabTimeLineEvent - tab clicked");
	public static final EventType<TabTimeLineEvent> LYRICS_CLICKED = new EventType<>(ANY, "TabTimeLineEvent - lyrics clicked");
	public static final EventType<TabTimeLineEvent> LYRICS_DRAGGED = new EventType<>(ANY, "TabTimeLineEvent - lyrics draggeed");
	public static final EventType<TabTimeLineEvent> LYRICS_CREATED = new EventType<>(ANY, "TabTimeLineEvent - lyrics created");
	public static final EventType<TabTimeLineEvent> LYRICS_CHANGED = new EventType<>(ANY, "TabTimeLineEvent - lyrics changed");
	public static final EventType<TabTimeLineEvent> TAB_DRAGGED = new EventType<>(ANY, "TabTimeLineEvent - tab dragged");
	public static final EventType<TabTimeLineEvent> STRING_CLICKED = new EventType<>(ANY, "TabTimeLineEvent - string clicked");
	public static final EventType<TabTimeLineEvent> STRING_RIGHT_CLICKED = new EventType<>(ANY, "TabTimeLineEvent - string right clicked");
	
	private double lastMillisecond = -1.0;
	private double millisecond = -1.0;
	private double leftBeatMillisecond = -1.0;
	private double rightBeatMillisecond = -1.0;
	private	LinkedHashSet<TabStatement> triggeredTabs = null;
	private TabStatement clickedTab = null;
	private TabStatement draggedTab = null;
	private GuitarString guitarString = null;
	private LyricsStatement lyricsStatement = null;
	private Lyrics lyricsTrack = null;
	private int pitchDiff;
	private MouseEvent mouseEvent = null;
	
	public TabTimeLineEvent(EventType<TabTimeLineEvent> eventType, double lastMillisecond, double millisecond, LinkedHashSet<TabStatement> triggeredTabs) {
		super(eventType);
		this.lastMillisecond = lastMillisecond;
		this.millisecond = millisecond;
		this.triggeredTabs = triggeredTabs;
	}
	
	public TabTimeLineEvent(EventType<TabTimeLineEvent> eventType, LinkedHashSet<TabStatement> triggeredTabs, double leftBeatMillisecond, double rightBeatMillisecond) {
		super(eventType);
		this.leftBeatMillisecond = leftBeatMillisecond;
		this.rightBeatMillisecond = rightBeatMillisecond;
		this.triggeredTabs = triggeredTabs;
	}
	
	public TabTimeLineEvent(EventType<TabTimeLineEvent> eventType, LinkedHashSet<TabStatement> triggeredTabs, double leftBeatMillisecond, double rightBeatMillisecond,  MouseEvent mouseEvent) {
		super(eventType);
		this.leftBeatMillisecond = leftBeatMillisecond;
		this.rightBeatMillisecond = rightBeatMillisecond;
		this.triggeredTabs = triggeredTabs;
		this.mouseEvent = mouseEvent;
	}
	
	public TabTimeLineEvent(EventType<TabTimeLineEvent> eventType, TabStatement clickedTab, MouseEvent mouseEvent) {
		super(eventType);
		this.clickedTab = clickedTab;
		this.mouseEvent = mouseEvent;
	}
	
	public TabTimeLineEvent(EventType<TabTimeLineEvent> eventType, LyricsStatement lyricsStatement, MouseEvent mouseEvent) {
		super(eventType);
		this.lyricsStatement = lyricsStatement;
		this.mouseEvent = mouseEvent;
	}
	
	public TabTimeLineEvent(EventType<TabTimeLineEvent> eventType, double millisecond, TabStatement draggedTab, int pitchDiff) {
		super(eventType);
		this.millisecond = millisecond;
		this.draggedTab = draggedTab;
		this.pitchDiff = pitchDiff;
	}
	
	public TabTimeLineEvent(EventType<TabTimeLineEvent> eventType, double millisecond, LyricsStatement lyricsStatement) {
		super(eventType);
		this.millisecond = millisecond;
		this.lyricsStatement = lyricsStatement;
	}
	
	public TabTimeLineEvent(EventType<TabTimeLineEvent> eventType, double millisecond, GuitarString guitarString) {
		super(eventType);
		this.millisecond = millisecond;
		this.guitarString = guitarString;
	}
	
	public TabTimeLineEvent(EventType<TabTimeLineEvent> eventType, double millisecond, Lyrics lyricsTrack, LyricsStatement lyricsStatement) {
		super(eventType);
		this.millisecond = millisecond;
		this.lyricsTrack = lyricsTrack;
		this.lyricsStatement = lyricsStatement;
	}
	
	public double getLastMillisecond() {
		return lastMillisecond;
	}

	public void setLastMillisecond(double lastMillisecond) {
		this.lastMillisecond = lastMillisecond;
	}

	public double getMillisecond() {
		return millisecond;
	}

	public void setMillisecond(double millisecond) {
		// TODO check whether changing milliseconds corrupts tab identity in collection types
		this.millisecond = millisecond;
	}

	public LinkedHashSet<TabStatement> getTriggeredTabs() {
		return triggeredTabs;
	}

	public void setTriggeredTabs(LinkedHashSet<TabStatement> triggeredTabs) {
		this.triggeredTabs = triggeredTabs;
	}

	public TabStatement getClickedTab() {
		return clickedTab;
	}

	public void setClickedTab(TabStatement clickedTab) {
		this.clickedTab = clickedTab;
	}

	public MouseEvent getMouseEvent() {
		return mouseEvent;
	}

	public void setMouseEvent(MouseEvent mouseEvent) {
		this.mouseEvent = mouseEvent;
	}

	public TabStatement getDraggedTab() {
		return draggedTab;
	}

	public void setDraggedTab(TabStatement draggedTab) {
		this.draggedTab = draggedTab;
	}

	public int getPitchDiff() {
		return pitchDiff;
	}

	public void setPitchDiff(int pitchDiff) {
		this.pitchDiff = pitchDiff;
	}

	public GuitarString getGuitarString() {
		return guitarString;
	}

	public void setGuitarString(GuitarString guitarString) {
		this.guitarString = guitarString;
	}

	public Lyrics getLyricsTrack() {
		return lyricsTrack;
	}

	public void setLyricsTrack(Lyrics lyricsTrack) {
		this.lyricsTrack = lyricsTrack;
	}

	public LyricsStatement getLyricsStatement() {
		return lyricsStatement;
	}

	public void setLyricsStatement(LyricsStatement lyricsStatement) {
		this.lyricsStatement = lyricsStatement;
	}

	public double getLeftBeatMillisecond() {
		return leftBeatMillisecond;
	}

	public double getRightBeatMillisecond() {
		return rightBeatMillisecond;
	}
	
	
	
		
}
