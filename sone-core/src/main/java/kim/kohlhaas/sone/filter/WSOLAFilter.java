package kim.kohlhaas.sone.filter;

import java.util.ArrayList;
import java.util.LinkedList;

import javax.sound.sampled.AudioFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kim.kohlhaas.sone.filter.FilterProcess.FilterConfig;

public class WSOLAFilter implements ManagedFilter {
	
	final static Logger log = LoggerFactory.getLogger(WSOLAFilter.class);
    
    private int seekWindowLength;
    private int seekLength;
    private int overlapLength;
    
    private float[][] pMidBuffer; 
    private float[][] pRefMidBuffer;
    private float[][] outputDoubleBuffer;   

    
    private int intskip;
    private int sampleReq;
    
    private double overlapMs;
    private double sequenceMs;
    private double seekWindowMs;
    
    private double tempo;
    
    private AudioFormat format;
    private FilterConfig config;
    private String threadName;
    
    
    public WSOLAFilter(String threadName, AudioFormat format, double overlapMs, double sequenceMs, double seekWindowMs, double tempo) {
        this.format = format;
        this.overlapMs = overlapMs;
        this.sequenceMs = sequenceMs;
        this.seekWindowMs = seekWindowMs;
        this.tempo = tempo;
        this.threadName = threadName;
        

        
        applyNewParameters();
    }

    public void setFormat(AudioFormat format) {
        this.format = format;
        applyNewParameters();
    }
    
    private void applyNewParameters(){
        int oldOverlapLength = overlapLength;
        overlapLength = (int) ((format.getSampleRate() * overlapMs) / 1000);
        seekWindowLength = (int) ((format.getSampleRate() * sequenceMs) / 1000);
        seekLength = (int) ((format.getSampleRate() *  seekWindowMs) / 1000);
        
        //pMidBuffer and pRefBuffer are initialized with 8 times the needed length to prevent a reset
        //of the arrays when overlapLength changes.
        
        if(overlapLength > oldOverlapLength * 8 && pMidBuffer==null){
            pMidBuffer = new float[format.getChannels()][overlapLength * 8]; //overlapLengthx2?
            pRefMidBuffer = new float[format.getChannels()][overlapLength * 8];//overlapLengthx2?
            
                log.debug("New overlap length: {}", overlapLength);
        }
        
        log.debug("tempo: {} ", tempo);
        
        double nominalSkip = tempo * (seekWindowLength - overlapLength);
        intskip = (int) (nominalSkip + 0.5);
        
        sampleReq = Math.max(intskip + overlapLength, seekWindowLength) + seekLength;
        

        
        float[][] prevOutputBuffer = outputDoubleBuffer;
        outputDoubleBuffer = new float[format.getChannels()][getOutputBufferSize()];
        
        if(prevOutputBuffer!=null){
                log.debug("Copy outputFloatBuffer contents");
                for (int c = 0; c < format.getChannels(); c++) {
                	if (prevOutputBuffer.length <  outputDoubleBuffer.length) {
                		for(int i = 0 ; i < prevOutputBuffer[0].length && i < outputDoubleBuffer[c].length ; i++) {
	                    	outputDoubleBuffer[c][i] = prevOutputBuffer[0][i];
	                    }
                	} else {
	                    for(int i = 0 ; i < prevOutputBuffer[c].length && i < outputDoubleBuffer[c].length ; i++) {
	                    	outputDoubleBuffer[c][i] = prevOutputBuffer[c][i];
	                    }
                	}
                }
        }

        
        if ( config != null) {
            config.setChunkSize(getInputBufferSize());
            config.setOverlap(getOverlap());
        }
        
        
    }
    
    public double getTempo() {
        return this.tempo;
    }
    
    public void setTempo(double tempo) {
        this.tempo = tempo;
        applyNewParameters();
        
    }
    
    public int getInputBufferSize() {
        return sampleReq;
    }
    
    private int getOutputBufferSize() {
        log.debug("seekWindowLength - overlapLength: {} ", (seekWindowLength - overlapLength));
        return seekWindowLength - overlapLength;
    }
    
    public int getOverlap(){
        return sampleReq-intskip;
    }
    
    /**
     * Overlaps the sample in output with the samples in input.
     * @param output The output buffer.
     * @param input The input buffer.
     */
    private void overlap(final float[] output, int outputOffset, float[] input,int inputOffset, int channel) {
        
            for(int i = 0 ; i < overlapLength ; i++){
                    int itemp = overlapLength - i;
                    output[i + outputOffset] = (input[i + inputOffset] * i + pMidBuffer[channel][i] * itemp ) / overlapLength;  
            }
    }
    
    /**
     * Seeks for the optimal overlap-mixing position.
     * 
     * The best position is determined as the position where the two overlapped
     * sample sequences are 'most alike', in terms of the highest
     * cross-correlation value over the overlapping period
     * 
     * @param inputBuffer The input buffer
     * @param postion The position where to start the seek operation, in the input buffer. 
     * @return The best position.
     */
    private int seekBestOverlapPosition(float[] inputBuffer, int postion, int channel) {
            int bestOffset;
            double bestCorrelation, currentCorrelation;
            int tempOffset;

            int comparePosition;

            // Slopes the amplitude of the 'midBuffer' samples
            precalcCorrReferenceMono();
            
           
            bestCorrelation = -10;
            bestOffset = 0;

            // Scans for the best correlation value by testing each possible
            // position
            // over the permitted range.
            for (tempOffset = 0; tempOffset < seekLength; tempOffset++) {

                    comparePosition = postion + tempOffset;

                    // Calculates correlation value for the mixing position
                    // corresponding
                    // to 'tempOffset'
                    currentCorrelation = (double) calcCrossCorr(pRefMidBuffer[channel], inputBuffer, comparePosition);
                    // heuristic rule to slightly favor values close to mid of the
                    // range
                    double tmp = (double) (2 * tempOffset - seekLength) / seekLength;
                    currentCorrelation = ((currentCorrelation + 0.1) * (1.0 - 0.25 * tmp * tmp));

                    // Checks for the highest correlation value
                    if (currentCorrelation > bestCorrelation) {
                            bestCorrelation = currentCorrelation;
                            bestOffset = tempOffset;
                    }
            }

            return bestOffset;

    }
    
    /**
     * Slopes the amplitude of the 'midBuffer' samples so that cross correlation
     * is faster to calculate. Why is this faster?
     */
     void precalcCorrReferenceMono()
     {
         for (int c = 0; c < format.getChannels(); c++) {
         for (int i = 0; i < overlapLength; i++){
             float temp = i * (overlapLength - i);
             pRefMidBuffer[c][i] = pMidBuffer[c][i] * temp;
         }
         }
     }
     
     double calcCrossCorr(float[] mixingPos, float[] compare, int offset) {
         double corr = 0;
         double norm = 0;


         
         for (int i = 1; i < overlapLength; i ++) {
             corr += mixingPos[i] * compare[i + offset];
             norm += mixingPos[i] * mixingPos[i];
         }

         // To avoid division by zero.
         if (norm < 1e-8){
             norm = 1.0;    
         }
         return corr / Math.pow(norm,0.5);
     }
    
    @Override
    public void writeFrame(float[][] frameValues, FilterFlow filterFlow) {
        int offset;
        int sequenceLength;

        for (int c = 0; c < format.getChannels(); c++) {
            //Search for the best overlapping position.
            offset =  seekBestOverlapPosition(frameValues[c],0, c);
         // Mix the samples in the 'inputBuffer' at position of 'offset' with the 
            // samples in 'midBuffer' using sliding overlapping
            // ... first partially overlap with the end of the previous sequence
            // (that's in 'midBuffer')
            overlap(outputDoubleBuffer[c],0,frameValues[c],offset, c);
                  //copy sequence samples from input to output                       
                    sequenceLength = seekWindowLength - 2 * overlapLength;
            System.arraycopy(frameValues[c], offset + overlapLength, outputDoubleBuffer[c], overlapLength, sequenceLength);
                 
                    // Copies the end of the current sequence from 'inputBuffer' to 
                    // 'midBuffer' for being mixed with the beginning of the next 
                    // processing sequence and so on
            System.arraycopy(frameValues[c], offset + sequenceLength + overlapLength, pMidBuffer[c], 0, overlapLength);
        }

        filterFlow.next(outputDoubleBuffer);

    }

    @Override
    public void init(FilterProcess.FilterConfig config) {
        this.config = config;
        config.setChunkSize(getInputBufferSize());
        config.setOverlap(getOverlap());
        config.setThreadName(threadName);
    }

    @Override
    public void onLineChanged() {

        
    }

}
