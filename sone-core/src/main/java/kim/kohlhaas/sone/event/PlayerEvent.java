package kim.kohlhaas.sone.event;

public class PlayerEvent extends Event {
    
    private double millisecond = 0;
    
    public enum PlayerState {
        INITIALIZED,
        PLAYING,
        SEEKING,
        PAUSED,
        CLOSED,
        FINISHED
    }
    
    private PlayerState state = PlayerState.INITIALIZED;
    

    public PlayerEvent(Object source) {
        super(source);
        // TODO Auto-generated constructor stub
    }
    
    public PlayerEvent(Object source, PlayerState state) {
        this(source);
        this.state = state;
    }
        
    public PlayerState getState() {
        return state;
    }

    public PlayerEvent setState(PlayerState state) {
        this.state = state;
        return this;
    }
    
    public double getMillisecond() {
        return millisecond;
    }

    public PlayerEvent setMillisecond(double millisecond) {
        this.millisecond = millisecond;
        return this;
    }

    /**
     * 
     */
    private static final long serialVersionUID = 2325882023433770243L;
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "PlayerEvent [state=" + state + ", millisecond=" + millisecond +"]";
    }
}
