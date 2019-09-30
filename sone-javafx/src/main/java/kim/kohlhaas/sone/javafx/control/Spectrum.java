package kim.kohlhaas.sone.javafx.control;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import kim.kohlhaas.javafx.css.StyleParser;
import kim.kohlhaas.sone.analyze.FloatSpectrogram;
import kim.kohlhaas.sone.harmony.Tone;
import kim.kohlhaas.sone.javafx.event.HoverEvent;
import kim.kohlhaas.sone.util.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;

public class Spectrum extends Pane {
    
	final static Logger log = LoggerFactory.getLogger(Spectrum.class);
	
    private ArrayList<Rectangle> bars = new ArrayList<Rectangle>();
    private ArrayList<Rectangle> drops = new ArrayList<Rectangle>();
    private float[] spectrum = new float[0];
    private float[] velocity = new float[0];
    private Color strokePeakColor;
    private Color strokeBaseColor;
    private double lastWidth = 0.0;
    private int toneResolution = 1;
    private Line frequencyLine;
    private double barWidth = 1.0;
    private boolean hasHoverExited = true;
    private FloatSpectrogram spectrogram;
    private int channel = 0;
    private float[] spectrumData;
    private double millisecond = 0.0;
    private boolean isHoverSyncOn = true;
    private boolean normalizeToAbsMax = false;

    public final DoubleProperty millisecondProperty;
    public final IntegerProperty hoverIndexProperty;
    
    public Spectrum() {
        this(0);
    }
    
    public Spectrum(int channel) {
        this.channel = channel;
        
        sceneProperty().addListener((observable, oldValue, newValue) -> onSceneChanged());  
        widthProperty().addListener((observable, oldValue, newValue) -> {
            initBars(spectrum.length);
            plot();
        });
        heightProperty().addListener((observable, oldValue, newValue) ->  plot());
        
        this.addEventHandler(MouseEvent.MOUSE_MOVED, e -> setMousePointer(e.getSceneX(), e.getSceneY()));
        this.addEventHandler(MouseEvent.MOUSE_EXITED, e -> hideHover());
        
        frequencyLine  = new Line(1, 0, 1, 100);
        frequencyLine.setVisible(false);
        frequencyLine.setStrokeLineCap(StrokeLineCap.BUTT);
        frequencyLine.getStyleClass().add("frequency-line");
        this.getChildren().add(frequencyLine);
        
        millisecondProperty = new SimpleDoubleProperty();
        millisecondProperty.addListener((observable, oldValue, newValue) -> {
        	try {
        		refresh();
        	} catch (ArrayIndexOutOfBoundsException exception) {
        		log.warn("spectrogram probably accessed while it is loading: {}", exception.getMessage());
        		log.error("Excption: {}", exception);
        	}
        });
        
        hoverIndexProperty = new SimpleIntegerProperty();
        hoverIndexProperty.addListener((observable, oldValue, newValue) -> hoverToneElsewhere());
    }
    
    public boolean isHoverSyncOn() {
        return isHoverSyncOn;
    }

    public void setHoverSyncOn(boolean hoverSyncOn) {
        this.isHoverSyncOn = hoverSyncOn;
    }

    private void hoverToneElsewhere() {
        if (!isHoverSyncOn) {
            return;
        }
        
        int index = hoverIndexProperty.get();
        double x;
        double xSnap; 
        if (hasHoverExited) {
            if (index != -1) {
                xSnap = Math.floor(index / toneResolution) * (toneResolution * barWidth) + (toneResolution * barWidth) / 2.0;
                
                frequencyLine.setStartX(xSnap);
                frequencyLine.setEndX(xSnap);
                frequencyLine.setStartY(0);
                frequencyLine.setEndY(getHeight());
                frequencyLine.setVisible(true);
            } else {
                frequencyLine.setVisible(false);
            }
        }
    }
    
    public void refresh() {
        millisecond = millisecondProperty.get(); 
        
        if (spectrogram != null) {
            spectrogram.copySpectrum((channel < spectrogram.getChannels()) ? channel : 0, 
                    (int)(spectrogram.getTimeSteps() * millisecond/ spectrogram.getMilliseconds()),
                    spectrumData);
            setSpectrum(spectrumData);
        }
    }
    
    private void setMousePointer(double sceneX, double sceneY) {
        if (bars.size() > 0) {
            double localX = sceneX - this.localToScene(this.getBoundsInLocal()).getMinX();
            double localY = sceneY - this.localToScene(this.getBoundsInLocal()).getMinY();
            int freqIndex = (int) Math.floor(localX /  barWidth); 
            double xSnap = Math.floor(freqIndex / toneResolution) * (toneResolution * barWidth) + (toneResolution * barWidth) / 2.0;

            frequencyLine.setStartX(xSnap);
            frequencyLine.setEndX(xSnap);
            frequencyLine.setStartY(0);
            frequencyLine.setEndY(getHeight());
            frequencyLine.setVisible(true);
            
            if (hasHoverExited) {
                hasHoverExited = false;
                this.fireEvent(new HoverEvent(HoverEvent.HOVER_ENTERED, xSnap, localY));
            } else {
                this.fireEvent(new HoverEvent(HoverEvent.HOVER_MOVED, xSnap, localY));
            }
            
            hoverIndexProperty.set(freqIndex);
        }
    }
    
    private void hideHover() {
        frequencyLine.setVisible(false);
        hasHoverExited = true;
        hoverIndexProperty.set(-1); // TODO find another way to hide the bar
        this.fireEvent(new HoverEvent(HoverEvent.HOVER_EXITED));
    }
    
    private void onSceneChanged() {
        String strokePeakColorString = null;
        String strokeBaseColorString = null;
		try {
			strokePeakColorString = StyleParser.getStyleRuleValueAsString(this, "Spectrum", "-custom-stroke-peak");
			strokeBaseColorString = StyleParser.getStyleRuleValueAsString(this, "Spectrum", "-custom-stroke-base");
		} catch (MalformedURLException e) {
			log.warn(e.toString());
		}
        
        if (strokePeakColorString == null) {
            strokePeakColorString = "#fffff0";
        } 
        if (strokeBaseColorString == null) {
            strokeBaseColorString = "#000000";
        } 
        strokePeakColor = Color.web(strokePeakColorString); 
        strokeBaseColor = Color.web(strokeBaseColorString); 
    }
    
    @Override
    public boolean isResizable() {
        return true;
    }
    
    @Override
    public double computeMinWidth(double height) {
        return 100.0;
    }
    
    @Override
    public double computeMinHeight(double width) {
        return 50.0; 
    }
    
    public void setSpectrum(float[] spectrum) {
        this.spectrum = spectrum;
        initBars(spectrum.length);
        plot();
    }
    
    private void plot() {
        double h;
        double scale;
        float max = 1.0f;
        double normalizeFactor = 1.0;
        double height = getHeight() - 10.0;
        double rootMeanSquare = 0;
        
        if (spectrum != null) {
        	if (spectrogram != null && normalizeToAbsMax) {
        		max = spectrogram.getMaxAmp();
        	} else {
        		max = ArrayUtils.getMax(spectrum);
        	}
           
        	normalizeFactor = 1.0 / max;
            
                        
            for (int i = 0; i < spectrum.length; i++) {
            	            	
                scale = spectrum[i] * normalizeFactor;
                h = Math.round(height * scale);                
                bars.get(i).setHeight(h);
                bars.get(i).setY(getHeight()-h);
                bars.get(i).setFill(Color.rgb(
                        (int) (255.0*(strokeBaseColor.getRed() + ((strokePeakColor.getRed() - strokeBaseColor.getRed()) * scale))),
                        (int) (255.0*(strokeBaseColor.getGreen() + ((strokePeakColor.getGreen() - strokeBaseColor.getGreen()) * scale))), 
                        (int) (255.0*(strokeBaseColor.getBlue() + ((strokePeakColor.getBlue() - strokeBaseColor.getBlue()) * scale)))
                ));
                
                
                if (drops.get(i).getY() > bars.get(i).getY() - 2) {
                    
                    drops.get(i).setY(bars.get(i).getY() - 2);
                    velocity[i] = 0.0f;
                } else {
                    scale = (float) ((getHeight() - drops.get(i).getY()) / getHeight());
                    velocity[i] += 0.1f;
                    drops.get(i).setHeight(1);
                    drops.get(i).setY(drops.get(i).getY() + velocity[i]);
                    drops.get(i).setFill(Color.rgb(
                            (int) (255.0*(strokeBaseColor.getRed() + ((strokePeakColor.getRed() - strokeBaseColor.getRed()) * scale))),
                            (int) (255.0*(strokeBaseColor.getGreen() + ((strokePeakColor.getGreen() - strokeBaseColor.getGreen()) * scale))), 
                            (int) (255.0*(strokeBaseColor.getBlue() + ((strokePeakColor.getBlue() - strokeBaseColor.getBlue()) * scale)))
                    ));
                }
                
                if (bars.get(i).getHeight() - drops.get(i).getHeight() <= 1) {
                    drops.get(i).setHeight(0);
                }
            }
        }
    }
    
    private void initBars(int count) {  
        if (count != bars.size() || lastWidth != getWidth()) {
            lastWidth = getWidth();
            Rectangle rectangle;
            velocity = new float[count];
            Arrays.fill(velocity, 0.0f);
            bars.clear();
            this.getChildren().clear();
            barWidth = getWidth() / count;
            frequencyLine.setStrokeWidth(toneResolution * barWidth);
            
            for (int i = 0; i < count; i++) {
                rectangle = new Rectangle(i * barWidth, getHeight(), barWidth, 0);
                this.getChildren().add(rectangle);
                bars.add(i, rectangle);
            }
            
            for (int i = 0; i < count; i++) {
                rectangle = new Rectangle(i * barWidth, getHeight() - 2, barWidth, 2);
                this.getChildren().add(rectangle);
                drops.add(i, rectangle);
                drops.get(i).setFill(Color.rgb(155 + (int) 100, 155, 155));
            }
            
            this.getChildren().add(frequencyLine);
        }
    }

    public int getToneResolution() {
        return toneResolution;
    }

    public void setToneResolution(int toneResolution) {
        this.toneResolution = toneResolution;
        frequencyLine.setStrokeWidth(toneResolution * barWidth);
    }

    public FloatSpectrogram getSpectrogram() {
        return spectrogram;
    }

    public void setSpectrogram(FloatSpectrogram spectrogram) {
        this.spectrogram = spectrogram;
        spectrumData = new float[spectrogram.getFreqs()];
        refresh();
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

	public boolean isNormalizeToAbsMax() {
		return normalizeToAbsMax;
	}

	public void setNormalizeToAbsMax(boolean normalizeToAbsMax) {
		this.normalizeToAbsMax = normalizeToAbsMax;
		plot();
	}
        
}
