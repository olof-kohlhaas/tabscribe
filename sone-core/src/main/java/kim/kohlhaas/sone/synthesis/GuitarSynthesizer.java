package kim.kohlhaas.sone.synthesis;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kim.kohlhaas.sone.filter.Filter;
import kim.kohlhaas.sone.filter.FilterFlow;
import kim.kohlhaas.sone.harmony.MidiToneSet;
import kim.kohlhaas.sone.harmony.PianoToneSet;
import kim.kohlhaas.sone.harmony.Tone;
import kim.kohlhaas.sone.harmony.VeryLongStringToneSet;

import kim.kohlhaas.sone.signal.FloatSourceDataLine;
import kim.kohlhaas.sone.synthesis.GuitarSettings.Body;


public class GuitarSynthesizer implements Runnable {
	
	final static Logger log = LoggerFactory.getLogger(GuitarSynthesizer.class);
    
    private GuitarSettings settings = GuitarSettings.getInstance();
    
    private static final float SAMPLE_RATE = 44100.0f;
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 2;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;
    private static final float VOLUME_FACTOR = 2.0f;

    
    private AudioFormat format;
    private SourceDataLine sourceDataLine;
    private Thread thread;
    private String threadName;
    private GuitarString[] strings;

    // needed because SourceDataLine.write resets the isInterrupted flag of the thread by wait, sleep or something 
    private boolean isInterrupted;
    private static final int CHUNK_SIZE_IN_FRAMES = (int) (SAMPLE_RATE / 100);
    private final Object pluckSync = new Object();
    private final float[][] frameChunk = new float[CHANNELS][CHUNK_SIZE_IN_FRAMES];
    
    private PianoToneSet stringToneSet;
    
    private GuitarCharacter character;
    private Filter guitarBody;
    private GuitarOutput guitarOutput; 
    
    private float volume = 1.0f;
    private FloatControl volumeControl = null;
    
    
    public GuitarSynthesizer(String threadName) throws LineUnavailableException {
        this.threadName = threadName;
        
        format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
                
        stringToneSet = new VeryLongStringToneSet();
        
        character = new GuitarCharacter(format, stringToneSet, 65535);
        
        if (settings.getBody().equals(Body.SIMPLE)) {
            guitarBody = new GuitarSimpleBody(format);
        } else if (settings.getBody().equals(Body.BAND_PASS)) {
            guitarBody = new GuitarBandPassBody(format);
        } else {
            guitarBody = null;
        }
        
        guitarOutput = new GuitarOutput();   

        
        sourceDataLine = new FloatSourceDataLine();    
    }
    
    public GuitarSynthesizer(String threadName, Tone[] tone) throws LineUnavailableException {
    	this(threadName);
    	MidiToneSet midiToneSet = new MidiToneSet();
    	
    	int[] midiIndex = new int[tone.length];
    	
    	for (int i = 0; i < tone.length; i++) {
    		midiIndex[i] = midiToneSet.getIndex(tone[i]);
    	}
    	
    	createStrings(midiIndex);
    	
    }
    
    public GuitarSynthesizer(String threadName, int[] midiIndex) throws LineUnavailableException {
    	this(threadName);
    	
    	createStrings(midiIndex);
    }
    
    public void createStrings(int[] midiIndex) {
        strings = new GuitarString[midiIndex.length];
        
        for (int i = 0; i < strings.length; i++) {
            strings[i] = new GuitarString(format, stringToneSet, character, midiIndex[i] + Math.abs(stringToneSet.getToneOffset()), i);   
        }     
    }
    
    
    
    public void open() throws LineUnavailableException {
        if (!sourceDataLine.isOpen()) {
            ((FloatSourceDataLine)sourceDataLine).open(format, CHUNK_SIZE_IN_FRAMES * 2, true);
            volumeControl = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
            setVolume(volume);
            sourceDataLine.start();
            thread = new Thread(this, threadName);
            thread.setPriority(Thread.MIN_PRIORITY);
            isInterrupted = false;
            thread.start();
        }
    }
    
    public void close(boolean immediately) {
        if (thread != null) {
            isInterrupted = true;
            thread.interrupt();
        }

        if (sourceDataLine.isOpen()) {

            if (immediately) {
                sourceDataLine.stop();
                sourceDataLine.flush();
            } else {
                sourceDataLine.drain();
                sourceDataLine.stop();
            }

            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            sourceDataLine.close();
            volumeControl = null;
        }

    }

    @Override
    public void run() {
        double sampleL = 0;
        double sampleR = 0;
        int frame = 0;
        float change = 0.0f;

        
        while(!isInterrupted) {
            

            sampleL = 0;
            sampleR = 0;
            for (int i = 0; i < strings.length; i++) {

                sampleL += strings[i].sample(GuitarString.Channel.LEFT);
                sampleR += strings[i].sample(GuitarString.Channel.RIGHT);
                strings[i].tic();
            }
            
            
            
            if (sampleL > 1.0) {
                sampleL = 1.0;
            }
            
            if (sampleL < -1.0) {
                sampleL = -1.0;
            }
            
            if (sampleR > 1.0) {
                sampleR = 1.0;
            }
            
            if (sampleR < -1.0) {
                sampleR = -1.0;
            }
            
            
                
            frameChunk[0][frame] = (float) sampleL;
            frameChunk[1][frame] = (float) sampleR;

                
            // add absolute changes up
            change += Math.abs(frameChunk[0][frame]);
            change += Math.abs(frameChunk[1][frame]);

            frame++;
                
            if (frame == CHUNK_SIZE_IN_FRAMES) {
                frame = 0;
        
                // average change of all channels and samples of the last chunk of frames
                    change /= CHANNELS;
                    change /= CHUNK_SIZE_IN_FRAMES;
 
                    
                    for (int i = 0 ; i < frameChunk[0].length; i++) {
                        frameChunk[0][i] = (frameChunk[0][i]) * VOLUME_FACTOR;
                        frameChunk[1][i] = (frameChunk[1][i]) * VOLUME_FACTOR;
                    }

                    if (guitarBody != null) {
                        guitarBody.writeFrame(frameChunk, guitarOutput);
                    } else {
                        guitarOutput.next(frameChunk, 0, CHUNK_SIZE_IN_FRAMES);
                    }
	                synchronized (pluckSync) {
	                    if (change <= 0.00005f && !isInterrupted) {
	                        try {
	                        	log.debug("pluck wait");
                            pluckSync.wait();

                            log.debug("pluck awake");
                        } catch (InterruptedException e) {
                            if (isInterrupted) {
                                Thread.currentThread().interrupt();
                                log.debug("pluck interrupt");
                            }
                        }
                    }
                    change = 0;
                }

            }

        }
        log.debug("guitar process terminated");
    }
    
    private class GuitarOutput implements FilterFlow {

        @Override
        public void next(float[][] frameValues) {
            ((FloatSourceDataLine) sourceDataLine).write(frameValues, 0, CHUNK_SIZE_IN_FRAMES);
        }

        @Override
        public void next(float[][] frameValues, int offset, int length) {
            ((FloatSourceDataLine) sourceDataLine).write(frameValues, offset, length);
        }
        
    }
    
    public void pluck(int index, int tab, double velocity) {
        synchronized (pluckSync) {
            strings[index].pluck(tab, velocity);
            pluckSync.notifyAll();
        }    
    }
    
    public void damp(int index) {
    	strings[index].damp();
    }
    
    public void damp() {
        for (int i = 0; i < strings.length; i++) {
            strings[i].damp();
        }
    }
    
    public void playTone(int index, Tone tone, double velocity) {
        synchronized (pluckSync) {
            strings[index].pluck(stringToneSet.getIndex(tone), velocity);
                        
            pluckSync.notifyAll();
        }
    }
    
    public float getVolume() {
		return volume;
	}

	public void setVolume(float volume) {
		this.volume = volume;
		
		if(volume == 0.0f) {
			damp();
		}
		
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
    
    
}
