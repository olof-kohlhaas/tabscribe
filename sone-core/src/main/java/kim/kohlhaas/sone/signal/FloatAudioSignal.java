package kim.kohlhaas.sone.signal;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;

import java.io.File;

/**
 * 
 * Represents an audio signal as a float sequence normalized to less or equal to 1.0 and greater or equal to -1.0.
 *
 */
public interface FloatAudioSignal extends AudioSignal{

    /**
     * Copies float values of a certain channel of the audio signal into the passed array.
     * 
     * @param channel the channel the frames are copied from.
     * @param framePosition the position where to start copying.
     * @param frameCount the number of frames to be copied.
     * @param dest the destination array the frames are copied to.
     * @param destOffset the starting position in the destination array where the frames are copied to.
     * @return the number of frames actually copied.
     */
    public int copyFrames(int channel, long framePosition, int frameCount, float[] dest, int destOffset);
    
    public float getMin(int framePosition, int length);
    
    public float getMin(int channel, int framePosition, int length);
    
    public float getMax(int framePosition, int length);
    
    public float getMax(int channel, int framePosition, int length);
    
    public AudioFormat getFormat();
    
    public File getFile();
    
    public void close();
    
}
