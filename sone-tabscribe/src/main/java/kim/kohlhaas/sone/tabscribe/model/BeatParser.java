package kim.kohlhaas.sone.tabscribe.model;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.SubmissionPublisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import kim.kohlhaas.sone.signal.FloatAudioSignal;

public class BeatParser implements Publisher<BarLine> {

	private final static Logger log = LoggerFactory.getLogger(BeatParser.class);
	
	private Session session;
	private FloatAudioSignal signal;
	
	private double nextBarlineMilli;
	private double lastBarlineMilli;
	private BarLine currentBarline;
	private BarLine nextBarline;
	
	private double millisecondPosition = 0.0;
	private final DoubleProperty millisecondProperty;
	private final IntegerProperty barLineCountProperty;
	private final DoubleProperty viewPortWidthProperty;	
    private double millisecondWidth;
    private final DoubleProperty millisecondWidthProperty;
    
    private double milliLeft;
   	private double width;
	private double zoomXScale;
	private double mouseX;	
	private boolean isMouseOver = false;
	
	private int samplesPerTimeStep;
    private double duration;
    private double virtualWidth;
    private int pixelLength;
    public static final String[] colors = {null, "#1E90FF", "#00FA9A", "#FF1493", "#FF7F50"};
    
    private TreeMap<Integer, Double> beatLineMillis = new TreeMap<>();
    private TreeMap<Double, Integer> beatColors = new TreeMap<>();
    private TreeMap<Double, BarLine> barLineMillis = new TreeMap<>();
    private TreeSet<Double> measureMillis = new TreeSet<>();
	private TreeSet<Double> beatMillis = new TreeSet<>();
	
	private double mouseBeatStartMilli;
	private double mouseBeatEndMilli;
	private double mouseBeatStartX;
	private double mouseBeatEndX;
	
	private SubmissionPublisher<BarLine> barlinePublisher;
    
	public BeatParser(int samplesPerTimeStep) {
		this.samplesPerTimeStep = samplesPerTimeStep;
		
		barLineCountProperty = new SimpleIntegerProperty(1);
		
		viewPortWidthProperty =  new SimpleDoubleProperty(0.0);
		viewPortWidthProperty.addListener((obs, oldValue, newValue) -> setWidth(newValue.doubleValue()));
		
		millisecondProperty = new SimpleDoubleProperty(0.0);
		millisecondProperty.addListener((obs, oldValue, newValue) -> setMillisecondPosition(newValue.doubleValue()));
		
		millisecondWidthProperty = new SimpleDoubleProperty(1.0);
		millisecondWidthProperty.addListener((obs, oldValue, newValue) -> setMillisecondWidth(newValue.doubleValue())); 
		
		barlinePublisher = new SubmissionPublisher<>();
	
	}

	private void setMillisecondPosition(double millisecondPosition) {
		this.millisecondPosition = millisecondPosition;
		if (millisecondPosition >= nextBarlineMilli && nextBarlineMilli < duration || millisecondPosition < lastBarlineMilli) {
			setBarlineSwitchTrigger();
		}
		resetTimeDimensions();
	}
	
	private void setBarlineSwitchTrigger() {
		BarLine recentBarline = getBarLineAt(millisecondPosition);	
		Map.Entry<Double, BarLine> nextEntry = barLineMillis.higherEntry(millisecondPosition);
		
		if (nextEntry != null) {
			nextBarline = nextEntry.getValue();
		} else {
			nextBarline = null;
		}

		lastBarlineMilli = recentBarline.getMillisecond();
		if (nextBarline != null) {
			nextBarlineMilli = nextBarline.getMillisecond();
		} else {
			nextBarlineMilli = duration;
		}
		
		if (currentBarline != null && currentBarline != recentBarline) {
			currentBarline = recentBarline;
			barlinePublisher.submit(currentBarline);
		} else {
			currentBarline = recentBarline;
		}
		
	}

	public BarLine getBarLineAt(double millisecond) {
		Map.Entry<Double, BarLine> result;
		result = barLineMillis.floorEntry(millisecondPosition);
		
		if (result == null) {
			result = barLineMillis.ceilingEntry(millisecondPosition);
		} 
		
		return result.getValue();
	}
	
	public Double getCeilingBeatMilli() {
		return beatMillis.ceiling(millisecondPosition);
	}
	
	public Double getFloorBeatMilli(double millisecond) {
		return beatMillis.floor(millisecond);
	}
	
	public Double getNextBeatMilli() {
		Double ceiling = beatMillis.ceiling(millisecondPosition);
	
		if (ceiling != null && ceiling - millisecondPosition < 1.0) {
			return beatMillis.higher(ceiling);
		} else {
			return ceiling;
		}
	}
	
	public Double getPreviousBeatMilli() {
		Double floor = beatMillis.floor(millisecondPosition);
		
		if (floor != null && millisecondPosition - floor < 1.0) {
			return beatMillis.lower(floor);
		} else {
			return floor;
		}
	}
	
	public Double getNextMeasureMilli() {
		Double ceiling = measureMillis.ceiling(millisecondPosition);
	
		if (ceiling != null && ceiling - millisecondPosition < 1.0) {
			return measureMillis.higher(ceiling);
		} else {
			return ceiling;
		}
	}
	
	public Double getPreviousMeasureMilli() {
		Double floor = measureMillis.floor(millisecondPosition);
		
		if (floor != null && millisecondPosition - floor < 1.0) {
			return measureMillis.lower(floor);
		} else {
			return floor;
		}
	}
	
	public Double getNextBarLineMilli() {
		Double ceiling = barLineMillis.ceilingKey(millisecondPosition);
	
		if (ceiling != null && ceiling - millisecondPosition < 1.0) {
			return barLineMillis.higherKey(ceiling);
		} else {
			return ceiling;
		}
	}
	
	public Double getPreviousBarLineMilli() {
		Double floor = barLineMillis.floorKey(millisecondPosition);
		
		if (floor != null && millisecondPosition - floor < 1.0) {
			return barLineMillis.lowerKey(floor);
		} else {
			return floor;
		}
	}
	
	public Double getLastBeatMilli() {
		return beatMillis.last();
	}
	
	public Double getFirstBeatMilli() {
		return beatMillis.first();
	}
	
	public Double getLastMeasureMilli() {
		return measureMillis.last();
	}
	
	public Double getFirstMeasureMilli() {
		return measureMillis.first();
	}
	
	public Double getLastBarLineMilli() {
		return barLineMillis.lastKey();
	}
	
	public Double getFirstBarLineMilli() {
		return barLineMillis.firstKey();
	}
	
	public boolean isMeasureMilli(Double milli) {
		if (milli == null) {
			return false;
		} else {
			return measureMillis.contains(milli);
		}
	}
	
	public boolean isBeatLine(int x) {
    	return beatLineMillis.containsKey(x);
    }
    
	private void resetMouse() {
		double viewPortX = getPixelPosition() - (int) (width / 2.0);
		Map.Entry<Integer, Double> floorMouse = beatLineMillis.floorEntry((int) (mouseX + viewPortX));
		Map.Entry<Integer, Double> ceilingMouse = beatLineMillis.higherEntry((int) (mouseX + viewPortX));
		
		if (floorMouse == null) {
			mouseBeatStartMilli = 0.0;
			mouseBeatStartX = 0.0;
		} else {
			mouseBeatStartMilli = floorMouse.getValue().doubleValue();
			mouseBeatStartX = floorMouse.getKey().doubleValue() - viewPortX;
			if (mouseBeatStartMilli < milliLeft) {
				mouseBeatStartMilli =  milliLeft;
			}
			
			if (mouseBeatStartX < 0.0) {
				mouseBeatStartX = 0.0;
			}
		}
		
		if (ceilingMouse == null) {
			mouseBeatEndMilli = 0.0;
			mouseBeatEndX = 0.0;
		} else {
			mouseBeatEndMilli = ceilingMouse.getValue().doubleValue();
			mouseBeatEndX = ceilingMouse.getKey().doubleValue() - viewPortX;
			if (mouseBeatEndMilli > milliLeft + millisecondWidth) {
				mouseBeatEndMilli =  milliLeft + millisecondWidth;
			}
			
			if (mouseBeatEndX > width) {
				mouseBeatEndX = width;
			}
		}		
	}
	
	public double getMouseBeatStartMilli() {	
		return mouseBeatStartMilli;
	}
	
	public double getMouseBeatEndMilli() {
		return mouseBeatEndMilli;
	}
	
	public double getMouseBeatStartX() {
		return mouseBeatStartX;
	}
	
	public double getMouseBeatEndX() {
		return mouseBeatEndX;
	}
	
	public int getCeilingBeatX(int x) {
		Integer ceilingX = beatLineMillis.ceilingKey(x);
		
		if (ceilingX == null) {
			return pixelLength;
		} else {
			return ceilingX.intValue();
		}
	}
	
	
    public int getMeasureColor(int x) {
    	Map.Entry<Integer, Double> milli = beatLineMillis.floorEntry(x);
    	if (milli != null && x <= pixelLength) {
    		return beatColors.get(milli.getValue());
    	} else {
    		return 0;
    	}
    }
	
	private void resetGrid() {   	
    	beatLineMillis.clear();
    	Iterator<Double> iterator = beatMillis.iterator();
    	Double beatMilli;
     	
    	while (iterator.hasNext()) {
    		beatMilli = iterator.next();
    		beatLineMillis.put((int) Math.ceil(pixelLength * (beatMilli.doubleValue() / duration)), beatMilli);
    	}
    }    
    
    public void parseMillis() {
    	BarLine currentBarLine = session.getBarLines().getDefaultBarLine();
    	BarLine nextBarLine;
    	double currentMeasureMilli;
    	double currentBeatMilli;
    	double nextMeasureMilli;
    	int measureCount = 0;
    	int barlineCount = 0;
    	int currentColorIndex;
    	
    	barLineMillis.clear();
    	measureMillis.clear();
    	beatMillis.clear();
    	beatColors.clear();
    	
    	while (currentBarLine != null) {
    		nextBarLine = session.getBarLines().getHigherBarLine(currentBarLine.getMillisecond());
    		currentMeasureMilli = currentBarLine.getMillisecond() + currentBarLine.getMeasureMilliOffset();
    		barLineMillis.put(currentMeasureMilli, currentBarLine);
    		
    		if (nextBarLine != null) {
	    		nextMeasureMilli = nextBarLine.getMillisecond() + nextBarLine.getMeasureMilliOffset();
	    	} else {
	    		nextMeasureMilli = duration;
	    	}
    		
    		do {
    			measureMillis.add(currentMeasureMilli);
    			
    			if (measureCount % 2 == 0) {
    				if (barlineCount % 2 == 0) {
    					currentColorIndex = 1;
    				} else {
    					currentColorIndex = 2;
    				}
    			} else {
    				if (barlineCount % 2 == 0) {
    					currentColorIndex = 3;
    				} else {
    					currentColorIndex = 4;
    				}
    			}   			
    			
    			measureCount++;
    			for (int i = 0; i < currentBarLine.getBeatsPerMeasure(); i++) {
    				currentBeatMilli = currentMeasureMilli + i * currentBarLine.getMeasureMilliDuration() / currentBarLine.getBeatsPerMeasure();
    				if (currentBeatMilli <= nextMeasureMilli) {
    					beatMillis.add(currentBeatMilli);
    					beatColors.put(currentBeatMilli, currentColorIndex);
    				}
    			}
    			
    			currentMeasureMilli += currentBarLine.getMeasureMilliDuration();
    		} while (currentMeasureMilli <= nextMeasureMilli);
    		
    		currentBarLine = nextBarLine;
    		barlineCount++;
    		measureCount = 0;
    	}
    	barLineCountProperty.set(barLineMillis.size());
    }
	
	public Beat getBeat(int x) {	
		double leftMilliInclusive = beatLineMillis.get(x);
		Map.Entry<Integer, Double> higherBeat = beatLineMillis.higherEntry(x);
		double rightMilliExclusive;
		int rightXExclusive;
		
		if (higherBeat == null) {
			rightMilliExclusive = duration;
			rightXExclusive = (int) width;
		} else {
			rightMilliExclusive = higherBeat.getValue().doubleValue();
			rightXExclusive = higherBeat.getKey().intValue();
		}
		 
		return new Beat(session, leftMilliInclusive, rightMilliExclusive, x, rightXExclusive);
	}
	

		
	public int getPixelLength() {
		return pixelLength;
	}
	
	public int getPixelPosition() {
		return (int) Math.ceil(pixelLength * (millisecondPosition / duration));
	}
	
	public void reset() {
		if (session != null) {	
			
			parseMillis();
			resetSignalScale();
			resetGrid();
			setBarlineSwitchTrigger();
		}		
	}
	
	private void resetSignalScale() {
		double normMillisecondWidth;
		
		if (signal != null) {
			duration = signal.getMillisecondDuration();
			normMillisecondWidth = ((width * samplesPerTimeStep) / signal.getFrameRate()) * 1000;			
			virtualWidth = Math.ceil(((double) signal.getFrameLength()) / (double) samplesPerTimeStep);	
			zoomXScale = normMillisecondWidth / millisecondWidth;
			pixelLength = (int) Math.ceil(virtualWidth * zoomXScale);
		}	
	}
	
	private void resetTimeDimensions() {
		milliLeft = millisecondPosition - millisecondWidth / 2.0;
	}
	
	public double getMouseMilli() {
		return millisecondPosition - millisecondWidth / 2.0 + mouseX * millisecondWidth / width;
	}
	
	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
		reset();
	}
	
	public DoubleProperty millisecondProperty() {
        return millisecondProperty;
    }

	private void setMillisecondWidth(double millisecondWidth) {
		this.millisecondWidth = millisecondWidth;
		resetTimeDimensions();
		resetSignalScale();
		resetGrid();
	}
	
	public DoubleProperty millisecondWidthProperty() {
		return millisecondWidthProperty;
	}

	public double getMilliLeft() {
		return milliLeft;
	}

	public DoubleProperty viewPortWidthProperty() {
		return viewPortWidthProperty;
	}
	
	private void setWidth(double width) {
		this.width = width;
		resetSignalScale();
	}

	public double getMouseX() {
		return mouseX;
	}

	public void setMouseX(double mouseX) {
		this.mouseX = mouseX;
		resetMouse();
	}

	public FloatAudioSignal getSignal() {
		return signal;
	}

	public void setSignal(FloatAudioSignal signal) {
		this.signal = signal;
		resetSignalScale();
	}

	public double getZoomXScale() {
		return zoomXScale;
	}

	public double getVirtualWidth() {
		return virtualWidth;
	}

	public boolean isMouseOver() {
		return isMouseOver;
	}

	public void setMouseOver(boolean isMouseOver) {
		this.isMouseOver = isMouseOver;
	}
	
	public IntegerProperty barLineCountProperty() {
		return barLineCountProperty;
	}

	@Override
	public void subscribe(Subscriber<? super BarLine> subscriber) {
		barlinePublisher.subscribe(subscriber);
	}
		
}
