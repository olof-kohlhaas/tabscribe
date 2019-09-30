package kim.kohlhaas.sone.event;

public class LoopEvent extends Event {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private double triggerPosition;
    private double restartPosition;
    
    public LoopEvent(Object source, double triggerPosition, double restartPosition) {
        super(source);
        
        this.triggerPosition = triggerPosition;
        this.restartPosition = restartPosition;
    }

    public double getTriggerPosition() {
        return triggerPosition;
    }

    public double getRestartPosition() {
        return restartPosition;
    }

    @Override
    public String toString() {
        return "LoopEvent [triggerPosition=" + triggerPosition + ", restartPosition=" + restartPosition + "]";
    }
    

}
