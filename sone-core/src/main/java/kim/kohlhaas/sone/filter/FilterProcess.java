package kim.kohlhaas.sone.filter;

import java.util.List;

import javax.sound.sampled.AudioFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kim.kohlhaas.sone.buffer.PrimitiveDoubleQueue2D;
import kim.kohlhaas.sone.buffer.PrimitiveFloatQueue2D;

public class FilterProcess implements Runnable, FilterWorker {
    
    final static Logger log = LoggerFactory.getLogger(FilterProcess.class);
    private static final int DEFAULT_CHANNELS = 2;
    
    private PrimitiveFloatQueue2D input;
    private FilterConfig config;
    private int channels;
    private ManagedFilter filter;
    private FilterChain filterChain;
    private Thread thread;
    private float[][] sampleCarrier;
    private float[][] overlapBuffer;
    private float[][] lastChunk;
    private boolean isLastChunkReleased = true;
    private final NextFilter nextFilter = new NextFilter();
    
    private double stepFilterTimeScale = 1.0;
    private double lastStepFilterTimeScale = 1.0;
    
    private double lastFilterTimeScale = 1.0;
    private double filterTimeScale = 1.0;
    private long totalFramesReceived = 0;
    private long totalFramesReleased = 0;
    private int currentFramesReceived = 0;
    private int currentFramesReleased = 0;
    private boolean firstFrame;
    
    public FilterProcess(ManagedFilter filter, FilterChain filterChain) {
        this(FilterProcess.DEFAULT_CHANNELS, filter, filterChain);
    }
    
    public FilterProcess(AudioFormat format, ManagedFilter filter, FilterChain filterChain) {
        this(format.getChannels(), filter, filterChain);
    } 
    
    public FilterProcess(int channels, ManagedFilter filter, FilterChain filterChain) {
        this.channels = channels;
        this.filter = filter;
        this.filterChain = filterChain;
        this.config = new FilterConfig();
        
        resetBuffer();
        filter.init(config);
    }

    
    
    private void resetFilterTimeScale() {
        filterTimeScale = 1.0;
        lastFilterTimeScale = 1.0;
        totalFramesReceived = 0;
        totalFramesReleased = 0;
        stepFilterTimeScale = 1.0;
        lastStepFilterTimeScale = 1.0;
        currentFramesReceived = 0;
        currentFramesReleased = 0;
    }

    public double getFilterTimeScale() {
        return filterTimeScale;
    }

    private void resizeCarrier() {
        synchronized (input) {
            sampleCarrier = new float[sampleCarrier.length][config.getChunkSize()];
            input.notifyAll();
        }
    }
    
    private void resetBuffer() {
        terminate();
        firstFrame = true;
        
        if (input == null || input.getChannels() != channels) {
            input = new PrimitiveFloatQueue2D(config.getInputBufferSize(), channels, filter.getClass().getName());
        }
        
        if (sampleCarrier == null || sampleCarrier.length != channels || sampleCarrier[0].length != config.getChunkSize()) {
            sampleCarrier = new float[channels][config.getChunkSize()];
        }
        
        if (overlapBuffer == null || overlapBuffer.length != channels || overlapBuffer[0].length != config.getOverlap()) {
            overlapBuffer = new float[channels][config.getOverlap()];
        }
        
        if (lastChunk == null || lastChunk.length != channels || lastChunk[0].length != config.getChunkSize()) {
            lastChunk = new float[channels][config.getChunkSize()];
        }
        
        thread = new Thread(this);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setName(config.getThreadName());
        thread.start();        
    }
    
    private void resetOverlap() {
        float[][] tmp = new float[channels][overlapBuffer[0].length];
        synchronized (input) {
            for (int c = 0; c < channels; c++) {
                System.arraycopy(overlapBuffer[c], 0, tmp[c], 0, overlapBuffer[0].length);
            }
            
            overlapBuffer = new float[channels][config.getOverlap()];
            
            for (int c = 0; c < channels; c++) {
                System.arraycopy(tmp[c], 0, overlapBuffer[c], 0, Math.min(tmp[0].length, config.getOverlap()));
            }
        }
    }   

    @Override
    public void run() {
        boolean carrierFilled = false;
        
        while (!Thread.currentThread().isInterrupted()) {
            synchronized (input) {
                try {
                    if (input.length() < config.getChunkSize()) {
                        input.wait();
                    }
                    
                    carrierFilled = false;
                    if (input.length() >= config.getChunkSize() && firstFrame 
                            || input.length() >= config.getChunkSize() - config.getOverlap() && !firstFrame) {
                        
                        if (firstFrame) {
                            input.shift(sampleCarrier, sampleCarrier[0].length);
                        } else {
                            input.shift(sampleCarrier, config.getOverlap(), sampleCarrier[0].length - config.getOverlap());
                        }
                        
                        
                        for (int c = 0; c < sampleCarrier.length; c++) {
                            if (lastChunk[c].length != sampleCarrier[c].length) {
                                lastChunk[c] = new float[sampleCarrier[c].length];
                            }
                            if (firstFrame) {
                                System.arraycopy(sampleCarrier[c], 0, lastChunk[c], 0, sampleCarrier[c].length);
                            } else {
                                System.arraycopy(sampleCarrier[c], 0, lastChunk[c], 0, sampleCarrier[c].length - config.getOverlap());
                                System.arraycopy(overlapBuffer[c], 0, sampleCarrier[c], 0, config.getOverlap());
                            }
                            
                            System.arraycopy(sampleCarrier[c], sampleCarrier[c].length - config.getOverlap(), overlapBuffer[c], 0, config.getOverlap());
                        }
                        isLastChunkReleased = false;
                        carrierFilled = true;
                        
                        input.notifyAll();
                    }
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            if (carrierFilled) {
                if (!Thread.currentThread().isInterrupted()) {
                    if (firstFrame) {
                        currentFramesReceived += sampleCarrier[0].length;
                    } else {
                        currentFramesReceived += sampleCarrier[0].length - config.getOverlap();
                    }
                    firstFrame = false;
                    filter.writeFrame(sampleCarrier, nextFilter);
                } else {

                }
            }
        }
        log.debug("process terminated");
    }
    
    public void terminate() {
        if (thread != null) {
            thread.interrupt();
        }
    }
    
    @Override
    public PrimitiveFloatQueue2D getInput() {
        return input;
    }


    @Override
    public FilterConfig getConfig() {
        return config;
    }
    
    
    public float[][] emptyLastChunk() {
        float[][] result;
        
        
        
        synchronized (input) {
            if (isLastChunkReleased) {
                result = new float[channels][0];
            } else {
                isLastChunkReleased = true;
                result = new float[channels][config.getChunkSize()-config.getOverlap()];
                for (int c = 0; c < channels; c++) {
                    log.debug("EMPTY LAST CHUNK - channel: {}, last chunk length: {}, result length: {}, conf chunk size: {}, conf overlap: {}",
                            c, lastChunk[c].length, result[c].length, config.getChunkSize(), config.getOverlap());
                    System.arraycopy(lastChunk[c], 0, result[c], 0, config.getChunkSize()-config.getOverlap());
                }
                
            }
        }
        
        return result;
    }
    
    public float[][] emptyInput() {
        float[][] result;
        synchronized (input) {
            result = input.shift(0);
            currentFramesReceived = 0;
            currentFramesReleased = 0;
            input.notifyAll();
        }
        
        return result;
    }
    
    public class NextFilter implements FilterFlow {
        private boolean isFlushed = false;
        
        public void flush() {
            isFlushed = true;
        }
        
        @Override
        public void next(float[][] frameValues) {
            next(frameValues, 0, frameValues[0].length);
        }

        @Override
        public void next(float[][] frameValues, int offset, int length) {
         // Soll vermeiden, dass nach einem längeren wait in einen möglicherweise bereits entfernten Filter geschrieben wird.
            boolean wasBlocked = true;
            FilterWorker nextFilterWorker;
            PrimitiveFloatQueue2D output;      
            
            while (wasBlocked && !Thread.currentThread().isInterrupted()) {
                nextFilterWorker = filterChain.getNextFilterWorker(FilterProcess.this);
                wasBlocked = false;
                if (nextFilterWorker != null) {
                    output = nextFilterWorker.getInput();
                    
                    synchronized (output) {
                        while (output.length() >= nextFilterWorker.getConfig().getInputBufferSize() && !Thread.currentThread().isInterrupted()) {
                            try {
                                wasBlocked = true;
                                output.wait();
                                if (isFlushed) {
                                    isFlushed = false;
                                    return;
                                }   
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }
                        
                        if (Thread.currentThread().isInterrupted()) {
                            return;
                        }
                        
                        if (isFlushed) {
                            isFlushed = false;
                            return;
                        }
                        
                        if (!wasBlocked) {
                            currentFramesReleased = length;
                            output.push(frameValues, offset, length);
                            
                            isLastChunkReleased = true;
                            
                            totalFramesReceived += currentFramesReceived;
                            totalFramesReleased += currentFramesReleased;                     
                                                        
                            if (totalFramesReceived > 0) {
                                lastFilterTimeScale = filterTimeScale; 
                                filterTimeScale = (double) totalFramesReleased / totalFramesReceived;
                            }
                            
                            if (currentFramesReceived > 0) {
                                lastStepFilterTimeScale = stepFilterTimeScale;
                                stepFilterTimeScale = (double) currentFramesReleased / currentFramesReceived;
                                
                                if (lastStepFilterTimeScale != stepFilterTimeScale){
                                    double diff = 1.0 - lastStepFilterTimeScale / stepFilterTimeScale;
                                    if (Math.abs(diff) >= 0.05) {
                                        filterChain.adjustTimePosition();
                                    }
                                }
                            }
                            
                            currentFramesReceived = 0;
                            output.notifyAll();
                        }
                    }
                } else {
                    // Filterergebnis verfällt, da Filter vermutlich abgeschaltete
                }
            }
        }
    }
    
    public class FilterConfig {
        private int overlap;
        private int inputBufferSize;
        private int chunkSize;
        private Filter filter;
        private String threadName;
        
        public FilterConfig() {
            this.overlap = 0;
            // wenn sich im inputbuffer mehr frames stauen, wird der vorangestellte filter geblockt
            this.inputBufferSize = 300;
            this.chunkSize = 100;
            this.threadName = FilterProcess.this.filter.getClass().getName();
        }

        public int getOverlap() {
            return overlap;
        }

        public void setOverlap(int overlap) {
            this.overlap = overlap;
            FilterProcess.this.resetOverlap();
        }

        public int getInputBufferSize() {
            return inputBufferSize;
        }

        public void setInputBufferSize(int inputBufferSize) {
            this.inputBufferSize = inputBufferSize;
            FilterProcess.this.getInput().resize(inputBufferSize);
        }

        public int getChunkSize() {
            return chunkSize;
        }

        public void setChunkSize(int chunkSize) {
            this.chunkSize = chunkSize;
            if (chunkSize > this.inputBufferSize) {
                setInputBufferSize(chunkSize * 2);
            }
            FilterProcess.this.resizeCarrier();
        }

        public String getThreadName() {
            return threadName;
        }

        public void setThreadName(String threadName) {
            this.threadName = threadName;
            FilterProcess.this.thread.setName(this.threadName);
        }
        
    }

    @Override
    public Filter getFilter() {        
        return filter;
    }

    @Override
    public int getFramesInProgress() {
        return this.currentFramesReceived;
    }

    @Override
    public void onAddedToFilterChain() {
        resetFilterTimeScale();        
    }

    @Override
    public void onRemovedFromFilterChain() {
        resetFilterTimeScale();        
    }

    @Override
    public void onLineChanged(AudioFormat format) {
        this.channels = format.getChannels();
        resetBuffer();
        resetFilterTimeScale();
        filter.onLineChanged();        
    }

    @Override
    public void flush() {
        nextFilter.flush();
        synchronized (input) {
            input.discard(input.length());
            input.notifyAll();
        }
    }

    

}
