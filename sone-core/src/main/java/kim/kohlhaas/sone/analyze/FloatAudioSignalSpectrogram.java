package kim.kohlhaas.sone.analyze;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kim.kohlhaas.sone.signal.FloatAudioSignal;
import kim.kohlhaas.sone.util.ArrayUtils;
import kim.kohlhaas.sone.util.PCMUtils;

public class FloatAudioSignalSpectrogram extends LinearTimeFloatSpectrogram {
    
    final static Logger log = LoggerFactory.getLogger(FloatAudioSignalSpectrogram.class);
    private static final int MAX_FREQS = 1024;
    private float[][][] grid;
    private FloatAudioSignal floatAudioSignal;
    private float[][] calcChannelSamples;
    private float max;
    private int samplesPerTimeStep;
    private int freqs; 
    private int timeSteps;
    private double milliseconds;
    private FFTAnalyzer analyzer;
    private FFTAnalyzer.Resolution spectrumWidth;
    
    public FloatAudioSignalSpectrogram() {        
        try {
            setFFTLevel(FFTAnalyzer.Resolution.FRQ_512);
        } catch (IOException e) {
            log.error("At construction time there should no signal been set to throw this exception: {}",  e);            
        }
        
        analyzer = FFTAnalyzer.getInstance();
    }
    
    public void setFFTLevel(FFTAnalyzer.Resolution spectrumWidth) throws IOException {
        this.spectrumWidth = spectrumWidth;
        samplesPerTimeStep = spectrumWidth.getValue();
        freqs = samplesPerTimeStep / 2;
        if (freqs > MAX_FREQS) {
            freqs = MAX_FREQS;
        }
        resetTimeSteps();
        
        if (floatAudioSignal != null) {
            scan();
        }
    }
    
    public FFTAnalyzer.Resolution getFFTLevel() {
        return spectrumWidth;
    }

    @Override
    public int getChannels() {
        if (floatAudioSignal != null) {
            return floatAudioSignal.getChannels();
        } else {
            return 0;
        }
    }
    
    @Override
    public int getFreqs() {
        return freqs;
    }
    
    @Override
    public int getTimeSteps() {
        return timeSteps;
    }

    @Override
    public int getSamplesPerTimeStep() {
        return samplesPerTimeStep;
    }
    
    @Override
    public double getMilliseconds() {
        return milliseconds;
    }
    
    @Override
    public float getMaxAmp() {
        return max;
    }
    
    @Override
    public float getMaxAmp(int channel) {
        return ArrayUtils.getMax(grid[channel]);
    }
        
    @Override
    public float getMaxAmpInFreq(int channel, int freqIndex) {
        return ArrayUtils.getMax(grid[channel], freqIndex);
    }
    
    @Override
    public float getMaxAmpInTime(int channel, int timeIndex) {
        return ArrayUtils.getMax(grid[channel][timeIndex]);
    }
    
    @Override
    public float getAmp(int timeIndex, int freqIndex) {
        float result = 0.0f;
        for (int c = 0; c < floatAudioSignal.getChannels(); c++) {
            result += grid[c][timeIndex][freqIndex];
        }
        return result / floatAudioSignal.getChannels();
    }
    
    @Override
    public float getAmp(int channel, int timeIndex, int freqIndex) {
        return grid[channel][timeIndex][freqIndex];
    }
    
    
    @Override
    public void copySpectrum(int channel, int timeIndex, float[] dest) {
        System.arraycopy(grid[channel][timeIndex], 0, dest, 0, getFreqs());
    }

    @Override
    public void copySpectrum(int channel, int from, int to, float[][] dest) {
        for (int t = from; t < grid[channel].length && t <= to; t++) {
            System.arraycopy(grid[channel][t], 0, dest, 0, getFreqs());
        }
    }
    
    @Override
    public double getFreq(int freqIndex) {
    	if (floatAudioSignal != null) {
        return ((double) freqIndex / getSamplesPerTimeStep()) / 2.0 * floatAudioSignal.getFrameRate();
    	} else {
    		return 0.0;
    	}
    }
    
    @Override
    public FloatAudioSignal getFloatAudioSignal() {
        return this.floatAudioSignal;
    }
    
    @Override
    public void setFloatAudioSignal(FloatAudioSignal floatAudioSignal) throws IOException {
        this.floatAudioSignal = floatAudioSignal;
        milliseconds = PCMUtils.getMilliseconds(floatAudioSignal);
        resetTimeSteps();
        max = 0;
        scan();
    }
    
    private void resetTimeSteps() {
        if (floatAudioSignal != null) {
            timeSteps = (int) Math.ceil((double) floatAudioSignal.getFrameLength() / getSamplesPerTimeStep());
        }
    }
    
    private void scan() throws IOException {
        int resultIndex;

        calcChannelSamples = new float[floatAudioSignal.getChannels()][samplesPerTimeStep * 2];
        grid = new float[floatAudioSignal.getChannels()][getTimeSteps()][freqs];
        
        for (int c = 0; c < floatAudioSignal.getChannels(); c++) {
            resultIndex = 0;
            for (int i = 0; i < floatAudioSignal.getFrameLength(); i += samplesPerTimeStep) {
                
                calcChannelSamples[c] = analyzer.getSpectrum(spectrumWidth, floatAudioSignal, c, i, false);

                for (int f = 0; f < calcChannelSamples[c].length && f < freqs; f++) {
                    grid[c][resultIndex][f] = calcChannelSamples[c][f];
                    if (grid[c][resultIndex][f] > max) {
                        max = grid[c][resultIndex][f];
                    }
                }
                
                resultIndex++;
            }
        }       
    }

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	} 
    

}
