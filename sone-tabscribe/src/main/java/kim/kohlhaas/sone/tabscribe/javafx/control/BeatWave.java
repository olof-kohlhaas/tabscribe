package kim.kohlhaas.sone.tabscribe.javafx.control;

import java.nio.ByteBuffer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import kim.kohlhaas.sone.javafx.Environment;
import kim.kohlhaas.sone.signal.FloatAudioSignal;
import kim.kohlhaas.sone.signal.Waveform;
import kim.kohlhaas.sone.tabscribe.model.BeatParser;

public class BeatWave extends Region {
	
	private FloatAudioSignal signal;
	private Canvas canvas;
	private GraphicsContext graphicsContext;
	private double viewPortWidth = 0.0;
	private double height = 0.0;
    private PixelWriter pixelWriter;
    private PixelReader pixelReader;
    private WritablePixelFormat<ByteBuffer> bufferFormat;
    private WritableImage writableImage;
    private boolean isBeatX;
    private int lastBeatX;
    private Waveform waveform;
    private double maxVirtualWidth;
    private double currentMin;
    private double currentMax;
    private int waveX;
    private int xRange;
    private int samplesPerTimeStep;
    private double maxZoomScale;
    private float[] min;
    private float[] max;
    private Color backgroundColor;
    private double lastMillisecondPosition;
    private double millisecondWidth;
    private double duration;
    private byte[] copyArray;
    private double maxWidth = Environment.getMaxSingleScreenWidth();
    private static final int BYTES_PER_PIXEL = 4;
    private int lastPixelPosition;
    private Color lineColor = Color.WHITE;
    private Color darkLineColor = Color.web("#767c80");
    private BeatParser beatParser;
    private Color[] measureColors;
    
	public BeatWave(BeatParser beatParser, int samplesPerTimeStep, double maxZoomScale) {
		this.beatParser = beatParser;
		this.samplesPerTimeStep = samplesPerTimeStep;
		this.maxZoomScale = maxZoomScale;
		
		measureColors = new Color[beatParser.colors.length];
		for (int i = 0; i < beatParser.colors.length; i++) {
			if (beatParser.colors[i] == null) {
				measureColors[i] = null;
			} else {
				measureColors[i] = Color.web(beatParser.colors[i]);
			}
		}
		
		backgroundColor = Color.web("#3c3f41");
		bufferFormat = WritablePixelFormat.getByteBgraPreInstance();
		
		canvas = new Canvas();
        this.getChildren().add(canvas);
		graphicsContext = canvas.getGraphicsContext2D();
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
			millisecondWidth = newValue.doubleValue();
			draw(true);
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
	
	private int getWaveX(int canvasX) {
		double milliX = (canvasX * millisecondWidth) / viewPortWidth;
		return (int) Math.floor(((beatParser.getMilliLeft() + milliX) / duration) * maxVirtualWidth);
	}
	
	public void draw(boolean sweep) {
		int virtualX;
		int virtualLeftX;
		int deltaX = 0;
		int startX = 0;
		Color xColor;

       
				
		if(!sweep) {
			deltaX = beatParser.getPixelPosition() - lastPixelPosition;
			startX = (int) viewPortWidth - deltaX;
			
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
            pixelReader.getPixels(deltaX, 0, (int) viewPortWidth - deltaX, (int) height, bufferFormat, copyArray, 0,  ((int) viewPortWidth - deltaX) * BYTES_PER_PIXEL);
            pixelWriter.setPixels(0, 0, (int) viewPortWidth - deltaX, (int) height, bufferFormat, copyArray, 0, ((int) viewPortWidth - deltaX) * BYTES_PER_PIXEL);
            lastBeatX -= deltaX;
        }	
        
		virtualLeftX = beatParser.getPixelPosition() - (int) (viewPortWidth / 2.0);
        for (int x = 0; x < (int) viewPortWidth; x++) {
        	virtualX = virtualLeftX + x;
        	if (isBeatX = beatParser.isBeatLine(virtualX)) {

        	} else {
        		if (sweep || x >= startX) {
	        		currentMin = 0.0;
	        		currentMax = 0.0; 
	        		if (signal != null) {
		        		waveX = getWaveX(x);
		        		xRange = (int) Math.ceil(((millisecondWidth / viewPortWidth) / duration)  * maxVirtualWidth);
		        		for (int f = 0; f < xRange; f++) {
		        			if (f + waveX >= 0 && f + waveX < maxVirtualWidth) {
		        				if (currentMin > min[f + waveX]) {
		        					currentMin = min[f + waveX];
		        				}	    	        				
		        				if (currentMax < max[f + waveX]) {
		        					currentMax = max[f + waveX];
		        				}
		        			}
		        		}
		        		
	        		}
        		}
        	}

        	
        	if (sweep || x >= startX ) {
        		
        		xColor = measureColors[beatParser.getMeasureColor(virtualX)];
	        	for (int y = 0; y < (int) height; y++) {
	        		if ( y == 0) {
	        			if (xColor != null) {
	        				pixelWriter.setColor(x, y, lineColor);
	        			} else {
	        				pixelWriter.setColor(x, y, darkLineColor);
	        			}
	        		} else {
		        		if (isBeatX) {
		        			pixelWriter.setColor(x, y, Color.WHITE);
		        		} else {
		        			if (signal != null && xColor != null) {
		        				if ( y <= height / 2.0 - currentMin * (height / 2 - 2.0)   && y >= height / 2.0 - currentMax * (height / 2.0 - 2.0)) {		        					
	        						pixelWriter.setColor(x, y, Color.BLACK);
		        				}
		        				else {		        					
		        					pixelWriter.setColor(x, y,xColor);
		        				}
		        			} else {
		        				pixelWriter.setColor(x, y, backgroundColor);
		        			}
		        		}
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

	public FloatAudioSignal getSignal() {
		return signal;
	}

	public void setSignal(FloatAudioSignal signal) {
		this.signal = signal;
		duration = signal.getMillisecondDuration();
		waveform = new Waveform(signal);
		maxVirtualWidth = Math.ceil(((double) signal.getFrameLength() * maxZoomScale) / (double) samplesPerTimeStep);
		min = new float[(int) maxVirtualWidth];
		max = new float[(int) maxVirtualWidth];
		waveform.fitToWidth(maxVirtualWidth);
		
		for (int x = 0; x < min.length; x++) {
			min[x] = waveform.getMin(x);
			max[x] = waveform.getMax(x);
		}
		
		resetDimensions();
	}

	public BeatParser getBeatParser() {
		return beatParser;
	}
	
}
