package kim.kohlhaas.sone.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kim.kohlhaas.sone.buffer.PrimitiveFloatQueue2D;
import kim.kohlhaas.sone.filter.FilterProcess.FilterConfig;
import kim.kohlhaas.sone.signal.FloatPCMByteConverter;
import kim.kohlhaas.sone.signal.FloatSourceDataLine;

public class FilterChain  {

    final static Logger log = LoggerFactory.getLogger(FilterChain.class);
    
    private HashMap<ManagedFilter, FilterWorker> filterProcessByFilter = new HashMap<ManagedFilter, FilterWorker>();
    
    private List<FilterWorker> filterProcesses = new ArrayList<FilterWorker>();
    
    private FilterProcess pcmFilterProcess;
    private FilterProcess headFilterProcess;
    

    private AudioFormat format;
    private SourceDataLine sourceDataLine;

    private int framesReceived = 0;
    private int framesReleased = 0;
    private long lastFilterCompoMicroseconds;
    private long lastFilterCompoEqualized;
    private long lastLineMicrosecond = 0L;
    private long currentLineMicrosecond = 0L;
    private String name;
    
    private final int chunkFrames;
    
    
    private final Object PAUSE_TOKEN = new Object();
    private boolean isPaused = false;
    private boolean wasFlushedWhenPaused = false;
    
    private float volume = 1.0f;
    private FloatControl volumeControl = null;

    public FilterChain(String name, int chunkFrames) {
    	this.name = name;
    	this.chunkFrames = chunkFrames;
        pcmFilterProcess = new FilterProcess(new DecimalToPCMFilter(), this);
        // sorgt dafür, dass immer ein Filter da ist, der Frames annimmt und hinter dem weitere filter hinzugefügt und 
        // entfernt werden können, ohne dass dabei der ankommende Stream der DecimalDataSource geblockt werden muss.
        headFilterProcess = new FilterProcess(new HeadFilter(), this);
    }
    
    public void open(AudioFormat format) throws LineUnavailableException {
        
        if(!format.equals(this.format) ) {
            this.format = format;

            sourceDataLine = new FloatSourceDataLine();

            
            for (Map.Entry<ManagedFilter, FilterWorker> entry : filterProcessByFilter.entrySet()) {
                entry.getValue().onLineChanged(format);
            }
            pcmFilterProcess.onLineChanged(format);
            headFilterProcess.onLineChanged(format);

        }
        
        if (!sourceDataLine.isOpen()) {
            ((FloatSourceDataLine)sourceDataLine).open(format, this.chunkFrames * 2, true);
            framesReceived = 0;
            framesReleased = 0;
            lastFilterCompoMicroseconds = 0;
            lastFilterCompoEqualized = 0;
        }
        
        
        volumeControl = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
        setVolume(volume);
        
    }
    
    public void stop() {
        synchronized (PAUSE_TOKEN) {
            isPaused = true;
            if (sourceDataLine != null) {
            	sourceDataLine.stop();
            }
        }
    }
    
    public void start() {
        synchronized (PAUSE_TOKEN) {
            isPaused = false;
            PAUSE_TOKEN.notifyAll();
            sourceDataLine.start();
        }
    }
    
    public void flush() {
        if (isPaused) {
            wasFlushedWhenPaused = true;
        }
        
        if (headFilterProcess != null) {
            headFilterProcess.flush();
        }
        
        for (FilterWorker filterWorker : filterProcesses) { 
            filterWorker.flush();
        }
        
        if (pcmFilterProcess != null) {
            pcmFilterProcess.flush();
        }
        
        if (sourceDataLine != null) {
            sourceDataLine.flush();
        }
    }
    
    public void drain() {
        sourceDataLine.drain();
    }
    
    public void close() {
        log.debug("CLOSE");
        flush();
        
        if (sourceDataLine != null) {
            sourceDataLine.flush();
            sourceDataLine.close();
        }
        
        volumeControl = null;
    }
        
    public FilterWorker getNextFilterWorker(FilterWorker filterWorker) {

        synchronized (filterProcesses) {
            if (filterWorker.equals(headFilterProcess)) {
                if (filterProcesses.size() > 0) {
                    return filterProcesses.get(0);
                } else {
                    return this.pcmFilterProcess;
                }
            }
            
            int index = filterProcesses.indexOf(filterWorker);
            
            if (index == -1) {
                return null;
            }
            
            if (index + 1 >= filterProcesses.size()) {
                
                return pcmFilterProcess;
            } else {
                return filterProcesses.get(index + 1);
            }
        }
    }
    
    public void terminate() {
        for (Map.Entry<ManagedFilter, FilterWorker> entry : filterProcessByFilter.entrySet()) {
            entry.getValue().terminate();
        }
        
        if (headFilterProcess != null) {
            headFilterProcess.terminate();
        }
        
        if (pcmFilterProcess != null) {
            pcmFilterProcess.terminate();
        }
        
        close();
    } 
    
    public long getMicrosecondPosition() {
        double timeScale = 1.0;
        long currentFilterCompoEqualized;
        long result;
                
        for (FilterWorker filterWorker : filterProcesses) {
            timeScale *= filterWorker.getFilterTimeScale();
        }

        if (sourceDataLine != null) {
        	currentLineMicrosecond = sourceDataLine.getMicrosecondPosition();
        	if (this.currentLineMicrosecond < lastLineMicrosecond && this.currentLineMicrosecond > 0) {
        		log.warn("SourceDataLine jumped back from {} to {}. Maybe adjust dataline buffersize.", this.lastLineMicrosecond,  this.currentLineMicrosecond);
        	}
        	lastLineMicrosecond = this.currentLineMicrosecond;
        	
            currentFilterCompoEqualized = (long) ((currentLineMicrosecond - this.lastFilterCompoMicroseconds) / timeScale);
            result = currentFilterCompoEqualized + this.lastFilterCompoEqualized;
            
            this.lastFilterCompoEqualized += ((getMicrosecondPositionByFlow() - result) / 10);
            
            if (filterProcesses.size() > 0) {
                return result;
            } else {
                return getMicrosecondPositionByFlow();
            }
        } else {
            return 0;
        }
    }
    
    private long getMicrosecondPositionByFlow() {
        double flowScale = 1.0;       
        
        if (this.getFramesReleased() > 0) {
            flowScale = (double) (this.getFramesReceived() - this.getTotalFramesInProgress()) / this.getFramesReleased();
        }
        
        if (sourceDataLine != null) {
            return (long) (sourceDataLine.getMicrosecondPosition() * flowScale);
        } else {
            return 0;
        }
    }
    
    public void adjustTimePosition() {
        this.lastFilterCompoEqualized = this.getMicrosecondPositionByFlow();
        this.lastFilterCompoMicroseconds = sourceDataLine.getMicrosecondPosition();
    }
    
    public int getTotalFramesInProgress() {
        int frames = this.headFilterProcess.getInput().length() + this.headFilterProcess.getFramesInProgress();
        FilterWorker previousFilter = this.headFilterProcess;
        for (FilterWorker filterWorker : filterProcesses) {
            frames += (filterWorker.getInput().length() + filterWorker.getFramesInProgress()) / previousFilter.getFilterTimeScale();
            previousFilter = filterWorker;
        }
        
        frames += this.pcmFilterProcess.getInput().length();
        
        return frames;
    }
    
    public long getFramesReleasedNotPlayed() {
        return this.getFramesReleased() - sourceDataLine.getLongFramePosition();
    }
    

    public void addFirst(ManagedFilter filter) {
        add(0, filter);
    }   
    
    public void addLast(ManagedFilter filter) {
        add(-1, filter);
    }
    
    public void add(int index, ManagedFilter filter) {
        log.debug("ADD filter: {}", filter);
        synchronized (filterProcesses) {
            
            this.lastFilterCompoEqualized = this.getMicrosecondPositionByFlow();
            if (sourceDataLine != null) {
                this.lastFilterCompoMicroseconds = sourceDataLine.getMicrosecondPosition();
            } else {
                this.lastFilterCompoMicroseconds = 0;
            }
            
            FilterWorker filterWorker = filterProcessByFilter.get(filter);
            
            if (filterWorker == null) {
                if (format != null) {
                    filterWorker = new FilterProcess(format, filter, this);
                } else {
                    filterWorker = new FilterProcess(filter, this);
                }
            
                filterProcessByFilter.put(filter, filterWorker);
            }
            
            if (!filterProcesses.contains(filterWorker)) {
                filterWorker.onAddedToFilterChain();
                if (index == -1) {
                    filterProcesses.add(filterWorker);
                } else {
                    filterProcesses.add(index, filterWorker);
                }
            }
        
            
        }
    }
    
    public boolean isFirst(Filter filter) {    
        FilterWorker filterWorker = filterProcessByFilter.get(filter);
        
        if (filterWorker != null) {
        
            synchronized (filterProcesses) {
                if (this.filterProcesses.indexOf(filterWorker) == 0) {
                    return true;
                } else {
                    return false;
                }
            }
            
        } else {
            return false;            
        }
    }
    
    public boolean isLast(Filter filter) {    
        FilterWorker filterWorker = filterProcessByFilter.get(filter);
        
        if (filterWorker != null) {
        
            synchronized (filterProcesses) {
                if (this.filterProcesses.indexOf(filterWorker) == this.filterProcesses.size() - 1) {
                    return true;
                } else {
                    return false;
                }
            }
            
        } else {
            return false;            
        }
    }
    
    public void remove(Filter filter) {
        log.debug("REMOVE filter: {}", filter);
        FilterWorker nextFilterWorker;
        float[][] removedInput;
        float[][] removedLastChunk;
        PrimitiveFloatQueue2D nextInput;
        int index;
        FilterWorker filterWorker = filterProcessByFilter.get(filter);
        
        if (filterWorker != null) {
        
            synchronized (filterProcesses) {
                index = this.filterProcesses.indexOf(filterWorker);
                
                if (index == -1) {
                    return;
                }
                
                this.lastFilterCompoEqualized = this.getMicrosecondPositionByFlow();
                if (sourceDataLine != null) {
                    this.lastFilterCompoMicroseconds = sourceDataLine.getMicrosecondPosition();
                }
                
                nextFilterWorker = this.getNextFilterWorker(filterWorker);
                nextInput = nextFilterWorker.getInput();
                this.filterProcesses.remove(index);
            }
                
            removedInput = filterWorker.emptyInput();
            removedLastChunk = filterWorker.emptyLastChunk();
            
            /* in this case an exceeded limit of the input buffer size is ignored because the player may be paused so
             * that switching off a filter would block. Asynchronous waiting in a thread may lead to unreasonably
             * complicated handling of samples due to repeatedly switching on and off the filter. */
            synchronized (nextInput) {
                nextInput.push(removedLastChunk, 0, removedLastChunk[0].length);
                nextInput.push(removedInput, 0, removedInput[0].length);
                nextInput.notifyAll();
            }

            
            // release this filter AND the previous filter from wait()-blocking
            filterWorker.flush();
            
            filterWorker.onRemovedFromFilterChain();
        }
        
    }
    
    public int getPosition(Filter filter) {
        FilterWorker filterWorker;
        
        synchronized (filterProcesses) {
            filterWorker =  this.filterProcessByFilter.get(filter);
            return this.filterProcesses.indexOf(filterWorker);
        }
    }
    
    public boolean contains(Filter filter) {
        FilterWorker filterWorker;
        
        synchronized (filterProcesses) {
            filterWorker =  this.filterProcessByFilter.get(filter);
            return this.filterProcesses.indexOf(filterWorker) != -1;
        }
    }
    
    public boolean isEmpty() {
        synchronized (filterProcesses) {
            return this.filterProcesses.isEmpty();
        }
    }
    
    public int getFramesReceived() {
        return framesReceived;
    }

    public int getFramesReleased() {
        return framesReleased;
    }
   
    public void writeFrames(float[][] frameValues, int offset, int length) {
        PrimitiveFloatQueue2D output = headFilterProcess.getInput();
        
        synchronized (output) {
            while (output.length() >= headFilterProcess.getConfig().getInputBufferSize()) {
                try {
                    output.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            if (Thread.currentThread().isInterrupted()) {
                log.info("Thread \'{}\' has been interrupted. Discarding frames.", Thread.currentThread().getName());
            } else {
                output.push(frameValues, offset, length);
                framesReceived+=length;
                output.notifyAll();
            }
        }
        
    }
    
    public void writeFrames(float[][] frameValues) {
    	writeFrames(frameValues, 0, frameValues[0].length);
    }
    
    private void writeResultFrame(float[][] frameValues) {
        synchronized (PAUSE_TOKEN) {
            if (isPaused) {
                try {
                    PAUSE_TOKEN.wait();
                    if (wasFlushedWhenPaused) {
                        wasFlushedWhenPaused = false;
                        return;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }            

            ((FloatSourceDataLine) sourceDataLine).write(frameValues, 0, frameValues[0].length);
            framesReleased += frameValues[0].length;
        }

    }
    
    public class DecimalToPCMFilter implements ManagedFilter {

        @Override
        public void writeFrame(float[][] frameValues, FilterFlow filterFlow) {
            FilterChain.this.writeResultFrame(frameValues);
        }

        @Override
        public void init(FilterConfig config) {
            config.setChunkSize(chunkFrames);
            config.setInputBufferSize(chunkFrames * 2);
            config.setThreadName(name + " - Decimal To Byte PCM Thread");
        }

        @Override
        public void onLineChanged() {}

    }
    
    public class HeadFilter implements ManagedFilter {

        @Override
        public void writeFrame(float[][] frameValues, FilterFlow filterFlow) {
            filterFlow.next(frameValues);
        }

        @Override
        public void init(FilterConfig config) {
            config.setThreadName(name + " - Filter Chain Head Thread");            
        }

        @Override
        public void onLineChanged() {}
        
    }

	public float getVolume() {
		return volume;
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
    
    

}
