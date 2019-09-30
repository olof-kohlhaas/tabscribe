package kim.kohlhaas.sone.javafx.control;

import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.PixelReader;
import java.nio.ByteBuffer;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import kim.kohlhaas.javafx.color.ColorRange;
import kim.kohlhaas.javafx.color.HeatColorRange;
import kim.kohlhaas.javafx.css.StyleParser;
import kim.kohlhaas.sone.analyze.FloatSpectrogram;
import kim.kohlhaas.sone.javafx.Environment;
import kim.kohlhaas.sone.util.MathUtils;

public class SpectrogramChannel extends Pane {
    
    final static Logger log = LoggerFactory.getLogger(SpectrogramChannel.class);
    
    private FloatSpectrogram spectrogram;
    private Canvas canvas;    
    private double canvasXScale = 1.0;
    private double canvasYScale = 1.0;
    private double maxWidth = Environment.getMaxSingleScreenWidth();
    private double zoomXScale = 1.0; 
    private double millisecondPosition = 0.0;    
    private WritableImage writableImage;
    private PixelWriter pixelWriter;
    private PixelReader pixelReader;
    private byte[] copyArray; 
    private WritablePixelFormat<ByteBuffer> bufferFormat;
    private int gridIndex;
    private double lastMillisecondPosition;
    private int pixelPosition;
    private int pixelLength;
    private GraphicsContext graphicsContext;
    private Scale scale;
    private double spectroWidth;    
    private static final int BYTES_PER_PIXEL = 4;
    private final DoubleProperty maxScrollProperty;
    private ReadOnlyDoubleWrapper maxScrollReadWrapper;
    private final DoubleProperty visibleAmountProperty;
    private ReadOnlyDoubleWrapper visibleAmountReadWrapper;
    private final DoubleProperty verticalScrollPositionProperty;
    private double lastScrollPosition = 0.0;    
    private ColorRange colorRange;
    double rootMeanSquare = 0;
    private Color backgroundColor;    
    private boolean isDownScaled = false;
    private boolean isSkipped = false;
    
    public SpectrogramChannel() {
        canvas = new Canvas();
        this.getChildren().add(canvas);
        
        graphicsContext = canvas.getGraphicsContext2D();
        
        maxScrollProperty = new SimpleDoubleProperty();
        maxScrollReadWrapper = new ReadOnlyDoubleWrapper(this, "maxScrollProperty", 0.0);
        maxScrollReadWrapper.bind(maxScrollProperty);
        
        visibleAmountProperty = new SimpleDoubleProperty();
        visibleAmountReadWrapper = new ReadOnlyDoubleWrapper(this, "visibleAmountProperty", 0.0);
        visibleAmountReadWrapper.bind(visibleAmountProperty);
        
        verticalScrollPositionProperty = new SimpleDoubleProperty(this, "verticalScrollPositionProperty", 0.0);
        
        scale = new Scale();
        scale.setX(canvasXScale);
        scale.setY(canvasYScale);
        scale.setPivotX(0);
        scale.setPivotY(0);
        
        canvas.getTransforms().clear();
        canvas.getTransforms().add(scale);
        
        colorRange = new HeatColorRange();
        
        sceneProperty().addListener((observable, oldValue, newValue) -> onSceneChanged());      

        widthProperty().addListener((observable, oldValue, newValue) -> onWidthChanged());
        heightProperty().addListener((observable, oldValue, newValue) -> onHeightChanged());
        
        verticalScrollPositionProperty.addListener((observable, oldValue, newValue) -> onVerticalScrollChanged());
        
        
    }
    
    public final ReadOnlyDoubleProperty maxScroll() {
        return maxScrollReadWrapper.getReadOnlyProperty();
    }
    
    public final ReadOnlyDoubleProperty visibleAmount() {
        return visibleAmountReadWrapper.getReadOnlyProperty();
    }
    
    public DoubleProperty verticalScrollPosition() {
        return verticalScrollPositionProperty;
    }
    
    private void onVerticalScrollChanged() {
        if (verticalScrollPositionProperty.get() < 0.0) {
            verticalScrollPositionProperty.set(0.0);
        } else if (verticalScrollPositionProperty.get() > maxScrollProperty.get()) {
            verticalScrollPositionProperty.set(maxScrollProperty.get());
        }
        
        if (lastScrollPosition != verticalScrollPositionProperty.get()) {
            lastScrollPosition = verticalScrollPositionProperty.get();
            draw(true);
        }
    }
    
    private void onSceneChanged() {
        try {
            backgroundColor = Color.web(StyleParser.getStyleRuleValueAsString(this, "SpectrogramChannel", "-fx-background"));
        } catch (MalformedURLException e) {
            log.warn(e.toString());
        } 
        
        colorRange.setBackgroundColor(backgroundColor);
        
        draw(true);
    }
    
    public void setColorRange(ColorRange colorRange) {
        this.colorRange = colorRange;
        this.colorRange.setBackgroundColor(backgroundColor);
        draw(true);
    }
    
    public void resetDimensions() {
        double width = getWidth();
        double height = getHeight();
        double diffHeight;
        
        if (spectrogram != null ) {
            diffHeight = spectrogram.getFreqs() - height;
            if (diffHeight < 0) {
                canvasYScale = height / spectrogram.getFreqs();
                visibleAmountProperty.set(0.0);
                maxScrollProperty.set(0.0);
                verticalScrollPositionProperty.set(0.0);
            } else {
                canvasYScale = 1.0;
                visibleAmountProperty.set(height * diffHeight / spectrogram.getFreqs());
                maxScrollProperty.set(diffHeight);
                if (Math.abs(verticalScrollPositionProperty.get()) > diffHeight) {
                    verticalScrollPositionProperty.set(diffHeight); 
                }
            }  
        }

        scale.setY(canvasYScale);
        

        spectroWidth = width;        
        canvasXScale = 1.0;
        
        
        if (canvasXScale <= zoomXScale) {
        	canvasXScale = zoomXScale;
        	spectroWidth = width / zoomXScale;
        	isDownScaled = false;
        } else if (canvasXScale > zoomXScale) {
        	spectroWidth = width;
        	isDownScaled = true;
        } 
                
        scale.setX(canvasXScale);
        
        draw(true);
    }
    
    private void onWidthChanged() {
    	adjustBuffers();
        resetDimensions();
    }
    
    private void onHeightChanged() {
        resetDimensions();
    }
    
    private void draw(boolean sweep) {
        if (spectrogram == null) {
            return;
        }
        
        int scrollY = (int) verticalScrollPositionProperty.get();
        double width = getWidth();
        double height = getHeight();        
        double millisecondPosition = getMillisecondPosition();
        int deltaX;
        int startX = 0;
        int power;
        float maxAmp = spectrogram.getMaxAmp();
              
        if (spectroWidth > 0 && height > 0) {
            if (sweep) {
                canvas.setWidth(width);
                canvas.setHeight(spectrogram.getFreqs());
                if (isDownScaled) {
                	pixelLength = (int) Math.ceil(spectrogram.getTimeSteps() * zoomXScale);
                    pixelPosition = (int) Math.ceil(pixelLength * (millisecondPosition / spectrogram.getMilliseconds()));
                } else {                    
                    lastMillisecondPosition = millisecondPosition;
                }

                isSkipped = false;
            } else {           	
            	if (isDownScaled) {
            		deltaX = ((int) Math.ceil(pixelLength * (millisecondPosition / spectrogram.getMilliseconds()))) - pixelPosition;         		
            	} else {
            		deltaX = getGridIndex(0, millisecondPosition) - (getGridIndex(0, lastMillisecondPosition));
            	}

	            if (deltaX > 0 && deltaX < spectroWidth && !sweep) {           	
	                pixelReader.getPixels(deltaX, 0, (int) spectroWidth-deltaX, spectrogram.getFreqs(), bufferFormat, copyArray, 0,  ((int) spectroWidth - deltaX) * BYTES_PER_PIXEL);
	                pixelWriter.setPixels(0, 0, (int) spectroWidth - deltaX, spectrogram.getFreqs(), bufferFormat, copyArray, 0, ((int) spectroWidth - deltaX) * BYTES_PER_PIXEL);
	                startX = (int) spectroWidth - deltaX;
	                isSkipped = false;
	            } else if (deltaX <= 0 && !sweep) {
	            	isSkipped = true;
	            	//TODO interpolate skipped values
	            }
            }
            if (!isSkipped) {
	            if (maxAmp > 0.0) {
	                for (int x = startX; x < spectroWidth; x++) {
	                	gridIndex = getGridIndex(x, millisecondPosition);

	                    for (int y = 0; y < spectrogram.getFreqs() && y < height; y++) {
	                       if (gridIndex < 0 || gridIndex > spectrogram.getTimeSteps() - 1) {
	                           power = 0;
	                       } else {
	                    	   power = (int)((colorRange.getLength() - 1) * (MathUtils.lb(spectrogram.getAmp(gridIndex,spectrogram.getFreqs()-1-(y + scrollY)) + 1) / MathUtils.lb(maxAmp+1)));
	                       }
	                       pixelWriter.setColor(x, y, colorRange.getColor(power));
	                    } 
	                }
	            } else {
	                power = 0;
	                for (int x = startX; x < spectroWidth; x++) {
	                    for (int y = 0; y < spectrogram.getFreqs() && y < height; y++) {
	                       pixelWriter.setColor(x, y, colorRange.getColor(power));
	                    } 
	                }
	            }
	            
	            graphicsContext.drawImage(writableImage, 0, 0, spectroWidth, spectrogram.getFreqs(), 0, 0, spectroWidth, spectrogram.getFreqs());
	            
	            if (isDownScaled) {
	            	pixelPosition = (int) Math.ceil(pixelLength * (millisecondPosition / spectrogram.getMilliseconds()));
	            } else {
	            	lastMillisecondPosition = millisecondPosition;
	            }           
	            
            }

        }
        
        
    }
    
    public FloatSpectrogram getSpectrogram() {
        return spectrogram;
    }


    public void setSpectrogram(FloatSpectrogram spectrogram) {
        this.spectrogram = spectrogram;
        reset();
    }
    
    public void reset() {
        if (spectrogram != null) {
            adjustBuffers();            
            resetDimensions();
        }
    }
    
    private void adjustBuffers() {
    	if (spectrogram != null && (writableImage == null || getWidth() > maxWidth)) {
    		if (getWidth() > maxWidth) {
    			if (getWidth() > Environment.getMaxMulitScreenWidth()) {
    				maxWidth = getWidth();
    			} else {
    				maxWidth = Environment.getMaxMulitScreenWidth();
    			}
    		}
	    	writableImage = new WritableImage((int) maxWidth, spectrogram.getFreqs());
	    	pixelWriter = writableImage.getPixelWriter();
	        pixelReader = writableImage.getPixelReader();
	        bufferFormat = WritablePixelFormat.getByteBgraPreInstance();
	        copyArray = new byte[(int) maxWidth * spectrogram.getFreqs() * BYTES_PER_PIXEL];
    	}
    }

    public double getMillisecondPosition() {
        return millisecondPosition;
    }

    public void setMillisecondPosition(double millisecondPosition) {
    	if (millisecondPosition < this.millisecondPosition) {
            this.millisecondPosition = millisecondPosition;
            draw(true);
    	} else {
    		this.millisecondPosition = millisecondPosition;
        	draw(false);
    	}
    }

    @Override
    public boolean isResizable() {
        return true;
    }
    
    @Override
    public double computePrefWidth(double height) {
        return 256.0;
    }
    
    @Override
    public double computeMinWidth(double height) {
        return 256.0;
    }
    
    public double getXPosition(double milli) {
        return (milli / spectrogram.getMilliseconds()) * spectrogram.getTimeSteps();
    }
    
    public double getMilliPosition(int x) {
    	return ((double) x / spectrogram.getTimeSteps()) * spectrogram.getMilliseconds();
    }

    public double getCanvasXScale() {
        return canvasXScale;
    }
    
    public double getZoomXScale() {
        return zoomXScale;
    }

    public double getCanvasYScale() {
        return canvasYScale;
    }
    
    public void setZoomXScale(double minXScale) {
    	this.zoomXScale = minXScale;
    	resetDimensions();
    }
        
    private int getGridIndex(int canvasX, double millisecond) {
    	 if (isDownScaled) {
    		 return (int) Math.floor(((canvasX -  spectroWidth / 2) / zoomXScale) + getXPosition(millisecond));
    	 } else {
    		 return (int) Math.floor((canvasX - spectroWidth / 2) + getXPosition(millisecond));
    	 }
    }

    
}
