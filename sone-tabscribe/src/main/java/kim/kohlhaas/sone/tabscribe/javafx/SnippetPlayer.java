package kim.kohlhaas.sone.tabscribe.javafx;

import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kim.kohlhaas.sone.filter.FilterChain;
import kim.kohlhaas.sone.filter.ResampleFilter;
import kim.kohlhaas.sone.filter.WSOLAFilter;
import kim.kohlhaas.sone.signal.FloatAudioSignal;
import kim.kohlhaas.sone.signal.FloatSourceDataLine;
import kim.kohlhaas.sone.synthesis.GuitarSynthesizer;

public class SnippetPlayer implements Runnable {
	
	final static Logger log = LoggerFactory.getLogger(SnippetPlayer.class);
	
	private FloatAudioSignal signal;
    private AudioFormat format;
    private SourceDataLine sourceDataLine;
    private float volume = 1.0f;
    private FloatControl volumeControl = null;
    private float[][] frameChunk;
    private int bufferLength;
    private Thread thread;
 // needed because SourceDataLine.write resets the isInterrupted flag of the thread by wait, sleep or something 
    private boolean isInterrupted;
    private boolean isNewSnippetAvailable = false;
    private final Object playSync = new Object();
    private double fromMilli;
    private double toMilli;
    private boolean isLooping = false;
    
    private FilterChain filterChain;
    private ResampleFilter resampleFilter;
    private WSOLAFilter wsolaFilter;
    private double pitchFactor = 1.0;
    private double stretchFactor = 1.0;
    private boolean useFilterChain = false;
    private static final int CHUNK_SIZE_IN_FRAMES = 12000;

	public SnippetPlayer() { // TODO generalize Audio-Boilerplate with Metronome and GuitarSynthesizer
		filterChain = new FilterChain("Snippet Player", CHUNK_SIZE_IN_FRAMES / 6);
		bufferLength = CHUNK_SIZE_IN_FRAMES;
		resampleFilter = new ResampleFilter("Snippet Player - Resample Filter");
		sourceDataLine = new FloatSourceDataLine();
		thread = new Thread(this, "Beat-Snippet-Player");
        isInterrupted = false;
        thread.start();
	}	
	
	public void play(double fromMilli, double toMilli, boolean loop) {
		log.debug("play snippet from milli: {}, to milli: {}", fromMilli, toMilli);
		synchronized (playSync) {
			this.fromMilli = fromMilli;
			this.toMilli = toMilli;
			this.isLooping = loop;
			isNewSnippetAvailable = true;
			playSync.notifyAll();
		}
	}
	
	@Override
    public void run() {
		long fromSample = 0;
		long toSample = 0;
		int samples;
		int written;
		boolean isFirstLoop;
		
		
		while (!isInterrupted) {
			if (sourceDataLine != null && sourceDataLine.isOpen()) {
				log.debug("pre flush");
				
				sourceDataLine.flush();
				filterChain.flush();
			}
			isFirstLoop = true;
			do {
				synchronized (playSync) {
					if ((isNewSnippetAvailable || isLooping ) && fromMilli < toMilli && format != null) {
						fromSample = (long) ((fromMilli  / 1000.0) * format.getSampleRate());
						toSample = (long) ((toMilli  / 1000.0) * format.getSampleRate());
					}
					isNewSnippetAvailable = false;
				}
			

			
				while (fromSample <= toSample && toSample > 0 && !isNewSnippetAvailable && !isInterrupted && signal != null && (isLooping || isFirstLoop)) {
					if (fromSample + (bufferLength - 1) <= toSample) {
						samples = bufferLength;
					} else {
						samples = (int) (toSample - fromSample) + 1; 
					}
					
					for (int c = 0; c < format.getChannels(); c++) {  
						signal.copyFrames(c, fromSample, samples, frameChunk[c], 0);
					}			
					
					if (useFilterChain) {
						filterChain.writeFrames(frameChunk, 0, samples);
					} else {
						written = ((FloatSourceDataLine) sourceDataLine).write(frameChunk, 0, samples);
						log.debug("write sample from: {},  count: {}, bufferlength: {}, written: {} ", fromSample, samples,  bufferLength, written);
					}
					
					
					fromSample += bufferLength;
					
				}
				isFirstLoop = false;
				
				
				try {
					Thread.sleep(100L);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					e.printStackTrace();
				}
				if (!isNewSnippetAvailable && !isInterrupted && sourceDataLine != null && sourceDataLine.isOpen()) {
					if (useFilterChain) {
						filterChain.drain();
					} else {
						sourceDataLine.drain();
					}
					
				}
				

			} while (isLooping);
			
			if (isNewSnippetAvailable || isInterrupted) {
				log.debug("post flush");
				sourceDataLine.flush();
				filterChain.flush();
			} 
			
			synchronized (playSync) {
				if (!isNewSnippetAvailable && !isInterrupted) {
					try {
	                    log.debug("snippet play wait");
	                    playSync.wait();
	
	                    log.debug("snippet play awake");
	                } catch (InterruptedException e) {
	                    if (isInterrupted) {
	                        Thread.currentThread().interrupt();
	                        log.debug("snippet play interrupt");
	                    }
	                }
				}
			}
		}
		log.info("snippet play terminated");
	}
	
	public void open(FloatAudioSignal signal) throws LineUnavailableException {
		double wsolaTempo = 1.0;
		
		this.signal = signal;
		format = signal.getFormat();
		
		frameChunk = new float[format.getChannels()][bufferLength];
		
		if (wsolaFilter != null) {
           wsolaTempo = wsolaFilter.getTempo();           
        }
        
        if (wsolaFilter == null) {
            wsolaFilter = new WSOLAFilter("Snippet Player - WSOLA Filter", signal.getFormat(), 20, 100,35, wsolaTempo);
            if (wsolaTempo != 1.0) {
                filterChain.addFirst(wsolaFilter);
            }
        } else {
           wsolaFilter.setFormat(signal.getFormat()); 
        }		
		
        
        
        arrangePitchStretch();

    }
    
    public void close(boolean immediately) {
    	if (thread != null) {
            isInterrupted = true;
            thread.interrupt();
        }
    	
    	filterChain.stop();
    	filterChain.close();
    	
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
    
    public float getVolume() {
		return volume;
	}

	public void setVolume(float volume) { // TODO DRY! in some classes
		this.volume = volume;
		
		filterChain.setVolume(volume);
		
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

	public boolean isLooping() {
		return isLooping;
	}

	public void setLooping(boolean isLooping) {
		this.isLooping = isLooping;
	}
	
	private void setResampleFilterEnabled(boolean isEnabled) {
        
        //TODO Mit Comparable Positionen von WSOLA- und Resample vergleichen, damit später weitere Filter hinzugefügt werden können
        if (isEnabled) { 
            //TODO Diese Abfrage muss momentan nicht in der setWsola... gemacht werde, da in der arrangePitchStretch der Wsola vor resample gesetzt wird 
            if (this.stretchFactor <= 1.0) {
                if (filterChain.isFirst(resampleFilter)) {
                    filterChain.remove(resampleFilter);
                }
                filterChain.addLast(resampleFilter);
            } else {
                if (filterChain.isLast(resampleFilter)) {
                    filterChain.remove(resampleFilter);
                }
                filterChain.addFirst(resampleFilter);
            }
        } else {
            filterChain.remove(resampleFilter);
        }
    }
    
    public void setPitch(double factor) throws LineUnavailableException {
        log.debug("pitch factor set to {}", factor);
        this.pitchFactor = factor;
        arrangePitchStretch();
    }
    
    public void setTimeStretch(double factor) throws LineUnavailableException {
        log.debug("stretch factor set to {}", factor);
        this.stretchFactor = factor;
        arrangePitchStretch();
    }
    
    public double getTimeStretch() {
    	return this.stretchFactor;
    }
    
    private void arrangePitchStretch() throws LineUnavailableException {
    	boolean wasUsingFilterChain = useFilterChain;
    	
    	useFilterChain = !(this.stretchFactor == 1.0 && this.pitchFactor == 1.0);
    	
    	if (wasUsingFilterChain != useFilterChain) {
    		log.info("use filter chain in snippet player: {}", useFilterChain);
    	}
    	
    	if (useFilterChain) {
    		filterChain.open(signal.getFormat());
    		filterChain.start();
    		if (sourceDataLine.isOpen()) {
    			sourceDataLine.flush();
    			sourceDataLine.close();
    		}
    	} else {
    		if (!sourceDataLine.isOpen()) {
                ((FloatSourceDataLine)sourceDataLine).open(format, bufferLength * 2 , true);
            }
            volumeControl = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
            setVolume(volume);
            sourceDataLine.start();
            filterChain.flush();
            filterChain.stop();
    	}
    	
        setWsolaFilterTempo(this.stretchFactor * (1.0 / this.pitchFactor)); 
        resampleFilter.setFactor((1.0 / this.pitchFactor));
        if (Math.floor((1.0 / this.pitchFactor) * 100) == 100) {
            setResampleFilterEnabled(false);
        } else {
            setResampleFilterEnabled(true);
        }

        log.debug("WSOLA-Position: {}, Resample-Position: {}", filterChain.getPosition(wsolaFilter), filterChain.getPosition(resampleFilter));
    }
	
    private void setWsolaFilterTempo(double tempo) {
        log.debug("SET_WSOLA_FILTER tempo: {}", tempo);
        if (tempo == 1.0) {
            filterChain.remove(wsolaFilter);
        } else {
            wsolaFilter.setTempo(tempo);
            if (!filterChain.contains(wsolaFilter)) {
                filterChain.addFirst(wsolaFilter);
            }
        }
    }  
	
}
