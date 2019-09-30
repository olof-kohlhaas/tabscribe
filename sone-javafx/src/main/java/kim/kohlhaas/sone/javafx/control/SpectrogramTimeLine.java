package kim.kohlhaas.sone.javafx.control;

import java.util.ArrayList;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import kim.kohlhaas.sone.analyze.FloatSpectrogram;
import kim.kohlhaas.sone.javafx.RuntimeSettings;
import kim.kohlhaas.sone.javafx.Settings;

public class SpectrogramTimeLine extends Pane {
    
    private FloatSpectrogram spectrogram;
    private Settings settings = RuntimeSettings.getInstance();
    private double millisecondPosition = 0.0;
    private double millisecondLeftBorder = 0.0;
    private final static double INIT_MILLI_BLOCK = 250.0;
    private final static double MIN_GAP = 50.0;
    private final static double MAX_GAP = 100.0;
    private double pixelBlockMilli;
    private double pixelBlockSec;
    private double scalaGap;
    private double benchmark;

    
    private double factorXScale = 1.0;
    private double factorYScale = 1.0;
    
    private ArrayList<Line> lines = new ArrayList<Line>(); 
    private ArrayList<Label> label = new ArrayList<Label>();
    
    private final DoubleProperty loopStartingProperty = new SimpleDoubleProperty(0.0);
    private final DoubleProperty loopTerminalProperty = new SimpleDoubleProperty(0.0);
    
    private Text loopStartingIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.SHARE, "15px");
    private Text loopTerminalIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.REPLY, "15px");
    
    private final DoubleProperty loopStartingXProperty = new SimpleDoubleProperty();
    private ReadOnlyDoubleWrapper loopStartingXReadWrapper = new ReadOnlyDoubleWrapper(this, "loopStartingXProperty", 0.0);
    private final DoubleProperty loopTerminalXProperty = new SimpleDoubleProperty();
    private ReadOnlyDoubleWrapper loopTerminalXReadWrapper = new ReadOnlyDoubleWrapper(this, "loopTerminalXProperty", 0.0);
    
    private final DoubleProperty millisecondWidthProperty = new SimpleDoubleProperty();
    private ReadOnlyDoubleWrapper millisecondWidthReadWrapper = new ReadOnlyDoubleWrapper(this, "millisecondWidthProperty", 0.0);
    
    public SpectrogramTimeLine() {
        widthProperty().addListener((observable, oldValue, newValue) -> reset());
        
        this.getChildren().addAll(loopStartingIcon, loopTerminalIcon);
        loopStartingIcon.setY(20.0);
        loopStartingIcon.setScaleY(-1.0);
        loopStartingIcon.setVisible(false);
        loopTerminalIcon.setY(15.0);
        loopTerminalIcon.setVisible(false);        
        
        loopStartingXReadWrapper.bind(loopStartingXProperty);
        loopTerminalXReadWrapper.bind(loopTerminalXProperty);
        
        millisecondWidthReadWrapper.bind(millisecondWidthProperty);
                
        loopStartingProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() > 0.0) {                
                reset();
            } else {
                loopStartingIcon.setVisible(false);
            }
        });
        
        loopTerminalProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() > 0.0) {
                reset();
            } else {
                loopTerminalIcon.setVisible(false);
            }
        });
    }
    
    @Override
    public boolean isResizable() {
        return true;
    }
    
    @Override
    public double computePrefHeight(double width) {
        return 25.0;
    }
    
    @Override
    public double computeMinHeight(double width) {
        return 25.0;
    }
    
    public void setMillisecondPosition(double milli) {
        this.millisecondPosition = milli;
        reset();
    }
    
    public void setSpectrogram(FloatSpectrogram spectrogram) {
        this.spectrogram = spectrogram;
        calcGap();
        reset();
    }        
    
    public void reset() {
        double pastSteps;
        double zeroXPosition;
        double firstX;
        
        int pastStepsFloor;
        int lineCount;
        Line tmpLine;
        Label tmpLabel;
        double lineX;
        
        if (spectrogram != null) {
        	            
            lineCount = (int) Math.floor((getWidth()/ factorXScale) / benchmark) + 2;
            if (lineCount > lines.size()) {
                for (int i = 0; i < lineCount - lines.size(); i++) {
                    tmpLine = new Line(i, 15, i, 25);
                    tmpLabel = new Label();
                    lines.add(tmpLine);
                    label.add(tmpLabel);
                    this.getChildren().add(tmpLine);
                    this.getChildren().add(tmpLabel);
                }
            }
            
            millisecondWidthProperty.set(spectrogram.getMilliseconds(getWidth() / factorXScale));
            // Wieviele Millisekunden sind vor dem sichtbaren Bereich bereits vergangen
            millisecondLeftBorder = millisecondPosition - millisecondWidthProperty.get() / 2.0;
            
            if (loopStartingProperty.get() > 0.0) {
                loopStartingXProperty.set(spectrogram.getTimeStep(loopStartingProperty.get() - millisecondLeftBorder) * factorXScale);
                if ((loopStartingXProperty.get() + scalaGap - 13) > 0.0 && (loopStartingXProperty.get() < getWidth() + scalaGap)) {
                    loopStartingIcon.setX(loopStartingXProperty.get() - 13);
                    loopStartingIcon.setVisible(true);
                    loopStartingIcon.toFront();
                } else {
                    loopStartingIcon.setVisible(false);
                }
            }
            
            if (loopTerminalProperty.get() > 0.0) {
                loopTerminalXProperty.set(spectrogram.getTimeStep(loopTerminalProperty.get() - millisecondLeftBorder) * factorXScale);
                if ((loopTerminalXProperty.get() + scalaGap) > 0.0 && (loopTerminalXProperty.get() < getWidth() + scalaGap)) {
                    loopTerminalIcon.setX(loopTerminalXProperty.get());
                    loopTerminalIcon.setVisible(true);
                    loopTerminalIcon.toFront();
                } else {
                    loopTerminalIcon.setVisible(false);
                }
            }
            
            // Wieviele Skaleneinheiten sind vor dem sichtbaren Bereich bereits vergangen
            pastSteps = millisecondLeftBorder / (pixelBlockSec * 1000.0);
            
            // Wieviele vollständige Skaleneinheiten sind vor dem sichtbaren Bereich bereits vergangen
            pastStepsFloor = (int) Math.floor(pastSteps);
            
            zeroXPosition = spectrogram.getTimeStep(0.0 - millisecondPosition) * factorXScale + Math.floor(getWidth() / 2);
            
            if (zeroXPosition > 0) {
                firstX = zeroXPosition;
            } else {
                firstX = spectrogram.getTimeStep((pastStepsFloor - pastSteps) * pixelBlockSec * 1000.0)* factorXScale;
            }   
            
            for (int i = 0; i < lines.size(); i++) {
                lineX = firstX + i*scalaGap* factorXScale;
                tmpLine = lines.get(i);
                tmpLabel = label.get(i);
                if (zeroXPosition > 0) {
                    tmpLabel.setText(Math.round(i * pixelBlockMilli) / 1000.0 + "s");
                } else {
                    tmpLabel.setText(Math.round((pastStepsFloor + i) * pixelBlockMilli) / 1000.0 + "s");
                }
                if (lineX >= getWidth() + scalaGap) {
                    tmpLine.setVisible(false);
                    tmpLabel.setVisible(false);
                } else {
                    tmpLine.setVisible(true);
                    tmpLabel.setVisible(true);
                }
                tmpLine.setStartX(lineX);
                tmpLine.setEndX(lineX);
                tmpLabel.setTranslateX(lineX - tmpLabel.getLayoutBounds().getWidth() / 2);
            }
        }
    }
    
    public double getFactorXScale() {
        return factorXScale;
    }

    public void setFactorXScale(double factorXScale) {
        this.factorXScale = factorXScale;
        calcGap();
        reset();
    }

    public double getFactorYScale() {
        return factorYScale;
    }

    public void setFactorYScale(double factorYScale) {
        this.factorYScale = factorYScale;
    }
    
    public DoubleProperty loopStartingProperty() {
        return loopStartingProperty;
    }

    public DoubleProperty loopTerminalProperty() {
        return loopTerminalProperty;
    }
    
    public final ReadOnlyDoubleProperty loopStartingX() {
        return loopStartingXReadWrapper.getReadOnlyProperty();
    }
    
    public final ReadOnlyDoubleProperty loopTerminalX() {
        return loopTerminalXReadWrapper.getReadOnlyProperty();
    }
    
    public final ReadOnlyDoubleProperty millisecondWidthProperty() {
    	return millisecondWidthReadWrapper.getReadOnlyProperty();
    }
    
    private void calcGap() {
    	if (spectrogram != null) {
        	
            pixelBlockMilli = INIT_MILLI_BLOCK;
            scalaGap = 0;
           
        	do {        		
            	benchmark = (pixelBlockMilli * spectrogram.getTimeSteps()) / spectrogram.getMilliseconds();
            	pixelBlockSec = Math.round(pixelBlockMilli) / 1000.0;
            	scalaGap = (spectrogram.getTimeSteps() * pixelBlockSec * 1000.0) / spectrogram.getMilliseconds();
            	
            	if (scalaGap * factorXScale < MIN_GAP ) {
            		pixelBlockMilli *= (Math.floor(MIN_GAP / (scalaGap * factorXScale)) + 1);
            	}

            } while (scalaGap * factorXScale < MIN_GAP);

        	while (scalaGap * factorXScale > MAX_GAP)  {
        		if (scalaGap * factorXScale > MAX_GAP) {
            		pixelBlockMilli /= Math.pow(2.0, (Math.floor((scalaGap * factorXScale / MAX_GAP))));
            	}
        		
            	benchmark = (pixelBlockMilli * spectrogram.getTimeSteps()) / spectrogram.getMilliseconds();
            	pixelBlockSec = Math.round(pixelBlockMilli) / 1000.0;
            	scalaGap = (spectrogram.getTimeSteps() * pixelBlockSec * 1000.0) / spectrogram.getMilliseconds();
            }
    	}
    }
    
}
