package kim.kohlhaas.sone.signal;

import java.io.IOException;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import kim.kohlhaas.sone.file.FormatConverter;
import kim.kohlhaas.sone.util.PCMUtils;

public class BufferedFloatStreamReader {
	
	private boolean wasTooLess = false;
	private int a = 0;
	private long totalFrames = 0L;
    
    private long headFrame;
    private int headBufferIndex;
    private int writeBufferIndex;
    private int copyStartIndex;

    private int framesReady;
    private long framesToRead;
    private long framesToSkip;
    private int bytesToRead;
    private long bytesToSkip; 
    
    private byte[] chunk;
    private float[][] chunkConverted;
    int bytesRead;
    int bytesSkipped;
    int framesConverted;
    int bufferWriteIndex;
    
    int sliceCount;
    
    private float[][] buffer;
    private final int bufferFrames;
    private final int CHUNK_FRAMES = 1000;
    
    private AudioInputStream inputStream;
    private AudioFormat format;
    private FormatConverter converter;
    private long millisAtSourceInit = System.currentTimeMillis();
    private int channels;
    private long frameLength;
    
    PCMByteFloatConverter byteConverter;
    
    public BufferedFloatStreamReader(FormatConverter converter, int bufferFrames) throws UnsupportedAudioFileException, IOException {
    	this.bufferFrames = bufferFrames;
    	this.converter = converter;
    	float frameRate;
    	
    	millisAtSourceInit = System.currentTimeMillis();
    	inputStream = converter.createInputStream();
    	millisAtSourceInit = System.currentTimeMillis();
    	
    	format = inputStream.getFormat();	
    	
    	if ((frameRate = format.getFrameRate()) <= 0) {
            frameRate = format.getSampleRate();
        }
    	
    	channels = format.getChannels();
    	frameLength = converter.getFrameLength();
    	    	
        headFrame = -1L;
        headBufferIndex = -1;
                
        chunk = new byte[(int) PCMUtils.getByteCount(format, CHUNK_FRAMES)];
        chunkConverted = new float[channels][CHUNK_FRAMES];
        bytesRead = 0;
        framesConverted = 0;

        buffer = new float[channels][bufferFrames];
        for (int i = 0; i < channels; i++) {
        	Arrays.fill(buffer[i], 0.0f);
        }
        
        byteConverter = new PCMByteFloatConverter(format);

        fill(bufferFrames - 1);
    }

    public BufferedFloatStreamReader(FormatConverter converter) throws UnsupportedAudioFileException, IOException {
    	this(converter, 300000); // 100000 approx. correspond 2 sec.
    } 
    
    private synchronized void fill(long targetFrame) throws IOException, UnsupportedAudioFileException {
    	totalFrames = 0L;
    	if (targetFrame > frameLength) {
    		targetFrame = frameLength - 1;
    	}
    	
    	if (targetFrame < headFrame) {
    		inputStream.close();
    		millisAtSourceInit = System.currentTimeMillis();
			inputStream = converter.createInputStream();
			millisAtSourceInit = System.currentTimeMillis();
			headFrame = -1L;
	        headBufferIndex = -1;
	        bytesRead = 0;
	        framesConverted = 0;
	        for (int i = 0; i < channels; i++) {
	        	Arrays.fill(buffer[i], 0.0f);
	        }
    	}
    	
		framesToRead = targetFrame - headFrame; // with initial headFrame of -1 in mind
				
		framesToSkip = targetFrame - bufferFrames - headFrame;
		bytesToSkip = PCMUtils.getByteCount(format, framesToSkip);
		bytesSkipped = 0;
		while (bytesToSkip > 0) {			
			bytesSkipped = (int) inputStream.skip(bytesToSkip);
			bytesToSkip -= bytesSkipped;
		}	
		
		if (framesToRead >= bufferFrames) {
			framesToRead = bufferFrames;
			headBufferIndex = bufferFrames - 1;
			writeBufferIndex = 0;	
		} else if (framesToRead > 0 && framesToRead < bufferFrames) {
			framesToSkip = 0;
			writeBufferIndex = (headBufferIndex + 1) % bufferFrames;
			headBufferIndex = (int) ((headBufferIndex + framesToRead) % bufferFrames); // ringbuffer
		} else if (framesToRead < 0) {
			throw new IOException("Target frame ahead of head frame");			
		} else {
			return;
		}
		
		headFrame = targetFrame;
		bytesRead = 0;

		while (framesToRead > 0) {
			if (framesToRead >= CHUNK_FRAMES) {
				bytesToRead = (int) PCMUtils.getByteCount(format, CHUNK_FRAMES);
			} else {
				bytesToRead = (int) PCMUtils.getByteCount(format, framesToRead);
			}	
			
			bytesRead = inputStream.read(chunk, 0, bytesToRead);
			if (bytesRead != -1) {
	            framesConverted = byteConverter.convert(chunk, 0, bytesRead, chunkConverted);
	            framesToRead -= framesConverted;
	            totalFrames += framesConverted;
	            for (int f = 0; f < framesConverted; f++) {
	                for (int c = 0; c < channels; c++) {
	                    buffer[c][writeBufferIndex] = chunkConverted[c][f];
	                }
	                writeBufferIndex = (writeBufferIndex + 1) % bufferFrames;
	            }
			} else {
				inputStream.close();
			}
		}		

    }
    
    protected synchronized int copyFrames(int channel, long framePosition, int frameCount, float[] dest, int destOffset) throws IOException, UnsupportedAudioFileException {
    	if (framePosition < 0) {
            framePosition = 0;
        }
    	
    	//TODO nice-to-have: possibility to fill dest array with frameCount greater than one buffer by looping
    	if (frameCount > bufferFrames) {
    		throw new RuntimeException("copy frames greater than buffer frames.");
    	}
        
        if (framePosition + frameCount > frameLength) {
            Arrays.fill(dest, destOffset, dest.length,  0.0f);
            if (framePosition >= frameLength) {
                frameCount = 0;
            } else {
                frameCount -= framePosition + frameCount - frameLength;
            }
            
        }
        
        if (frameCount > 0) {
        	if (!(framePosition + frameCount - 1 <= headFrame && framePosition >= headFrame - bufferFrames - 1)) {
				fill(framePosition + frameCount - 1);	
        	}

        	copyStartIndex = headBufferIndex - (int)(headFrame - framePosition);

        	if (copyStartIndex >= 0) {
        		System.arraycopy(buffer[channel], copyStartIndex, dest, destOffset, frameCount);
        	} else {
        		if (frameCount > (sliceCount = copyStartIndex * (-1))) {        		
        			System.arraycopy(buffer[channel], bufferFrames + copyStartIndex, dest, destOffset, sliceCount);
        		} else {
        			System.arraycopy(buffer[channel], bufferFrames + copyStartIndex, dest, destOffset, frameCount);	
        		}
        		
        		if ((sliceCount = frameCount + copyStartIndex) > 0) {
        			System.arraycopy(buffer[channel], 0, dest, destOffset - copyStartIndex, sliceCount);
        		}
        	}
        	
        }
        
        return frameCount;
    }
    
    protected void close() {

    	try {
    		chunk = null;
    		chunkConverted = null;
    		buffer = null;
			inputStream.close();
			converter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public int getPastMillisSinceSourceInit() {
    	return   (int) (System.currentTimeMillis() - millisAtSourceInit);
    }
    
    
    
}
