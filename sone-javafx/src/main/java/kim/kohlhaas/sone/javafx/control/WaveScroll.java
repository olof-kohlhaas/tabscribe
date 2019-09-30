package kim.kohlhaas.sone.javafx.control;

import java.net.MalformedURLException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import kim.kohlhaas.javafx.css.StyleParser;
import kim.kohlhaas.sone.javafx.Environment;
import kim.kohlhaas.sone.javafx.event.SeekEvent;
import kim.kohlhaas.sone.signal.FloatAudioSignal;
import kim.kohlhaas.sone.signal.Waveform;
import kim.kohlhaas.sone.util.PCMUtils;
import kim.kohlhaas.sone.util.TimeUtils;

public class WaveScroll extends Pane {

    final static Logger log = LoggerFactory.getLogger(WaveScroll.class);
    private float[] maxSamples;
    private float[] minSamples;
    private float[] rmsPlus;
    private float[] rmsMinus;
    private int maxSampleWidth;
    private int positionPixel;
    private int lastDrawnPosition;    
    private Tooltip timeTip;
    
    private float[] samplesMaxPlus;
    private float[] samplesMinMinus;
    private float[] rootMeanSquarePlus;
    private float[] rootMeanSquareMinus;
    private Waveform waveform;
    private Canvas canvas;
    private Color strokePastPeakColor;
    private Color strokeBasePeakColor;
    private Color strokePastRmsColor;
    private Color strokeBaseRmsColor;
    private Color strokePastBackColor;
    private Color strokeBaseBackColor;
    
    private final DoubleProperty loopStartingProperty = new SimpleDoubleProperty(0.0);
    private final DoubleProperty loopTerminalProperty = new SimpleDoubleProperty(0.0);
    
    private Text loopStartingIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.SHARE, "20px");
    private Text loopTerminalIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.REPLY, "20px");
    
    public DoubleProperty millisecondProperty;
        
    public WaveScroll() {
        this.maxSampleWidth = Environment.getMaxMulitScreenWidth();
        sceneProperty().addListener((observable, oldValue, newValue) -> onSceneChanged());
        canvas = new Canvas();
        
        this.getChildren().addAll(canvas, loopStartingIcon, loopTerminalIcon);
        maxSamples = new float[]{};
        minSamples = new float[]{};
        rmsPlus = new float[]{};
        rmsMinus = new float[]{};
        millisecondProperty = new SimpleDoubleProperty(0.0);
        millisecondProperty.addListener((observable, oldValue, newValue) -> refresh(false));
        setPrefHeight(50.0);
        loopStartingIcon.setY(45.0);
        loopStartingIcon.setScaleY(-1.0);
        loopStartingIcon.setVisible(false);
        loopTerminalIcon.setY(20.0);
        loopTerminalIcon.setVisible(false);
        
        positionPixel = -1;
        lastDrawnPosition = -1;
        timeTip = new Tooltip();
        Tooltip.install(canvas, timeTip);
        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, e -> onMouseMoved(e));
        canvas.addEventHandler(MouseEvent.MOUSE_EXITED, e -> timeTip.hide());
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> onMouseReleased(e));
        
        canvas.widthProperty().bind(this.widthProperty());
        canvas.heightProperty().bind(this.heightProperty());
        canvas.widthProperty().addListener((observable, oldValue, newValue) -> refresh(true));
        
        loopStartingProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() > 0.0) {
                loopStartingIcon.setX(millisecondToX(loopStartingProperty.get()) - 20.0);
                loopStartingIcon.setVisible(true);
            } else {
                loopStartingIcon.setVisible(false);
            }
        });
        
        loopTerminalProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() > 0.0) {
                loopTerminalIcon.setX(millisecondToX(loopTerminalProperty.get()));
                loopTerminalIcon.setVisible(true);
            } else {
                loopTerminalIcon.setVisible(false);
            }
        });
    }
    
    private void onMouseMoved(MouseEvent e) {
        double hoverMilli = xToMillisecond(e.getSceneX());
        String position = TimeUtils.getFormattedString(hoverMilli);
        timeTip.setText(position);
        timeTip.show(((Node) e.getSource()), 
                canvas.localToScreen(canvas.getBoundsInLocal()).getMinX() + e.getSceneX(), 
                canvas.localToScreen(canvas.getBoundsInLocal()).getMinY() - timeTip.getHeight());
    }
    
    private void onMouseReleased(MouseEvent e) {
        MouseEvent event = (MouseEvent) e;
        double x = event.getX();
        double y = event.getY();
        
        
        if (y >= 0 && y < canvas.getHeight()) {
            if (x < 0) {
                x = 0;
            } else if (x > canvas.getWidth()) {
                x = canvas.getWidth();
            }
            
            canvas.fireEvent(new SeekEvent(SeekEvent.SEEK, xToMillisecond(x)));   
        }
    }
    
    private void onSceneChanged() {
        String strokePastPeakColorString;
        String strokeBasePeakColorString;
        String strokePastRmsColorString;
        String strokeBaseRmsColorString;
        String strokePastBackColorString;
        String strokeBaseBackColorString;
		try {
			strokePastPeakColorString = StyleParser.getStyleRuleValueAsString(this, "WaveScroll", "-custom-past-peak");
			strokeBasePeakColorString = StyleParser.getStyleRuleValueAsString(this, "WaveScroll", "-custom-base-peak");
	        strokePastRmsColorString = StyleParser.getStyleRuleValueAsString(this, "WaveScroll", "-custom-past-rms");
	        strokeBaseRmsColorString = StyleParser.getStyleRuleValueAsString(this, "WaveScroll", "-custom-base-rms");
	        strokePastBackColorString = StyleParser.getStyleRuleValueAsString(this, "WaveScroll", "-custom-past-back");
	        strokeBaseBackColorString = StyleParser.getStyleRuleValueAsString(this, "WaveScroll", "-custom-base-back");
	        strokePastPeakColor = Color.web(strokePastPeakColorString);
	        strokeBasePeakColor = Color.web(strokeBasePeakColorString);
	        strokePastRmsColor = Color.web(strokePastRmsColorString);
	        strokeBaseRmsColor = Color.web(strokeBaseRmsColorString);
	        strokePastBackColor= Color.web(strokePastBackColorString);
	        strokeBaseBackColor = Color.web(strokeBaseBackColorString);
		} catch (MalformedURLException e) {
			log.warn(e.toString());
		}
         
    }
    
    @Override
    public boolean isResizable() {
        return true;
    }
    
    @Override
    public double computeMinWidth(double height) {
        return 50.0;
    }
    
    @Override
    public double computeMinHeight(double width) {
        return 10.0; 
    }
    
    @Override
    public double computePrefWidth(double height) {
        return 300.0;
    }
    
    @Override
    public double computePrefHeight(double width) {
        return 50.0; 
    }
    
    public int getPositionPixel() {
        return positionPixel;
    }

    public synchronized void clear() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
                
        if (width > 0 && height > 0) {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0, 0, width, height);
        }
    }
    
    public synchronized boolean drawPosition() {
        double height = canvas.getHeight();
        
        int posWidth = positionPixel - lastDrawnPosition;
        
        WritableImage wi;   
                
        if (posWidth > 0 && height > 0) {
            GraphicsContext gc = canvas.getGraphicsContext2D();     
            
            wi = new WritableImage(posWidth, (int) height);
            PixelWriter pw = wi.getPixelWriter();        
            
            try {
                
                for (int x = 0; x < posWidth; x++) {
                    for(int y = 0; y < height; y++) {
                        pw.setColor(x, y, strokePastBackColor);
                    }
                }
                
                for (int x = 0; x < posWidth; x++) {
                    pw.setColor(x, (int) height / 2, strokePastPeakColor);
                }
                                
                for (int x = 0; x < posWidth; x++) {
                    for(int y = (int) height / 2; y > (int) height / 2 - maxSamples[positionPixel - posWidth + x + 1] * height / 2; y--) {
                        pw.setColor(x, y, strokePastPeakColor);
                    }
                }
                
                for (int x = 0; x < posWidth; x++) {
                    for(int y = (int) height / 2; y < (int) height / 2 - minSamples[positionPixel - posWidth + x + 1] * height / 2; y++) {
                        pw.setColor(x, y, strokePastPeakColor);
                    }
                }
                
                for (int x = 0; x < posWidth; x++) {
                    for(int y = (int) height / 2; y > (int) height / 2 - rmsPlus[positionPixel - posWidth + x + 1] * height / 2; y--) {
                       pw.setColor(x, y, strokePastRmsColor);
                    }
                }
                
                for (int x = 0; x < posWidth; x++) {
                    for(int y = (int) height / 2; y < (int) height / 2 - rmsMinus[positionPixel - posWidth + x + 1] * height / 2; y++) {
                       pw.setColor(x, y, strokePastRmsColor);
                    }
                }
                
                
            } catch (IndexOutOfBoundsException ioobe) {
                log.debug("DRAW - cancelled due to resize event");
                return false;
            }

            gc.drawImage(wi, positionPixel - posWidth + 1 , 0);
            lastDrawnPosition = positionPixel;
        }
    
        return true;
    }
    
    public synchronized boolean draw() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        WritableImage wi;
                
        if (width > 0 && height > 0) {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            
            wi = new WritableImage((int) width, (int) height);
            PixelWriter pw = wi.getPixelWriter();        
            
            try {
                
                for (int x = 0; x < width; x++) {
                    for(int y = 0; y < height; y++) {
                        if(x <= positionPixel) {
                            pw.setColor(x, y, strokePastBackColor);
                        } else {
                            pw.setColor(x, y, strokeBaseBackColor);
                        }
                    }
                }
                
                for (int x = 0; x < width; x++) {
                    if(x <= positionPixel) {
                        pw.setColor(x, (int) height / 2, strokePastPeakColor);
                    } else {
                        pw.setColor(x, (int) height / 2, strokeBasePeakColor);
                    }
                }
                                
                for (int x = 0; x < width; x++) {
                    for(int y = (int) height / 2; y > (int) height / 2 - maxSamples[x] * height / 2; y--) {
                        if(x <= positionPixel) {
                            pw.setColor(x, y, strokePastPeakColor);
                        } else {
                            pw.setColor(x, y, strokeBasePeakColor);
                        }
                    }
                }
                
                for (int x = 0; x < minSamples.length; x++) {
                    for(int y = (int) height / 2; y < (int) height / 2 - minSamples[x] * height / 2; y++) {
                        if(x <= positionPixel) {
                            pw.setColor(x, y, strokePastPeakColor);
                        } else {
                            pw.setColor(x, y, strokeBasePeakColor);
                        }
                    }
                }
                
                for (int x = 0; x < rmsPlus.length; x++) {
                    for(int y = (int) height / 2; y > (int) height / 2 - rmsPlus[x] * height / 2; y--) {
                        if(x <= positionPixel) {
                            pw.setColor(x, y, strokePastRmsColor);
                        } else {
                            pw.setColor(x, y, strokeBaseRmsColor);
                        }
                    }
                }
                
                for (int x = 0; x < rmsMinus.length; x++) {
                    for(int y = (int) height / 2; y < (int) height / 2 - rmsMinus[x] * height / 2; y++) {
                        if(x <= positionPixel) {
                            pw.setColor(x, y, strokePastRmsColor);
                        } else {
                            pw.setColor(x, y, strokeBaseRmsColor);
                        }
                    }
                }
                
                
            } catch (IndexOutOfBoundsException ioobe) {
                log.debug("DRAW - cancelled due to resize event");
                return false;
            }
            
            gc.drawImage(wi, 0, 0);
            lastDrawnPosition = positionPixel;
            
        }
    
        return true;
    }
    
    private void refresh(boolean sweep) { 
        
        if (waveform != null) {
            double position = millisecondProperty.get() * 1.00 / PCMUtils.getMilliseconds(waveform.getSignal());    
            int lastPositionPixel = positionPixel;
            positionPixel = (int) Math.round(canvas.getWidth() * position);
            
            if (sweep) {
                loopStartingIcon.setX(millisecondToX(loopStartingProperty.get()) - 20.0);
                loopTerminalIcon.setX(millisecondToX(loopTerminalProperty.get()));
                minSamples = getSamplesMinMinus(canvas.getWidth());
                maxSamples = getSamplesMaxPlus(canvas.getWidth());
                rmsMinus = getRootMeanSquareMinus(canvas.getWidth());
                rmsPlus = getRootMeanSquarePlus(canvas.getWidth());
                clear();
                draw();
            } else {
                if (positionPixel == lastPositionPixel + 1) {
                    drawPosition();
                } else if (positionPixel == lastPositionPixel){
                    // TODO Opazität der Position steigern ???
                } else {
                    draw();
                }
            }
        }    
    }
    
    private double xToMillisecond(double x) {
        if (waveform != null) {
            return x * PCMUtils.getMilliseconds(waveform.getSignal()) / canvas.getWidth();
        } else {
            return 0.0;
        }
    }
    
    private double millisecondToX(double millisecond) {
        if (waveform != null) {
            return millisecond * canvas.getWidth() / PCMUtils.getMilliseconds(waveform.getSignal());
        } else {
            return 0.0;
        }
    }

    public int getMaxSampleWidth() {
        return maxSampleWidth;
    }    


    
    public void setFloatAudioSignal(FloatAudioSignal floatAudioSignal) {
        waveform = new Waveform(floatAudioSignal);
        waveform.fitToWidth(maxSampleWidth);
        
        samplesMaxPlus = new float[maxSampleWidth];
        samplesMinMinus = new float[maxSampleWidth];
        rootMeanSquarePlus = new float[maxSampleWidth];
        rootMeanSquareMinus = new float[maxSampleWidth];
        
        for (int i = 0; i < maxSampleWidth; i++) {
            samplesMaxPlus[i] = waveform.getMax(i);
            samplesMinMinus[i] = waveform.getMin(i);
            rootMeanSquarePlus[i] = (float) waveform.getRootMeanSquare(i, true);
            rootMeanSquareMinus[i] = (float) waveform.getRootMeanSquare(i, false);
        }
        
        refresh(true);
    }
    
    public float[] getSamplesMaxPlus(double visualWidth) {
        float[] result = new float[(int) visualWidth];
        Arrays.fill(result, 0.0f); 
        int index = 0;
        
        double scale = visualWidth / maxSampleWidth;
        
        for (int i = 0; i < samplesMaxPlus.length; i++) {
            index = (int) Math.floor(i * scale);
            if (samplesMaxPlus[i] > result[index]) {
                result[index] = samplesMaxPlus[i];
            }
        }
        
        return result;
    }

    public float[] getSamplesMinMinus(double visualWidth) {
        float[] result = new float[(int) visualWidth];
        Arrays.fill(result, 0.0f); 
        int index = 0;
        
        double scale = visualWidth / maxSampleWidth;
        
        for (int i = 0; i < samplesMinMinus.length; i++) {
            index = (int) Math.floor(i * scale);
            if (samplesMinMinus[i] < result[index]) {
                result[index] = samplesMinMinus[i];
            }
        }
        
        return result;
    }
    
    public float[] getRootMeanSquarePlus(double visualWidth) {
        float[] result = new float[(int) visualWidth];
        int index = 0;
        int count = 0;
        int nextIndex;
        double sum = 0;
        
        double scale = visualWidth / maxSampleWidth;
        
        for (int i = 0; i < rootMeanSquarePlus.length; i++) {
            sum += rootMeanSquarePlus[i];
            count++;
            nextIndex = (int) Math.floor(i * scale);
            
            if (index != nextIndex) {
                result[index] = (float) (sum / count);
                sum = 0;
                count = 0;
                index = nextIndex;
            }
        }
        
        return result;
    }
    
    public float[] getRootMeanSquareMinus(double visualWidth) {
        float[] result = new float[(int) visualWidth];
        int index = 0;
        int count = 0;
        int nextIndex;
        double sum = 0;;
        
        double scale = visualWidth / maxSampleWidth;
        
        for (int i = 0; i < rootMeanSquareMinus.length; i++) {
            sum += rootMeanSquareMinus[i];
            count++;
            nextIndex = (int) Math.floor(i * scale);
            
            if (index != nextIndex) {
                result[index] = (float) (sum / count);
                sum = 0;
                count = 0;
                index = nextIndex;
            }
        }
        
        return result;
    }
    
    public DoubleProperty loopStartingProperty() {
        return loopStartingProperty;
    }

    public DoubleProperty loopTerminalProperty() {
        return loopTerminalProperty;
    }
    
    
}
