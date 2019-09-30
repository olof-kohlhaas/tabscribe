package kim.kohlhaas.sone.tabscribe.javafx;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import kim.kohlhaas.sone.harmony.MidiToneSet;
import kim.kohlhaas.sone.harmony.Tone;
import kim.kohlhaas.sone.signal.FloatSourceDataLine;
import kim.kohlhaas.sone.synthesis.GuitarString;
import kim.kohlhaas.sone.tabscribe.javafx.control.BeatBarEvent;
import kim.kohlhaas.sone.tabscribe.model.BarLine;
import kim.kohlhaas.sone.tabscribe.model.BeatParser;
import kim.kohlhaas.sone.tabscribe.model.Session;


public class Metronome {

	private static final float SAMPLE_RATE = 44100.0f;
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 2;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;
	
    private AudioFormat format;
    private SourceDataLine sourceDataLine;
    
    private final float[][] frameChunk;
    
    private float volume = 1.0f;
    private FloatControl volumeControl = null;
    
	private final DoubleProperty millisecondProperty;
	private double millisecondPosition;
    private Double nextBeatMilli = 0.0;
    private boolean isNextBeatMeasure = true;
    
    private MidiToneSet midiToneSet = new MidiToneSet();
    private int minBufferLength;
    private final int bufferLength;

    private ChangeListener<Number> positionHandler;
    private BeatParser beatParser;
	
	public Metronome() {		
		format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
		sourceDataLine = new FloatSourceDataLine();
		
		minBufferLength = (int) Math.ceil(SAMPLE_RATE / midiToneSet.getKeyTone(0).getFrequency());
		bufferLength = minBufferLength * 2;
		frameChunk = new float[CHANNELS][bufferLength];
		
		positionHandler = (observable, oldValue, newValue) -> onMillisecondChanged(oldValue.doubleValue(), newValue.doubleValue());
		
		millisecondProperty = new SimpleDoubleProperty(0.0);
		millisecondProperty.addListener(positionHandler);
	}
	
	public void open() throws LineUnavailableException {
        if (!sourceDataLine.isOpen()) {
            ((FloatSourceDataLine)sourceDataLine).open(format, bufferLength, true);
            volumeControl = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
            setVolume(volume);
            sourceDataLine.start();
        }
    }
    
    public void close(boolean immediately) {

        if (sourceDataLine.isOpen()) {

            if (immediately) {
                sourceDataLine.stop();
                sourceDataLine.flush();
            } else {
                sourceDataLine.drain();
                sourceDataLine.stop();
            }
            
            sourceDataLine.close();
            volumeControl = null;
        }

    }

    public void click(Tone tone, double milliseconds, double ampFactor) {
    	int samples = (int) ((milliseconds / 1000.0) * SAMPLE_RATE);
    	int sample = 0;
    	int remainder;
    	
    	((FloatSourceDataLine) sourceDataLine).flush();
    	
    	while(sample < samples) {   	
			         
			for (int b = 0; b < frameChunk[0].length && sample < samples; b++) {
				for (int c = 0; c < CHANNELS; c++) {   
					frameChunk[c][b] = (float)  (Math.sin(2.0 * Math.PI * tone.getFrequency() * ((double) sample / SAMPLE_RATE)) * ampFactor);
				}
				sample++;
			}
			if ((remainder = (sample + 1) % bufferLength) == 0) {
				((FloatSourceDataLine) sourceDataLine).write(frameChunk, 0, bufferLength);
			} else {
				((FloatSourceDataLine) sourceDataLine).write(frameChunk, 0, remainder);
			}
        
    	}
    }	
	
	private void onMillisecondChanged(double oldMilli, double newMilli) {
		millisecondPosition = newMilli;
		if (nextBeatMilli != null && millisecondPosition > nextBeatMilli) {
			if (isNextBeatMeasure) {
				click(midiToneSet.getKeyTone(94), 10.0, 1.0);
			} else {
				click(midiToneSet.getKeyTone(75), 10.0, 0.75);
			}
			calcNextBeat();
		}
	}
	
	private void calcNextBeat() {
		if (beatParser != null) {
			nextBeatMilli = beatParser.getCeilingBeatMilli();
			isNextBeatMeasure = beatParser.isMeasureMilli(nextBeatMilli);
		}
	}
	
	public void reset() {
		calcNextBeat();
	}	
	
	public DoubleProperty millisecondProperty() {
        return millisecondProperty;
    }
	
    public float getVolume() {
		return volume;
	}
    
    public void pause() {
    	millisecondProperty.removeListener(positionHandler);
    }
    
    public void resume() {
    	millisecondProperty.removeListener(positionHandler);
    	reset();
    	millisecondProperty.addListener(positionHandler);
    }

	public void setVolume(float volume) {
		this.volume = volume;
		
		if (volumeControl != null) {
			// 100% equals 0dB and 0% equals the minimum lower than 0dB, not using the maximum rang yet
			float min = volumeControl.getMinimum();
			double dBVolume = 10 * Math.log10(volume); 
			dBVolume *= 4; //TODO check calculation for loudness. 
			if (dBVolume < min) {
				dBVolume = min;
			} 

			volumeControl.setValue((float) dBVolume);

		}
	}

	public BeatParser getBeatParser() {
		return beatParser;
	}

	public void setBeatParser(BeatParser beatParser) {
		this.beatParser = beatParser;
		this.calcNextBeat();
	}
	
}
