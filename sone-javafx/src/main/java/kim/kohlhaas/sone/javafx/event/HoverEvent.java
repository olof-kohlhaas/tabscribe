package kim.kohlhaas.sone.javafx.event;

import javafx.event.Event;
import javafx.event.EventType;
import kim.kohlhaas.sone.harmony.Tone;

public class HoverEvent extends Event {
    
    /**
     * 
     */
    private static final long serialVersionUID = -8170154298297177785L;
    public static final EventType<HoverEvent> ANY = new EventType<>("HoverEvent - any");
    public static final EventType<HoverEvent> HOVER_MOVED = new EventType<>(ANY, "HoverEvent - moved");
    public static final EventType<HoverEvent> HOVER_ENTERED = new EventType<>(HOVER_MOVED, "HoverEvent - entered");
    public static final EventType<HoverEvent> HOVER_EXITED = new EventType<>(ANY, "HoverEvent - exited");
    
    private double localX;
    private double localY;
    
    public HoverEvent(EventType<HoverEvent> eventType) {
        super(eventType);
    }
    
    public HoverEvent(EventType<HoverEvent> eventType, double localX, double localY) {
        super(eventType);

        this.localX = localX;
        this.localY = localY;
    }

    public double getLocalX() {
        return localX;
    }

    public double getLocalY() {
        return localY;
    }
    
}
