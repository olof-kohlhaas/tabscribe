package kim.kohlhaas.sone.javafx.event;

import javafx.event.Event;
import javafx.event.EventType;

public class SeekEvent extends Event {

    /**
     * 
     */
    private static final long serialVersionUID = -5370972844962980546L;
    public static final EventType<SeekEvent> ANY = new EventType<>("SeekEvent - any");
    public static final EventType<SeekEvent> SEEK = new EventType<>(ANY, "SeekEvent - seek");
    
    private double millisecond;
    
    public SeekEvent(EventType<SeekEvent> eventType, double millisecond) {
        super(eventType);
        this.millisecond = millisecond;
    }    
    
    public double getMillisecond() {
        return millisecond;
    }

    public void setMillisecond(double millisecond) {
        this.millisecond = millisecond;
    }





}
