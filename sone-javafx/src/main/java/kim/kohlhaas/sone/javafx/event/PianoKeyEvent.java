package kim.kohlhaas.sone.javafx.event;


import javafx.event.Event;
import javafx.event.EventType;
import kim.kohlhaas.sone.harmony.Tone;

public class PianoKeyEvent extends Event {

    /**
     * 
     */
    private static final long serialVersionUID = 2116994850237273044L;
    public static final EventType<PianoKeyEvent> ANY = new EventType<>("PianoKeyEvent - any");
    public static final EventType<PianoKeyEvent> MOUSE_CLICKED = new EventType<>(ANY, "PianoKeyEvent - mouse clicked");
    
    private Tone tone;   
    
    public PianoKeyEvent(EventType<PianoKeyEvent> eventType, Tone tone) {
        super(eventType);
        this.tone = tone;
    }

    public Tone getTone() {
        return tone;
    }

    public void setTone(Tone tone) {
        this.tone = tone;
    }


}
