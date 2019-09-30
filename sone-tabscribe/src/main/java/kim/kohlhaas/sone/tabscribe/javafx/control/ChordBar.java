package kim.kohlhaas.sone.tabscribe.javafx.control;

import java.nio.ByteBuffer;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import kim.kohlhaas.sone.javafx.Environment;
import kim.kohlhaas.sone.tabscribe.model.Beat;
import kim.kohlhaas.sone.tabscribe.model.BeatParser;
import kim.kohlhaas.sone.tabscribe.model.Chord;

public class ChordBar extends Region {
	
	private final static Logger log = LoggerFactory.getLogger(ChordBar.class);
	private Canvas canvas;
	private GraphicsContext graphicsContext;
	private DoubleProperty hoverMillisecondProperty;
	private final DoubleProperty mouseXProperty;
	private final DoubleProperty mouseBeatLeftXProperty;
	private final DoubleProperty mouseBeatRightXProperty;
	private final DoubleProperty mouseBeatLeftMilliProperty;
	private final DoubleProperty mouseBeatRightMilliProperty;
	private double viewPortWidth = 0.0;
	private double height = 0.0;
    private PixelWriter pixelWriter;
    private PixelReader pixelReader;
    private WritablePixelFormat<ByteBuffer> bufferFormat;
    private WritableImage writableImage;
    private WritableImage chordImage;
    private PixelReader chordReader;
    private SnapshotParameters chordSnapshotParam;
    private boolean isBeatX;
    private int lastBeatX;
    private int currentBeatX;
    private Color barBackgroundColor;
    private Color backgroundColor;
    private Color backgroundShadowColor;
    private Color measureColor;
    private double lastMillisecondPosition;
    private byte[] copyArray;
    private double maxWidth = Environment.getMaxSingleScreenWidth();
    private static final int BYTES_PER_PIXEL = 4;
    private static final int MEASURE_FLAG_PIXELS = 10;
    private int lastPixelPosition;
    private ChangeListener<Number> hoverBeatHandler;
    private BeatParser beatParser;
    private StackPane stackPane = new StackPane();
    private VBox snapshotPane = new VBox();
    private HashMap<Chord, WritableImage> chordImages = new HashMap<>();
    private Label chordText = new Label();
    private boolean isChordImageCachingOn = false;
    private Color[] measureColors;
    
    public ChordBar(BeatParser beatParser) {
    	this.beatParser = beatParser;
		
    	measureColors = new Color[beatParser.colors.length];
		for (int i = 0; i < beatParser.colors.length; i++) {
			if (beatParser.colors[i] == null) {
				measureColors[i] = null;
			} else {
				measureColors[i] = Color.web(beatParser.colors[i]);
			}
		}
    	
		barBackgroundColor = Color.web("#3c3f41");
		backgroundColor = Color.web("#9a9ea1");
		backgroundShadowColor = Color.web("#676c6f");
		measureColor = Color.web("#73787c");
		bufferFormat = WritablePixelFormat.getByteBgraInstance();
		
		this.setPrefHeight(95.0);
		VBox.setVgrow(this, Priority.NEVER);
		
		chordSnapshotParam = new SnapshotParameters();
		chordSnapshotParam.setFill(measureColor);
		chordSnapshotParam.setTransform(Transform.rotate(270.0, 0.0, 0.0));
		
		chordText.getStyleClass().add("chord-label");
		chordText.applyCss();
		
		snapshotPane.getChildren().add(chordText);
		snapshotPane.layout();
		
		canvas = new Canvas();
        stackPane.getChildren().addAll(snapshotPane, canvas); // chordText visible, but hidden behind canvas on purpose for snapshots
        stackPane.setAlignment(Pos.TOP_LEFT); 
        this.getChildren().add(stackPane);
        
		graphicsContext = canvas.getGraphicsContext2D();
		mouseXProperty = new SimpleDoubleProperty();
		mouseBeatLeftXProperty = new SimpleDoubleProperty(0.0);
		mouseBeatRightXProperty = new SimpleDoubleProperty(0.0);
		mouseBeatLeftMilliProperty = new SimpleDoubleProperty(0.0);
		mouseBeatRightMilliProperty = new SimpleDoubleProperty(0.0);
		hoverBeatHandler = (observable, oldValue, newValue) -> calcBeatBorders();
				
		hoverMillisecondProperty = new SimpleDoubleProperty(0.0);

		beatParser.viewPortWidthProperty().addListener((observable, oldValue, newValue) -> resetDimensions());
        heightProperty().addListener((observable, oldValue, newValue) -> resetDimensions());
                        
        beatParser.millisecondProperty().addListener((observable, oldValue, newValue) -> {
        	lastMillisecondPosition = newValue.doubleValue();

			if (lastMillisecondPosition < oldValue.doubleValue()) {
				draw(true);
			} else {
				draw(false);
			}
        });
		
		beatParser.millisecondWidthProperty().addListener((observable, oldValue, newValue) -> {
			draw(true);
        });
		
		this.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
        	mouseXProperty.addListener(hoverBeatHandler);
        	beatParser.millisecondProperty().addListener(hoverBeatHandler);
        	beatParser.setMouseOver(true);
        });
        
		this.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
        	mouseXProperty.removeListener(hoverBeatHandler);
        	beatParser.millisecondProperty().removeListener(hoverBeatHandler);
        	mouseBeatLeftXProperty.set(0.0);
			mouseBeatRightXProperty.set(0.0);
			beatParser.setMouseOver(false);
		});
		
		resetDimensions();
    }
    
    
	private void resetDimensions() {
		viewPortWidth = beatParser.viewPortWidthProperty().doubleValue();
		height = getHeight();
		if (viewPortWidth > 0 && height > 0) {
			canvas.setWidth(viewPortWidth);
	        canvas.setHeight(height);
			writableImage = new WritableImage((int) viewPortWidth, (int) height);
	    	pixelWriter = writableImage.getPixelWriter();
	        pixelReader = writableImage.getPixelReader();
	        copyArray = new byte[(int) maxWidth * (int) height * BYTES_PER_PIXEL];
	        draw(true);
		}
	}
		
	public void draw(boolean sweep) {
		int virtualX;
		int virtualLeftX;
		int deltaX = 0;
		int startX = 0;
		Chord currentChord = null;
		Beat currentBeat;
		double currentCopyWidth;
		int currentBeatXGap = 0;
		int currentBeatYGap = 0;
		double overhang;
		boolean currentChordDrawn = false;
		int beatDiff = 0 ;
		WritableImage tmpImage;
        Color xColor;
                
		if(!sweep) {
			beatDiff = (int) Math.ceil(viewPortWidth - lastBeatX);
			deltaX = beatParser.getPixelPosition() - lastPixelPosition;
			startX = (int) viewPortWidth - deltaX - beatDiff;
					
			if (deltaX >= viewPortWidth) {
				sweep = true;
			}
		}
		
		if(sweep) {
			lastPixelPosition = beatParser.getPixelPosition();
			startX = 0;
			deltaX = 0;
			lastBeatX = 0;
		}
        
		if (!sweep) {           	
            pixelReader.getPixels(deltaX, 0, (int) viewPortWidth - deltaX - beatDiff, (int) height, bufferFormat, copyArray, 0,  ((int) viewPortWidth - deltaX - beatDiff) * BYTES_PER_PIXEL);
            pixelWriter.setPixels(0, 0, (int) viewPortWidth - deltaX -beatDiff, (int) height, bufferFormat, copyArray, 0, ((int) viewPortWidth - deltaX - beatDiff) * BYTES_PER_PIXEL);
            lastBeatX -= deltaX; 
        }	
        
		virtualLeftX = beatParser.getPixelPosition() - (int) (viewPortWidth / 2.0);
        for (int x = 0; x < (int) viewPortWidth; x++) {
        	virtualX = virtualLeftX + x;
        	if (isBeatX = beatParser.isBeatLine(virtualX)) {
        		currentBeatX = x;
    			currentChord = null;
			}
        	
        	if (isBeatX && beatParser.getCeilingBeatX(virtualX) >= virtualLeftX + startX) {
        		currentBeat = beatParser.getBeat(virtualX);
    			currentChord = currentBeat.getChordArchetype();
    			if (currentChord.getKeynote() != null) {
    				log.debug("chord found {}", currentChord);
    				currentChordDrawn = false;
    			}
    			
    			if (currentChord.getKeynote() != null) {
        			if (!isChordImageCachingOn || (chordImage = chordImages.get(currentChord)) == null) {

	        			chordText.setText(currentChord.getChordString());
	        			chordText.applyCss();
	        			snapshotPane.layout();

	        			chordImage = chordText.snapshot(chordSnapshotParam, null);
	        			tmpImage = chordImages.put(currentChord, chordImage);
	        			log.debug("image {} created for chord {} and replaced {}.", chordImage, currentChord, tmpImage);
        			} else {
        				log.debug("image {} found for chord {}.", chordImage, currentChord);
        			}
        			
        	        chordReader = chordImage.getPixelReader();
        	        currentBeatXGap = currentBeatX + (int) ((currentBeat.getWidth() - chordImage.getWidth()) / 2.0);
        	        currentBeatYGap = (int) (height - chordImage.getHeight()) - 2;   	        
    			}
    		}
        	
        	if (sweep || x >= startX ) {
        		
	        	for (int y = 0; y < (int) height; y++) {
	        		if (y == height - 1) {
	        			pixelWriter.setColor(x, y, backgroundShadowColor);
	        		} else {
		        		if (isBeatX) {
		        			if (y > MEASURE_FLAG_PIXELS) {
		        				pixelWriter.setColor(x, y, backgroundColor);
		        			} else {
		        				pixelWriter.setColor(x, y, backgroundColor);
		        			}		    
		        		} else {
		        			xColor = measureColors[beatParser.getMeasureColor(virtualX)];
		        			
		        			if (xColor != null) {
        						if (y > MEASURE_FLAG_PIXELS) {        							
        							if (currentChord == null ||  !currentChordDrawn || x <= currentBeatXGap ||  x >= currentBeatXGap + chordImage.getWidth()-1 || y <= currentBeatYGap || y >= currentBeatYGap + chordImage.getHeight()) {							
        								pixelWriter.setColor(x, y, measureColor);
        							}
        						} else {
        							pixelWriter.setColor(x, y, xColor);
        						}	        					
		        			} else {
		        				pixelWriter.setColor(x, y, barBackgroundColor);
		        			}
		        		}
	        		}
	        	}	        	
	        	
	        	if (currentChord != null && currentChord.getKeynote() != null && !currentChordDrawn && x >= startX && x > currentBeatXGap && !isBeatX) {
        			currentCopyWidth = chordImage.getWidth() - 1 - (x - currentBeatXGap);        			
        			if (currentCopyWidth > 0) {
        				if ((overhang = x + currentCopyWidth) > viewPortWidth) {
        					currentCopyWidth -= (overhang - viewPortWidth);
        				}
        				pixelWriter.setPixels(x, currentBeatYGap, (int) currentCopyWidth, (int) chordImage.getHeight(), chordReader, (x - currentBeatXGap), 0);
        				currentChordDrawn = true;
        				log.debug("chord image {} copied to x={} with width={}, bar width {}", chordImage, x,  currentCopyWidth, viewPortWidth);
        			}
	        	}
	        	
	        	if (isBeatX) {
        			lastBeatX = x;
    			}
        	}        	 

        }

        graphicsContext.drawImage(writableImage, 0, 0, viewPortWidth, height, 0, 0, viewPortWidth, height);		
        lastPixelPosition += deltaX;
	}
	
	private void calcBeatBorders() {
		beatParser.setMouseX(mouseXProperty.doubleValue());		
		mouseBeatLeftMilliProperty.set(beatParser.getMouseBeatStartMilli());
		mouseBeatRightMilliProperty.set(beatParser.getMouseBeatEndMilli());
		mouseBeatLeftXProperty.set(beatParser.getMouseBeatStartX());
		mouseBeatRightXProperty.set(beatParser.getMouseBeatEndX());
	}
	
	public DoubleProperty hoverMillisecondProperty() {
		return hoverMillisecondProperty;
	}

	public DoubleProperty mouseXProperty() {
		return mouseXProperty;
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

	public boolean isChordImageCachingOn() {
		return isChordImageCachingOn;
	}

	public void setChordImageCachingOn(boolean isChordImageCachingOn) {
		this.isChordImageCachingOn = isChordImageCachingOn;
	}	

}
