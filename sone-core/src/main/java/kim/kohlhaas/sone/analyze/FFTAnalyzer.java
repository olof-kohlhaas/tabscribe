package kim.kohlhaas.sone.analyze;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kim.kohlhaas.sone.signal.FloatAudioSignal;
import kim.kohlhaas.sone.util.MathUtils;
import kj.dsp.KJFFT;

public class FFTAnalyzer {
    
    public enum Resolution {
        
        FRQ_2(2),
        FRQ_4(4),
        FRQ_8(8),
        FRQ_16(16),
        FRQ_32(32),
        FRQ_64(64),
        FRQ_128(128),
        FRQ_256(256),
        FRQ_512(512),
        FRQ_1024(1024),
        FRQ_2048(2048),
        FRQ_4096(4096),
        FRQ_8192(8192);
        
        private final int value;
               
        private Resolution(final int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
        
    }
    
    final static Logger log = LoggerFactory.getLogger(FFTAnalyzer.class);
    
    private static final HashMap<Thread, FFTAnalyzer> INSTANCES = new HashMap<Thread, FFTAnalyzer>();
    
    private Resolution spectrumWidth;
    private KJFFT kjfft;
    private float[] phaseCarrier;
    private float[] phaseResult;
    private float[] phaseCarrierVirtual;
    
    private int frameCount;
    private int destOffset;
    
    private FFTAnalyzer() {

    }
    
    /**
     * Returns a thread unique singleton instance. Each thread has its own singleton to avoid blocking of rapidly 
     * called synchronized methods while every instance can reuse arrays. 
     * 
     * @return the thread unique singleton instance
     */
    public static FFTAnalyzer getInstance() {
        Thread thread = Thread.currentThread();
        FFTAnalyzer instance = INSTANCES.get(thread);
        
        if (instance == null) {
            instance = new FFTAnalyzer();
            INSTANCES.put(thread, instance);
            // TODO check whether there are not reused threads which remain only in this hash map
            log.info("Maybe implement zombie thread cleaner, if this value increases: {}", INSTANCES.size());
        }
        
        return instance;
    }
    
    public static void clear() {
        INSTANCES.clear();
    }
    
    public static double getFrequencyOfIndex(Resolution resolution, FloatAudioSignal signal, int index) {
        return ((double) index / resolution.getValue()) / 2.0 * signal.getFrameRate();
    }
    
    public static double getFrequencyOfIndex(Resolution resolution, FloatAudioSignal signal, int index, double indexOneFreq) {
        return ((double) index / resolution.getValue()) / 2.0 * (signal.getFrameRate() / getVirtualTimeIterator(resolution, signal, indexOneFreq));
    }
    
    public static Resolution getMinimumResolution(double frequency, FloatAudioSignal signal) {        
        double requiredFFTResolution = signal.getFrameRate() / frequency;
        double binaryExponent = Math.ceil(MathUtils.lb(requiredFFTResolution));
        int requiredPowerOfTwoResolution = (int) Math.pow(2.0, binaryExponent);
        
        
        return Resolution.valueOf("FRQ_" + requiredPowerOfTwoResolution);
    }
    
    public static int getSampleCount(Resolution resolution) {
    	return resolution.getValue() * 2;
    }
    
    private static double getVirtualTimeIterator(Resolution resolution, FloatAudioSignal signal, double indexOneFreq) {
        return signal.getFrameRate() / (indexOneFreq * resolution.getValue());
    }
    
    public float[] getSpectrum(Resolution spectrumWidth, FloatAudioSignal signal, int channel,
            int upFromFramePosition, boolean normalized) throws IOException {
        return getSpectrum(spectrumWidth, signal, channel, upFromFramePosition, normalized, -1.0);
    }
    
    public float[] getSpectrum(Resolution spectrumWidth, FloatAudioSignal signal, int channel,
            int upFromFramePosition, boolean normalized, double indexOneFreq) throws IOException {
        double virtualIndex = 0.0;
        double virtualIterator;
        int firstIndex;
        int secondIndex;
        float firstVal;
        float secondVal;
        float diff;
        double firstPart;
        double secondPart;       
        
        
        if (this.spectrumWidth == null || spectrumWidth.getValue() != this.spectrumWidth.getValue()) {
            log.debug("resizing spectrum analyzer {}", Thread.currentThread().getName());
            this.spectrumWidth = spectrumWidth;
            
            phaseCarrier = new float[spectrumWidth.getValue() * 2];
            phaseCarrierVirtual = new float[spectrumWidth.getValue() * 2];
            kjfft = new KJFFT(spectrumWidth.getValue() * 2);
        }
        
        
        
        frameCount = spectrumWidth.getValue() * 2;
        
        destOffset = 0;
        
        if (upFromFramePosition < 0 ) {
            Arrays.fill(phaseCarrier, 0.0f);
            if (Math.abs(upFromFramePosition) < frameCount) {
                frameCount += upFromFramePosition; // subtract (negative) frame position
                destOffset = (int) upFromFramePosition * (-1);
                upFromFramePosition = 0;
            } else {
                frameCount = 0;
            }
        } else if (upFromFramePosition + frameCount > signal.getFrameLength()) {
            Arrays.fill(phaseCarrier, 0.0f);
            frameCount -= upFromFramePosition + frameCount - signal.getFrameLength();
            if (upFromFramePosition > signal.getFrameLength()) {
                frameCount = 0;
            }
        }
        
        if (frameCount > 0) {
            signal.copyFrames(channel, upFromFramePosition, frameCount, phaseCarrier, destOffset);
        }
        
        if (indexOneFreq < 0.0) {
            for (int i = 0; i < phaseCarrier.length; i++) {
                phaseCarrierVirtual[i] = phaseCarrier[i];
            }
        } else {
            virtualIterator = getVirtualTimeIterator(spectrumWidth, signal, indexOneFreq);
            if (virtualIterator > 1.0) {
                throw new RuntimeException("SpectrumWidth too small for given indexOneFreq");
            }
            
            for (int i = 0; i < phaseCarrier.length; i++) {
                firstIndex = (int) Math.floor(virtualIndex);
                secondIndex = (int) Math.ceil(virtualIndex);
                firstVal = phaseCarrier[firstIndex];
                secondVal = phaseCarrier[secondIndex];
                diff = secondVal - firstVal;
                firstPart = virtualIndex - firstIndex;
                secondPart = 1.0 - firstPart;
                phaseCarrierVirtual[i] = (float)(firstVal + diff * firstPart);
                virtualIndex += virtualIterator;
            }
        }
        
        phaseResult = kjfft.calculate(phaseCarrierVirtual);
        
        return phaseResult;
    }
    
    public float[] getSpectrum(Resolution spectrumWidth, FloatAudioSignal signal, int channel,
            double upFromMillisecond, boolean normalized) throws IOException {

        if (upFromMillisecond >= 0 ) { 
            return getSpectrum(spectrumWidth, signal, channel, (int) Math.ceil(signal.getFrameRate() * upFromMillisecond / 1000.0), normalized);
        } else {
            return getSpectrum(spectrumWidth, signal, channel, (int) Math.floor(signal.getFrameRate() * upFromMillisecond / 1000.0), normalized);
        }

    } 
}
