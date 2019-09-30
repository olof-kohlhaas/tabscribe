package kim.kohlhaas.sone.tabscribe.javafx.control;

import javafx.event.Event;
import javafx.event.EventType;

public class BeatBarEvent extends Event {
	
	private static final long serialVersionUID = 8583493656568091056L;
	
	public static final EventType<BeatBarEvent> ANY = new EventType<>("BeatBarEvent - any");
	public static final EventType<BeatBarEvent> BAR_LINE_UPDATED = new EventType<>(ANY, "BeatBarEvent - bar line updated");
	public static final EventType<BeatBarEvent> BEAT_CLICKED = new EventType<>(ANY, "BeatBarEvent - beat clicked");
	public static final EventType<BeatBarEvent> BEAT_PRESSED = new EventType<>(ANY, "BeatBarEvent - beat pressed");
	public static final EventType<BeatBarEvent> BEAT_RELEASED = new EventType<>(ANY, "BeatBarEvent - beat released");
	public static final EventType<BeatBarEvent> MILLISECOND_SELECTED = new EventType<>(ANY, "BeatBarEvent - millisecond selected");
	
	private double leftMillisecond = 0.0;
	private double rightMillisecond = 0.0;
	private double millisecond = 0.0;
	
	public BeatBarEvent(EventType<BeatBarEvent> eventType) {
		super(eventType);
	}
	
	public BeatBarEvent(EventType<BeatBarEvent> eventType, double leftMillisecond, double rightMillisecond) {
		super(eventType);
		
		this.leftMillisecond = leftMillisecond;
		this.rightMillisecond = rightMillisecond;
	}
	
	public BeatBarEvent(EventType<BeatBarEvent> eventType, double millisecond) {
		super(eventType);

		this.millisecond = millisecond;
	}

	public double getLeftMillisecond() {
		return leftMillisecond;
	}

	public void setLeftMillisecond(double leftMillisecond) {
		this.leftMillisecond = leftMillisecond;
	}

	public double getRightMillisecond() {
		return rightMillisecond;
	}

	public void setRightMillisecond(double rightMillisecond) {
		this.rightMillisecond = rightMillisecond;
	}

	public double getMillisecond() {
		return millisecond;
	}

	public void setMillisecond(double millisecond) {
		this.millisecond = millisecond;
	}
	
	
	
}
