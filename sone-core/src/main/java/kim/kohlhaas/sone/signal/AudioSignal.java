package kim.kohlhaas.sone.signal;

/**
 * 
 * Represents an audio signal with a certain frame rate, number of frames and channels.
 *
 */

public interface AudioSignal {
    
    /**
     * Obtains the number of channels.
     * 
     * @return the number of bytes per frame
     * @see javax.sound.sampled.AudioFormat#getChannels()
     */
    public int getChannels();
    
    /**
     * Obtains the frame rate in frames per second.
     * 
     * @return the number of frames per second
     * @see javax.sound.sampled.AudioFormat#getFrameRate()
     */
    public float getFrameRate();
    
    /**
     * Obtains the length of the stream, expressed in sample frames.
     * 
     * @return the length in sample frames
     * @see javax.sound.sampled.AudioInputStream#getFrameLength()
     */
    public long getFrameLength();
    
   	public double getMillisecondDuration();
    
    public int getPastMillisSinceSourceInit();
    
}
