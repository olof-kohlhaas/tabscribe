package kim.kohlhaas.sone.tabscribe.javafx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import kim.kohlhaas.sone.Player;
import kim.kohlhaas.sone.event.EventHandler;
import kim.kohlhaas.sone.event.LoopEvent;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;


public class StatusPlotterProcess {
    
    final static Logger log = LoggerFactory.getLogger(StatusPlotterProcess.class);
    
    private final DoubleProperty positionProperty = new SimpleDoubleProperty(0.0);
    private Player player;
    private int interval;
    private boolean isRunning = false;
    private final DoubleProperty loopStartingProperty = new SimpleDoubleProperty(0.0);
    private final DoubleProperty loopTerminalProperty = new SimpleDoubleProperty(0.0);
    private EventHandler<LoopEvent> onLoopTriggered;
    
    private Timeline timeline = null;
    
    public StatusPlotterProcess(Player player) {
        this.player = player;
    }
            
    public void refreshProperties() {
        if (isRunning) {
            positionProperty.setValue(player.getMillisecondPosition());
            if (loopTerminalProperty.get() > 0.0 
                    && loopTerminalProperty.get() > loopStartingProperty.get() 
                    && positionProperty.get() >= loopTerminalProperty.get()) {
                onLoopTriggered.handle(new LoopEvent(this, positionProperty.get(), loopStartingProperty.get()));
            }
        }
    }
    
    public DoubleProperty getPositionProperty() {
        return positionProperty;
    }

    public StatusPlotterProcess setInterval(int millis) {
        this.interval = millis;
        return this;
    }
    
    public void start() {
        if (!isRunning) {
            isRunning = true;
            if (timeline != null) {
                timeline.stop();
            }
            timeline = new Timeline(new KeyFrame(Duration.millis(interval), 
            	(a) -> refreshProperties()
            ));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        }
    }
    
    public void stop() {
        isRunning = false;
        if (timeline != null) {
            timeline.stop();
            Platform.runLater(() -> positionProperty.set(0.0));
        }
    }

    public DoubleProperty loopStartingProperty() {
        return loopStartingProperty;
    }

    public DoubleProperty loopTerminalProperty() {
        return loopTerminalProperty;
    }

    public EventHandler<LoopEvent> getOnLoopTriggered() {
        return onLoopTriggered;
    }

    public void setOnLoopTriggered(EventHandler<LoopEvent> onLoopTriggered) {
        this.onLoopTriggered = onLoopTriggered;
    }
    
    

}
