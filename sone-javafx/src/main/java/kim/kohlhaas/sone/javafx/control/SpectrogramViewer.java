package kim.kohlhaas.sone.javafx.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.util.Duration;
import kim.kohlhaas.javafx.color.ColorRange;
import kim.kohlhaas.sone.analyze.FloatSpectrogram;
import kim.kohlhaas.sone.javafx.event.HoverEvent;
import kim.kohlhaas.sone.javafx.control.SpectrogramTimeLine;

public class SpectrogramViewer extends StackPane {
    
	final static Logger log = LoggerFactory.getLogger(SpectrogramViewer.class);
    private VBox vBox;
    private Pane overlayContainer;
    private Line position;
    private Line frequencyLineLeft;
    private Line frequencyLineRight;
    private Line mouseTimeLineNorth;
    private Line mouseTimeLineSouth;
    private Line loopStartingLine;
    private Line loopTerminalLine;
    private Rectangle leftBeatBorder;
    private Rectangle rightBeatBorder;
    private SpectrogramChannel spectrogramChannel;
    private SpectrogramTimeLine timeLine;
    private Rectangle clippingMask;
    private int toneResolution = 1;
    private boolean hasHoverExited = true;
    
    private DoubleProperty hoverMillisecondProperty;
    private ReadOnlyDoubleWrapper hoverMillisecondReadWrapper;
    
    private final IntegerProperty hoverIndexProperty;
    private final DoubleProperty millisecondProperty;
    private final BooleanProperty timeSelectOnProperty = new SimpleBooleanProperty(true);
    private final DoubleProperty mouseXProperty;
    private final DoubleBinding hoverMillisecondBinding;
	private final DoubleProperty mouseBeatLeftXProperty;
	private final DoubleProperty mouseBeatRightXProperty;
    
    private ChangeListener<Number> hoverToneElsewhereHandler;
    private ChangeListener<Number> hoverTimeElsewhereHandler;
    
    private Stop[] leftBeatBorderStops = new Stop[] {new Stop(0, Color.web("#ffffff00")), new Stop(1, Color.web("#ffffff4b"))};
    private Stop[] rightBeatBorderStops = new Stop[] {new Stop(0, Color.web("#ffffff4b")), new Stop(1, Color.web("#ffffff00"))};
    private LinearGradient leftBeatBorderGradient = new LinearGradient(0.0, 0.0, 1.0, 0.0, true, CycleMethod.NO_CYCLE, leftBeatBorderStops);
    private LinearGradient rightBeatBorderGradient = new LinearGradient(0.0, 0.0, 1.0, 0.0, true, CycleMethod.NO_CYCLE, rightBeatBorderStops);
    
    public SpectrogramViewer() {
        vBox = new VBox();
        overlayContainer = new Pane();
        timeLine = new SpectrogramTimeLine();
        position = new Line(1, 1, 1, 1);
        frequencyLineLeft = new Line(0, 0, 10, 10);
        frequencyLineLeft.setVisible(false);
        frequencyLineLeft.setStrokeLineCap(StrokeLineCap.BUTT);
        frequencyLineLeft.getStyleClass().add("frequency-line");
        frequencyLineRight = new Line(0, 0, 10, 10);
        frequencyLineRight.setVisible(false);
        frequencyLineRight.setStrokeLineCap(StrokeLineCap.BUTT);
        frequencyLineRight.getStyleClass().add("frequency-line");
        mouseTimeLineNorth = new Line(0, 0, 10, 10);
        mouseTimeLineNorth.setVisible(false);
        mouseTimeLineNorth.setStrokeWidth(0.5);
        mouseTimeLineNorth.setStrokeLineCap(StrokeLineCap.BUTT);
        mouseTimeLineNorth.getStyleClass().add("position-line");
        mouseTimeLineSouth = new Line(0, 0, 10, 10);
        mouseTimeLineSouth.setVisible(false);
        mouseTimeLineSouth.setStrokeWidth(0.5);
        mouseTimeLineSouth.setStrokeLineCap(StrokeLineCap.BUTT);
        mouseTimeLineSouth.getStyleClass().add("position-line");
        loopStartingLine = new Line(0, 0, 10, 10);
        loopStartingLine.setVisible(false);
        loopStartingLine.setStrokeLineCap(StrokeLineCap.BUTT);
        loopStartingLine.getStyleClass().add("loop-line");
        loopStartingLine.startXProperty().bind(timeLine.loopStartingX());
        loopStartingLine.endXProperty().bind(timeLine.loopStartingX());
        loopTerminalLine = new Line(0, 0, 10, 10);
        loopTerminalLine.setVisible(false);
        loopTerminalLine.setStrokeLineCap(StrokeLineCap.BUTT);
        loopTerminalLine.getStyleClass().add("loop-line");
        loopTerminalLine.startXProperty().bind(timeLine.loopTerminalX());
        loopTerminalLine.endXProperty().bind(timeLine.loopTerminalX());
        position.getStyleClass().add("position-line");
		mouseBeatLeftXProperty = new SimpleDoubleProperty();
		mouseBeatRightXProperty = new SimpleDoubleProperty();
		leftBeatBorder = new Rectangle(0.0, 0.0, 450.0, 10.0);
		rightBeatBorder = new Rectangle(0.0, 0.0, 450.0, 10.0);
		leftBeatBorder.setVisible(false);
		rightBeatBorder.setVisible(false);
		leftBeatBorder.setFill(leftBeatBorderGradient);
		rightBeatBorder.setFill(rightBeatBorderGradient);
		
		mouseBeatLeftXProperty.addListener((observable, oldValue, newValue) -> {
			if (newValue.doubleValue() > 0.0) {
				leftBeatBorder.setX(newValue.doubleValue() - leftBeatBorder.getWidth());
				leftBeatBorder.setVisible(true);
			} else {
				leftBeatBorder.setX(-leftBeatBorder.getWidth());
				leftBeatBorder.setVisible(false);
			}
		});
		
		mouseBeatRightXProperty.addListener((observable, oldValue, newValue) -> {			
			if (newValue.doubleValue() > 0.0) {
				rightBeatBorder.setX(newValue.doubleValue());
				rightBeatBorder.setVisible(true);
			} else {
				rightBeatBorder.setX(spectrogramChannel.getWidth());
				rightBeatBorder.setVisible(false);
			}
		});
        
        timeLine.loopStartingX().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() >= 0.0 && newValue.doubleValue() < getWidth() && loopStartingProperty().get() > 0.0) {
                loopStartingLine.setVisible(true);
            } else {
                loopStartingLine.setVisible(false);
            }
        });
        
        timeLine.loopTerminalX().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() >= 0.0 && newValue.doubleValue() < getWidth() && loopTerminalProperty().get() > 0.0) {
                loopTerminalLine.setVisible(true);
            } else {
                loopTerminalLine.setVisible(false);
            }
        });
        
        loopStartingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() == 0.0) {
                loopStartingLine.setVisible(false);
            }
        });
        
        loopTerminalProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() == 0.0) {
                loopTerminalLine.setVisible(false);
            }
        });
        
        clippingMask = new Rectangle();
        this.setClip(clippingMask); 
        
        this.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            clippingMask.setWidth(newValue.getWidth());
            clippingMask.setHeight(newValue.getHeight());
            leftBeatBorder.setHeight(newValue.getHeight());
            rightBeatBorder.setHeight(newValue.getHeight());       
        });
        
        overlayContainer.getChildren().addAll(position, frequencyLineLeft, frequencyLineRight, 
                mouseTimeLineNorth, mouseTimeLineSouth, loopStartingLine, loopTerminalLine,
                leftBeatBorder, rightBeatBorder);
        
        hoverMillisecondProperty = new SimpleDoubleProperty(0.0);
        hoverMillisecondReadWrapper = new ReadOnlyDoubleWrapper(this, "hoverMillisecondProperty");
        hoverMillisecondReadWrapper.bind(hoverMillisecondProperty);    
        
        hoverToneElsewhereHandler = (observable, oldValue, newValue) -> hoverToneElsewhere();
        hoverTimeElsewhereHandler = (observable, oldValue, newValue) -> hoverTimeElsewhere();
        
        hoverIndexProperty = new SimpleIntegerProperty();        
        
                
        millisecondProperty = new SimpleDoubleProperty();
        mouseXProperty = new SimpleDoubleProperty();
        
        spectrogramChannel = new SpectrogramChannel();
        
        VBox.setVgrow(spectrogramChannel, Priority.ALWAYS);
        vBox.getChildren().addAll(timeLine, spectrogramChannel);
        this.getChildren().addAll(vBox, overlayContainer);
        this.setAlignment(Pos.TOP_LEFT);        

        hoverMillisecondBinding = new DoubleBinding() {
            {
                super.bind(millisecondProperty, mouseXProperty);
            }
            
            @Override
            protected double computeValue() {
            	if (spectrogramChannel.getSpectrogram() != null) {
                return millisecondProperty.get() 
                        + spectrogramChannel.getSpectrogram().getMilliseconds(
                                (mouseXProperty.get() - getWidth() / 2) / spectrogramChannel.getZoomXScale()
                        );
            	} else {
            		return 0.0;
            	}
            }
        };
        
        hoverMillisecondProperty.bind(hoverMillisecondBinding);
        
        this.addEventHandler(MouseEvent.MOUSE_MOVED, e -> setMousePointer(e.getSceneX(), e.getSceneY()));
        
        this.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
        	hoverIndexProperty.removeListener(hoverToneElsewhereHandler);
        	mouseXProperty.removeListener(hoverTimeElsewhereHandler);
        });
        
        this.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
        	hoverIndexProperty.addListener(hoverToneElsewhereHandler);
        	mouseXProperty.addListener(hoverTimeElsewhereHandler);
            hideHover();

        });
        
        
        this.addEventHandler(ScrollEvent.SCROLL, e -> {
            spectrogramChannel.verticalScrollPosition().set(
                    spectrogramChannel.verticalScrollPosition().get() - e.getDeltaY()
            );
            
            setMousePointer(e.getSceneX(), e.getSceneY());
        });
        
        spectrogramChannel.heightProperty().addListener((observable, oldValue, newValue) -> refresh());
        spectrogramChannel.widthProperty().addListener((observable, oldValue, newValue) -> refresh());
        millisecondProperty.addListener((observable, oldValue, newValue) -> {
        	try {
	            spectrogramChannel.setMillisecondPosition(millisecondProperty.get());
	            timeLine.setMillisecondPosition(millisecondProperty.get());
        	} catch (ArrayIndexOutOfBoundsException exception) {
        		log.warn("spectrogram probably accessed while it is loading: {}", exception.getMessage());
        		log.error("Excption: {}", exception);
        	}
        });
        
    }
    
    private void hoverTimeElsewhere() {
		mouseTimeLineNorth.setVisible(true);
		mouseTimeLineNorth.setStartX(mouseXProperty.get());
        mouseTimeLineNorth.setStartY(timeLine.getHeight());
        mouseTimeLineNorth.setEndX(mouseXProperty.get());
        mouseTimeLineNorth.setEndY(timeLine.getHeight() + spectrogramChannel.getHeight());
    }
    
    private void hoverToneElsewhere() {
        int index = hoverIndexProperty.get();
        double y;
        double ySnap;

        if (index != -1) {
            index = spectrogramChannel.getSpectrogram().getFreqs() - index - 1;
            y = (index - spectrogramChannel.verticalScrollPosition().get());
            ySnap = Math.floor(y / (toneResolution)) * (toneResolution * spectrogramChannel.getCanvasYScale()) + toneResolution * spectrogramChannel.getCanvasYScale() / 2.0;
            ySnap +=  timeLine.getHeight();
            if (ySnap >  timeLine.getHeight()) {
                frequencyLineLeft.setStartX(0);
                frequencyLineLeft.setEndX(getWidth() / 2 - 10 * spectrogramChannel.getZoomXScale());
                frequencyLineLeft.setStartY(ySnap);
                frequencyLineLeft.setEndY(ySnap);
                frequencyLineRight.setStartX(getWidth() / 2 + 10 * spectrogramChannel.getZoomXScale());
                frequencyLineRight.setEndX(getWidth());
                frequencyLineRight.setStartY(ySnap);
                frequencyLineRight.setEndY(ySnap);
                frequencyLineLeft.setVisible(true);
                frequencyLineRight.setVisible(true);
            } else {
                frequencyLineLeft.setVisible(false);
                frequencyLineRight.setVisible(false);
            }
        } else {
            frequencyLineLeft.setVisible(false);
            frequencyLineRight.setVisible(false);
        }
    }
    
    private void hideHover() {
        mouseTimeLineNorth.setVisible(false);
        mouseTimeLineSouth.setVisible(false);
        frequencyLineLeft.setVisible(false);
        frequencyLineRight.setVisible(false);
        mouseXProperty.set(-1); // TODO find another way to hide the bar 
        hasHoverExited = true;
        this.fireEvent(new HoverEvent(HoverEvent.HOVER_EXITED));
        
    }
    
    private void setMousePointer(double sceneX, double sceneY) {
        if (spectrogramChannel.getSpectrogram() != null) {                
            
            int freqIndex;
            double y = sceneY - this.localToScene(this.getBoundsInLocal()).getMinY() - timeLine.getHeight();
            double ySnap = Math.floor(y / (toneResolution * spectrogramChannel.getCanvasYScale())) * (toneResolution * spectrogramChannel.getCanvasYScale()) + toneResolution * spectrogramChannel.getCanvasYScale() / 2.0;
            
            if (ySnap < 0) {
                hideHover();
            } else {
                mouseTimeLineNorth.setVisible(true);
                mouseTimeLineSouth.setVisible(true);
                frequencyLineLeft.setVisible(true);
                frequencyLineRight.setVisible(true);
                
                freqIndex = spectrogramChannel.getSpectrogram().getFreqs() - 1 - (int) Math.floor((y + (int) spectrogramChannel.verticalScrollPosition().get()) / spectrogramChannel.getCanvasYScale());
                                
                mouseXProperty.set(sceneX - this.localToScene(this.getBoundsInLocal()).getMinX());

                frequencyLineLeft.setStartX(0);
                frequencyLineLeft.setEndX(mouseXProperty.get() - 10 * spectrogramChannel.getZoomXScale());
                frequencyLineLeft.setStartY(ySnap + timeLine.getHeight());
                frequencyLineLeft.setEndY(ySnap + timeLine.getHeight());
                frequencyLineRight.setStartX(mouseXProperty.get() + 10 * spectrogramChannel.getZoomXScale());
                frequencyLineRight.setEndX(getWidth());
                frequencyLineRight.setStartY(ySnap + timeLine.getHeight());
                frequencyLineRight.setEndY(ySnap + timeLine.getHeight());
                mouseTimeLineNorth.setStartX(mouseXProperty.get());
                mouseTimeLineNorth.setStartY(timeLine.getHeight());
                mouseTimeLineNorth.setEndX(mouseXProperty.get());
                mouseTimeLineNorth.setEndY(ySnap - Math.ceil((toneResolution * spectrogramChannel.getCanvasYScale()) / 2.0 - 1) + timeLine.getHeight());
                mouseTimeLineSouth.setStartX(mouseXProperty.get());
                mouseTimeLineSouth.setStartY(ySnap + Math.ceil((toneResolution * spectrogramChannel.getCanvasYScale()) / 2.0 + 1) + timeLine.getHeight());
                mouseTimeLineSouth.setEndX(mouseXProperty.get());
                mouseTimeLineSouth.setEndY(timeLine.getHeight() + spectrogramChannel.getHeight());
                
                if (hasHoverExited) {
                    hasHoverExited = false;
                    this.fireEvent(new HoverEvent(HoverEvent.HOVER_ENTERED, mouseXProperty.get(), ySnap + timeLine.getHeight()));
                } else {
                    this.fireEvent(new HoverEvent(HoverEvent.HOVER_MOVED, mouseXProperty.get(), ySnap + timeLine.getHeight()));
                }
                
                hoverIndexProperty.set(freqIndex);
                
            }
        }
    }
    
    
    public final ReadOnlyDoubleProperty hoverMillisecondProperty() {
        return hoverMillisecondReadWrapper.getReadOnlyProperty();
    }
    
    public final ReadOnlyDoubleProperty maxScroll() {
        return spectrogramChannel.maxScroll();
    }
    
    public final ReadOnlyDoubleProperty visibleAmount() {
        return spectrogramChannel.visibleAmount();
    }
    
    public DoubleProperty verticalScrollPosition() {
        return spectrogramChannel.verticalScrollPosition();
    }
    
    public FloatSpectrogram getSpectrogram() {
        return spectrogramChannel.getSpectrogram();
    }
    
    public void setColorRange(ColorRange colorRange) {
        spectrogramChannel.setColorRange(colorRange);
    }

    public void setSpectrogram(FloatSpectrogram spectrogram) {
        spectrogramChannel.setSpectrogram(spectrogram);
        timeLine.setFactorXScale(spectrogramChannel.getZoomXScale());
        frequencyLineLeft.setStrokeWidth(toneResolution * spectrogramChannel.getCanvasYScale());
        frequencyLineRight.setStrokeWidth(toneResolution * spectrogramChannel.getCanvasYScale());
        timeLine.setSpectrogram(spectrogram);
    }
    
    public void refresh() {
        spectrogramChannel.reset();
        frequencyLineLeft.setStrokeWidth(toneResolution * spectrogramChannel.getCanvasYScale());
        frequencyLineRight.setStrokeWidth(toneResolution * spectrogramChannel.getCanvasYScale());
        timeLine.reset();
        position.setStartX(getWidth() / 2 + (getWidth() % 2 == 0 ? 0.5 : 0.0));
        position.setEndX(getWidth() / 2 + (getWidth() % 2 == 0 ? 0.5 : 0.0));
        position.setStartY(timeLine.getHeight() + 1.0);
        position.setEndY(getHeight());
        loopStartingLine.setStartY(timeLine.getHeight() + 1.0);
        loopStartingLine.setEndY(getHeight());
        loopTerminalLine.setStartY(timeLine.getHeight() + 1.0);
        loopTerminalLine.setEndY(getHeight());
    }
    
    public int getToneResolution() {
        return toneResolution;
    }

    public void setToneResolution(int toneResolution) {
        this.toneResolution = toneResolution;
        frequencyLineLeft.setStrokeWidth(toneResolution * spectrogramChannel.getCanvasYScale());
        frequencyLineRight.setStrokeWidth(toneResolution * spectrogramChannel.getCanvasYScale());
    }

    @Override
    public boolean isResizable() {
        return true;
    }
    
    @Override
    public double computeMaxHeight(double width) {
        if (spectrogramChannel.getSpectrogram() != null) {
            return timeLine.getHeight() + spectrogramChannel.getSpectrogram().getFreqs();
        } else {
            return timeLine.getHeight() + 100.0;
        }
    }
    
    @Override
    public double computeMinHeight(double width) {
        return timeLine.getHeight() + 100.0;
    }

    public IntegerProperty hoverIndexProperty() {
        return hoverIndexProperty;
    }

    public DoubleProperty mouseXProperty() {
    	return mouseXProperty;
    }
    
    public DoubleProperty millisecondProperty() {
        return millisecondProperty;
    }

    public BooleanProperty timeSelectOnProperty() {
        return timeSelectOnProperty;
    }
    
    public DoubleProperty loopStartingProperty() {
        return timeLine.loopStartingProperty();
    }

    public DoubleProperty loopTerminalProperty() {
        return timeLine.loopTerminalProperty();
    }
    
    public void setZoomXScale(double zoomXScale) {
    	spectrogramChannel.setZoomXScale(zoomXScale);
    	timeLine.setFactorXScale(spectrogramChannel.getZoomXScale());
    	refresh();
    }
    
    public double getZoomXScale() {
    	return spectrogramChannel.getZoomXScale();
    }
    
    public final ReadOnlyDoubleProperty millisecondWidthProperty() {
    	return timeLine.millisecondWidthProperty();
    }
    
	
	public DoubleProperty mouseBeatLeftXProperty() {
		return mouseBeatLeftXProperty;
	}
	
	public DoubleProperty mouseBeatRightXProperty() {
		return mouseBeatRightXProperty;
	}
    
}
