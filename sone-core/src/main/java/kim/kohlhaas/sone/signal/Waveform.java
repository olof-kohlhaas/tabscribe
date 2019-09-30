package kim.kohlhaas.sone.signal;

import java.io.IOException;

public class Waveform {
    
    private FloatAudioSignal signal;
    private double samplesPerPixel;
    private double virtualWidth;
    private float[] interval;
    private final Object INTERVAL_SIZE = new Object();
    
    public Waveform (FloatAudioSignal signal) {
        setSignal(signal);
        setSamplesPerPixel(1.0);
        virtualWidth = signal.getFrameLength();
    }
    
    public float getMin(int x) {
        return signal.getMin((int) Math.ceil(x * samplesPerPixel), getIntervalLength());
    }
    
    public float getMax(int x) {
        return signal.getMax((int) Math.ceil(x * samplesPerPixel), getIntervalLength());
    }
    
    public float getMin(int channel, int x) {
        return signal.getMin(channel, (int) Math.ceil(x * samplesPerPixel), getIntervalLength());
    }
    
    public float getMax(int channel, int x) {
        return signal.getMax(channel, (int) Math.ceil(x * samplesPerPixel), getIntervalLength());
    }
    
    public double getRootMeanSquare(int x, boolean isPositive) {
        return getRootMeanSquare(-1, x, isPositive);
    }
    
   
    public double getRootMeanSquare(int channel, int x, boolean isPositive) {
        int fromChannel; // inclusive
        int toChannel; // exclusive
        double squareSum = 0.0;
        int n = 0;
        
        if (channel == -1) {
            fromChannel = 0;
            toChannel = signal.getChannels();
        } else {
            fromChannel = channel;
            toChannel = channel + 1;
        }
        
        for (int c = fromChannel; c < toChannel; c++) {
            signal.copyFrames(c, (int) Math.ceil(x * samplesPerPixel), getIntervalLength(), interval, 0);
            for (int i = 0; i < interval.length; i++) {
                // if and else-if look the same but are actually different
                if (isPositive && interval[i] >= 0) {
                    squareSum += Math.pow(interval[i], 2); // sum up and square positive and zero samples
                    n++;
                } else if (!isPositive && interval[i] <= 0) {
                    squareSum += Math.pow(interval[i], 2); // sum up and square negative and zero samples
                    n++;
                }
            }

        }
        
        if (n == 0) {
            return 0.0;
        } else {
            if (isPositive) {
                return Math.sqrt(squareSum / n);
            } else {
                return Math.sqrt(squareSum / n) * (-1);
            }
        }

    }
    
    public double fitToWidth(double width) {
        setSamplesPerPixel(signal.getFrameLength() / width);
        virtualWidth = width;
        return samplesPerPixel;
    }
    
    public double getWidth() {
        return signal.getFrameLength() / samplesPerPixel;
    }
    
    public FloatAudioSignal getSignal() {
        return signal;
    }

    public void setSignal(FloatAudioSignal signal) {
        this.signal = signal;
    }

    public double getSamplesPerPixel() {
        return samplesPerPixel;
    }

    public void setSamplesPerPixel(double samplesPerPixel) {
        this.samplesPerPixel = samplesPerPixel;
        interval = new float[getIntervalLength()];
    }
    
    private int getIntervalLength() {
        return (int) Math.ceil(getSamplesPerPixel());
    }
    
    
}
