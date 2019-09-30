package kim.kohlhaas.sone.signal;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kim.kohlhaas.sone.analyze.FFTAnalyzer;
import kim.kohlhaas.sone.file.AudioFileAccess;
import kim.kohlhaas.sone.file.FormatConverter;
import kim.kohlhaas.sone.util.PCMUtils;

public class BufferedFloatAudioSignal implements FloatAudioSignal {
	
	static final Logger log = LoggerFactory.getLogger(BufferedFloatAudioSignal.class);
          
    private IOException ioException = null;
    private UnsupportedAudioFileException unsupportedException = null;
    private FormatConverter converter;
    private BufferedFloatStreamReader floatReader;
    private float[] chunk;
    
    public BufferedFloatAudioSignal(File audioFile, int bufferFrames) throws IOException, UnsupportedAudioFileException {
        this(AudioFileAccess.getInstance(audioFile), bufferFrames);
    }
    
    public BufferedFloatAudioSignal(FormatConverter converter, int bufferFrames) throws UnsupportedAudioFileException, IOException {
        this.converter = converter;     

        this.floatReader = new BufferedFloatStreamReader(converter, bufferFrames);
        this.chunk = new float[FFTAnalyzer.Resolution.FRQ_4096.getValue()]; // TODO muss kleiner  sein  als bufferFrames, übergebene bufferFrames aus minimum der tiefsten frequenz des tonesets berechnen
    }

    public BufferedFloatAudioSignal(File audioFile) throws IOException, UnsupportedAudioFileException {
        this(AudioFileAccess.getInstance(audioFile));
    }
    
    public BufferedFloatAudioSignal(FormatConverter converter) throws UnsupportedAudioFileException, IOException {
        this.converter = converter;     

        this.floatReader = new BufferedFloatStreamReader(converter);
        this.chunk = new float[FFTAnalyzer.Resolution.FRQ_4096.getValue()];
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

        int framesCopied;
		try {
			framesCopied = floatReader.copyFrames(channel, framePosition, frameCount, dest, destOffset);
		} catch (IOException | UnsupportedAudioFileException e) {
			throw new RuntimeException(e);
		}
        return framesCopied;        
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
        fillChunk(channel, framePosition, length);
        
        for (int i = 0; i < length; i++) {
            min = Math.min(min, chunk[i]);
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
        fillChunk(channel, framePosition, length);
        
        for (int i = 0; i < length; i++) {
            max = Math.max(max, chunk[i]);
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
    
    private void fillChunk(int channel, int framePosition, int length) {
    	int size = chunk.length;
    	if (size < length) {
    		chunk = new float[length];
    	}

    	copyFrames(channel, framePosition, length, chunk, 0);
    }

	@Override
	public void close() {
		chunk = null;
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
