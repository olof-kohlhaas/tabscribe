package kim.kohlhaas.sone.filter;

import java.util.ArrayList;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.tarsos.dsp.resample.Resampler;
import kim.kohlhaas.sone.filter.FilterProcess.FilterConfig;

public class ResampleFilter implements ManagedFilter {
	
	final static Logger log = LoggerFactory.getLogger(ResampleFilter.class);
    
    private double factor;
    
    // TODO aus AudioFormat auslesen
    private int channels = 2;
    
    private Resampler resampler;
    
    private final int CHUNK_SIZE = 4096;
    

    private float[][] inputFloatBuffer;
    private float[][] outputFloatBuffer;
    private int overlap;
    private boolean isStreamHead;
    private final Object CONTINUOUS_STREAM = new Object();
    private final Object FACTOR_LOCK = new Object();
    private int nextOffset;
    private int nextLength;
    private String threadName;
    private double minFactor = 0.1;
    private double maxFactor = 4.000000000000004;
    
    public ResampleFilter(String threadName) {
    	this.threadName = threadName;
        factor = 1.0;
        resampler = new Resampler(false, minFactor, maxFactor);
        
        overlap = (int) CHUNK_SIZE / 2;
        
        inputFloatBuffer = new float[channels][CHUNK_SIZE];
        outputFloatBuffer = new float[channels][(int) (CHUNK_SIZE * factor)];

    }

    public void setFactor(double factor) {
        synchronized (FACTOR_LOCK) {
            this.factor = factor;
            if (outputFloatBuffer[0].length < CHUNK_SIZE * factor) {
                log.debug("Resize resampler");
                outputFloatBuffer = new float[channels][(int) (CHUNK_SIZE * factor)];
            }
        }
    }
    
    @Override
    public  void writeFrame(float[][] frameValues, FilterFlow filterFlow) {
        synchronized (FACTOR_LOCK) {
            synchronized (CONTINUOUS_STREAM) {
                if (isStreamHead) {
                    nextOffset = 0;
                    nextLength = (int) ((CHUNK_SIZE - overlap / 2) * factor);
                    isStreamHead = false;
                } else {
                    nextOffset = (int) (overlap * factor / 2);
                    nextLength = (int) ((CHUNK_SIZE - overlap) * factor);
                }
                
                
            }
               
            for (int c = 0; c < frameValues.length; c++) {
                resampler.process(factor, frameValues[c], 0, frameValues[c].length, false, outputFloatBuffer[c], 0, (int) (CHUNK_SIZE * factor));
            }
        
        }

        

        

        filterFlow.next(outputFloatBuffer, nextOffset, nextLength);

    }

    @Override
    public void init(FilterConfig config) {
        config.setChunkSize(CHUNK_SIZE);        
        config.setOverlap(overlap);
        config.setThreadName(threadName);
        isStreamHead = true;
    }

    @Override
    public  void onLineChanged() {
        log.debug("line change event received");
        synchronized (CONTINUOUS_STREAM) {
            isStreamHead = true;
        }
    }

}
