package kim.kohlhaas.sone.tabscribe.javafx.control;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import kim.kohlhaas.sone.javafx.event.HoverEvent;
import kim.kohlhaas.sone.tabscribe.model.BeatParser;

public class BeatBar extends StackPane {
	
	private BeatWave beatWave;
	private Line position;
	private Pane overlayContainer;
	private final DoubleProperty mouseXProperty;
	private final DoubleProperty mouseBeatLeftXProperty;
	private final DoubleProperty mouseBeatRightXProperty;
	private final DoubleProperty mouseBeatLeftMilliProperty;
	private final DoubleProperty mouseBeatRightMilliProperty;
	private Line mouseTimeLine;
	private DoubleProperty hoverMillisecondProperty;

    private EventHandler<MouseEvent> moveHandler;
    private ChangeListener<Number> hoverOutsideHandler;
    private ChangeListener<Number> hoverBeatHandler;
    private boolean isPressed = false;
	
	public BeatBar() {
		position = new Line(1, 1, 1, 1);
		mouseTimeLine = new Line(0, 0, 10, 10);
        mouseTimeLine.setVisible(false);
        mouseTimeLine.setStrokeWidth(0.5);
        mouseTimeLine.setStrokeLineCap(StrokeLineCap.BUTT);
        mouseTimeLine.getStyleClass().add("position-line");
        position.setStrokeWidth(0.5);
        position.setStrokeLineCap(StrokeLineCap.BUTT);
        position.getStyleClass().add("position-line");
		mouseXProperty = new SimpleDoubleProperty();
		mouseBeatLeftXProperty = new SimpleDoubleProperty(0.0);
		mouseBeatRightXProperty = new SimpleDoubleProperty(0.0);
		mouseBeatLeftMilliProperty = new SimpleDoubleProperty(0.0);
		mouseBeatRightMilliProperty = new SimpleDoubleProperty(0.0);
        
        overlayContainer = new Pane();
        overlayContainer.getChildren().addAll(position, mouseTimeLine);
        
        hoverMillisecondProperty = new SimpleDoubleProperty(0.0);          
        moveHandler = e -> setMousePointer(e.getSceneX(), e.getSceneY());
        hoverOutsideHandler = (observable, oldValue, newValue) -> hoverTimeElsewhere();
        hoverBeatHandler = (observable, oldValue, newValue) -> calcBeatBorders();
        this.widthProperty().addListener((obs, oldValue, newValue) -> {
        	position.setStartX(getWidth() / 2 + (getWidth() % 2 == 0 ? 0.5 : 0.0));
            position.setEndX(getWidth() / 2 + (getWidth() % 2 == 0 ? 0.5 : 0.0));
            position.setStartY(1.0);
            position.setEndY(getHeight());
        });
	}
		
	public void init(BeatParser beatParser, int samplesPerTimeStep, double maxZoomScale) {
		beatWave = new BeatWave(beatParser, samplesPerTimeStep, maxZoomScale);
		
		this.getChildren().addAll(beatWave, overlayContainer);
		this.setAlignment(Pos.TOP_LEFT);
		
        this.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
        	beatWave.getBeatParser().setMouseOver(true);
        	beatWave.getBeatParser().millisecondProperty().addListener(hoverBeatHandler);
        	mouseXProperty.removeListener(hoverOutsideHandler);
        	setMousePointer(e.getSceneX(), e.getSceneY());
        	this.addEventHandler(MouseEvent.MOUSE_MOVED, moveHandler);
        	this.addEventHandler(MouseEvent.MOUSE_DRAGGED, moveHandler);
        	this.fireEvent(new HoverEvent(HoverEvent.HOVER_ENTERED));
        });
        
        this.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
        	if (e.getButton() == MouseButton.SECONDARY || e.getButton() == MouseButton.PRIMARY && e.isControlDown()) {
        		this.fireEvent(new BeatBarEvent(BeatBarEvent.MILLISECOND_SELECTED, hoverMillisecondProperty.doubleValue()));
        	} else if (e.getButton() == MouseButton.PRIMARY && !e.isControlDown()) {
        		this.fireEvent(new BeatBarEvent(BeatBarEvent.BEAT_CLICKED, mouseBeatLeftMilliProperty.doubleValue(), mouseBeatRightMilliProperty.doubleValue()));
        	} 
        });
        
        this.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
        	if (e.getButton() == MouseButton.PRIMARY && !e.isControlDown()) {
	        	this.isPressed = true;
	        	this.fireEvent(new BeatBarEvent(BeatBarEvent.BEAT_PRESSED, mouseBeatLeftMilliProperty.doubleValue(), mouseBeatRightMilliProperty.doubleValue()));
        	}
        });
        
        this.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
        	if (e.getButton() == MouseButton.SECONDARY || e.getButton() == MouseButton.PRIMARY && e.isControlDown()) {
        		this.fireEvent(new BeatBarEvent(BeatBarEvent.MILLISECOND_SELECTED, hoverMillisecondProperty.doubleValue()));
        	} else if (e.getButton() == MouseButton.PRIMARY && !e.isControlDown()) {
	        	this.isPressed = false;
	        	this.fireEvent(new BeatBarEvent(BeatBarEvent.BEAT_RELEASED, mouseBeatLeftMilliProperty.doubleValue(), mouseBeatRightMilliProperty.doubleValue()));
        	}
        });
        
        this.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
        	beatWave.getBeatParser().setMouseOver(false);
        	beatWave.getBeatParser().millisecondProperty().removeListener(hoverBeatHandler);
        	mouseXProperty.addListener(hoverOutsideHandler);
        	mouseBeatLeftXProperty.set(0.0);
			mouseBeatRightXProperty.set(0.0);
        	this.removeEventHandler(MouseEvent.MOUSE_MOVED, moveHandler);
        	this.removeEventHandler(MouseEvent.MOUSE_DRAGGED, moveHandler);
            hideHover();
            this.fireEvent(new HoverEvent(HoverEvent.HOVER_EXITED));
        });
        
        mouseBeatLeftXProperty.addListener( (obs, oldValue, newValue) -> {
        	if (isPressed) {
        		this.fireEvent(new BeatBarEvent(BeatBarEvent.BEAT_PRESSED, mouseBeatLeftMilliProperty.doubleValue(), mouseBeatRightMilliProperty.doubleValue()));
        	}
        });
        
	}

	private void calcBeatBorders() {
		beatWave.getBeatParser().setMouseX(mouseXProperty.doubleValue());		
		mouseBeatLeftMilliProperty.set(beatWave.getBeatParser().getMouseBeatStartMilli());
		mouseBeatRightMilliProperty.set(beatWave.getBeatParser().getMouseBeatEndMilli());
		mouseBeatLeftXProperty.set(beatWave.getBeatParser().getMouseBeatStartX());
		mouseBeatRightXProperty.set(beatWave.getBeatParser().getMouseBeatEndX());
		hoverMillisecondProperty.set(beatWave.getBeatParser().getMouseMilli());
	}
	
	private void hideHover() {		
        mouseTimeLine.setVisible(false);
        mouseXProperty.set(-1); // TODO find another way to hide the bar 
    }
    
    private void setMousePointer(double sceneX, double sceneY) {
    	double y = sceneY - this.localToScene(this.getBoundsInLocal()).getMinY();
    	
        if (beatWave != null) {                
            mouseTimeLine.setVisible(true);                           
            mouseXProperty.set(sceneX - this.localToScene(this.getBoundsInLocal()).getMinX());

            mouseTimeLine.setStartX(mouseXProperty.doubleValue());
            mouseTimeLine.setStartY(0.0);
            mouseTimeLine.setEndX(mouseXProperty.doubleValue());
            mouseTimeLine.setEndY(getHeight());
            
            calcBeatBorders();
            this.fireEvent(new HoverEvent(HoverEvent.HOVER_MOVED, mouseXProperty.get(), y));
        }
    }
	
    private void hoverTimeElsewhere() {
		mouseTimeLine.setVisible(true);
		mouseTimeLine.setStartX(mouseXProperty.get());
        mouseTimeLine.setStartY(0.0);
        mouseTimeLine.setEndX(mouseXProperty.get());
        mouseTimeLine.setEndY(beatWave.getHeight());
    }
	
	public BeatWave getBeatWave() {
		return beatWave;
	}
	
	public DoubleProperty mouseXProperty() {
		return mouseXProperty;
	}
	
	public DoubleProperty hoverMillisecondProperty() {
		return hoverMillisecondProperty;
	}
	
	public DoubleProperty mouseBeatLeftXProperty() {
		return mouseBeatLeftXProperty;
	}
	
	public DoubleProperty mouseBeatRightXProperty() {
		return mouseBeatRightXProperty;
	}
	
	public DoubleProperty mouseBeatLeftMilliProperty() {
		return mouseBeatLeftMilliProperty;
	}
	
	public DoubleProperty mouseBeatRightMilliProperty() {
		return mouseBeatRightMilliProperty;
	}
	
}
