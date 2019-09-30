package kim.kohlhaas.sone.util;

import javax.sound.sampled.AudioFormat;

import kim.kohlhaas.sone.signal.AudioSignal;

public class PCMUtils {
    
    public static long getFrameCount(AudioFormat format, float milliseconds) {
        return getFrameCount(format.getFrameRate(), milliseconds);
    }
    
    public static long getFrameCount(float frameRate, float milliseconds) {
        return Math.round(frameRate * milliseconds / 1000);
    }
    
    public static long getByteCount(AudioFormat format, float milliseconds) {
        return getByteCount(format, getFrameCount(format, milliseconds));
    }
    
    public static long getByteCount(AudioFormat format, long frameCount) {
        return getByteCount(format.getChannels() * (format.getSampleSizeInBits() / 8), frameCount);
    }
    
    public static long getByteCount(int frameSize, long frameCount) {
        return frameSize * frameCount;
    }
    
    public static double getMilliseconds(AudioSignal signal) {
        return getMilliseconds(signal.getFrameLength(), signal.getFrameRate());
    }
    
    public static double getMilliseconds(long frameLength, float frameRate) {
        return (((double) frameLength / frameRate) * 1000.0);
    }
    
    public static long getMicroseconds(AudioSignal signal) {
        return getMicroseconds(signal.getFrameLength(), signal.getFrameRate());
    }
    
    public static long getMicroseconds(long frameLength, float frameRate) {
        return (long) ((frameLength / frameRate) * 1000000);
    }
    
}
