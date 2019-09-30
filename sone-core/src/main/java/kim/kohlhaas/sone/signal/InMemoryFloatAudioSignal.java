package kim.kohlhaas.sone.signal;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.tarsos.transcoder.ffmpeg.EncoderException;
import kim.kohlhaas.sone.file.AudioFileAccess;
import kim.kohlhaas.sone.file.FormatConverter;
import kim.kohlhaas.sone.file.TarsosFFmpegConverter;
import kim.kohlhaas.sone.util.PCMUtils;

public class InMemoryFloatAudioSignal implements FloatAudioSignal {
    
    final static Logger log = LoggerFactory.getLogger(InMemoryFloatAudioSignal.class);
    
    private static float FRAME_CHUNK_MILLISECS = 1000.0f;
    
    private float[][] signal;
    private IOException ioException = null;
    private UnsupportedAudioFileException unsupportedException = null;
    private FormatConverter converter;
    private BufferedFloatStreamReader floatReader;
        
    public InMemoryFloatAudioSignal(File audioFile) throws IOException, UnsupportedAudioFileException {
        this(AudioFileAccess.getInstance(audioFile));
    }
    
    public InMemoryFloatAudioSignal(FormatConverter converter) throws UnsupportedAudioFileException, IOException {
        this.converter = converter;     

        this.floatReader = new BufferedFloatStreamReader(converter);

        
        if (getFrameLength() > Integer.MAX_VALUE) {
            throw new UnsupportedAudioFileException("Audio file too long for in-memory access. "
                    + "Try another implementation of " + FloatAudioSignal.class.getName());
        }
        
        signal = new float[getChannels()][(int) getFrameLength()];
        
        //TODO thread nicht im konstruktor aufrufen, sondern nach initialisierung
        Thread thread = new Thread(() -> {

        	
            int chunkFrameCount = (int) PCMUtils.getFrameCount(converter.getTargetFormat(), FRAME_CHUNK_MILLISECS);


            int framesCopied = 0;
        	float[][] convert = new float[getChannels()][chunkFrameCount];
        	
            
            synchronized (signal) {
            	for (long f = 0; f < getFrameLength(); f+=chunkFrameCount) {
            		for (int c = 0; c < getChannels(); c++) {
            			try {
							framesCopied = floatReader.copyFrames(c, f, chunkFrameCount, convert[c], 0);
						} catch (IOException e) {
							ioException = e;
						} catch (UnsupportedAudioFileException e) {
							unsupportedException = e;
						}
            			for (int s = 0; s < framesCopied; s++) {
            				signal[c][(int)(f+s)] = convert[c][s];
                        }
            		} 
            	}
            }
            
            floatReader.close();
        });
        thread.setName("InMemoryFloatSignalReader");
        thread.start();
        log.info("loading audio signal...");
    }

    @Override
    public int getChannels() {
        return converter.getTargetFormat().getChannels();
    }

    @Override
    public long getFrameLength() {
        return converter.getFrameLength();
    }

    @Override
    public float getFrameRate() {
        return converter.getFrameRate();
    }
    
    @Override
    public int copyFrames(int channel, long framePosition, int frameCount, float[] dest, int destOffset) {
        throwPendingExceptions();
        
        synchronized (signal) {
            if (framePosition < 0) {
                framePosition = 0;
            }
            
            if (framePosition + frameCount > getFrameLength()) {
                Arrays.fill(dest, destOffset, dest.length,  0.0f);
                if (framePosition >= getFrameLength()) {
                    frameCount = 0;
                } else {
                    frameCount -= framePosition + frameCount - getFrameLength();
                }
                
            }
            
            if (frameCount > 0) {
                System.arraycopy(signal[channel], (int) framePosition, dest, destOffset, frameCount);
            }
        }
        
        return frameCount;
    }

    @Override
    public float getMin(int framePosition, int length) {
        float min = 0.0f;
        
        for (int i = 0; i < getChannels(); i++) {
            min = Math.min(min, getMin(i, framePosition, length));
        }     
        
        return min;
    }
    
    @Override
    public float getMin(int channel, int framePosition, int length) {
        float min = 0.0f;
        
        throwPendingExceptions();
        
        for (int i = 0; i < length && (framePosition + i) < signal[channel].length; i++) {
            min = Math.min(min, signal[channel][framePosition + i]);
        }
        
        return min;
    }
    
    @Override
    public float getMax(int framePosition, int length) {
        float max = 0.0f;
        
        for (int i = 0; i < getChannels(); i++) {
            max = Math.max(max, getMax(i, framePosition, length));
        }     

        return max;
    }
    
    @Override
    public float getMax(int channel, int framePosition, int length) {
        float max = 0.0f;
        
        throwPendingExceptions();
        
        for (int i = 0; i < length && (framePosition + i) < signal[channel].length; i++) {
            max = Math.max(max, signal[channel][framePosition + i]);
        }
        
        return max;
    }
    
    @Override
    public AudioFormat getFormat() {
        return converter.getTargetFormat();
    }
    
    private void throwPendingExceptions() {
        if (ioException != null) {
            throw new RuntimeException(ioException);
        }
        
        if (unsupportedException != null) {
            throw new RuntimeException(unsupportedException);
        }
    }

	@Override
	public void close() {
		floatReader.close();
	}

	@Override
	public int getPastMillisSinceSourceInit() {
		return floatReader.getPastMillisSinceSourceInit();
	}

	@Override
	public File getFile() {
		return converter.getFile();
	}

	@Override
	public double getMillisecondDuration() {
		return converter.getMillisecondDuration();
	}

}
