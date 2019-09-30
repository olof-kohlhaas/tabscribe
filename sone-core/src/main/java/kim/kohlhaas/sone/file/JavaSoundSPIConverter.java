package kim.kohlhaas.sone.file;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tritonus.share.sampled.file.TAudioFileFormat;

import be.tarsos.transcoder.Attributes;
import be.tarsos.transcoder.DefaultAttributes;
import be.tarsos.transcoder.Streamer;
import be.tarsos.transcoder.ffmpeg.EncoderException;

public class JavaSoundSPIConverter implements FormatConverter {
    
    private final static Logger log = LoggerFactory.getLogger(JavaSoundSPIConverter.class);
    private final static Map<File, JavaSoundSPIConverter> instances = new HashMap<File, JavaSoundSPIConverter>();
    
    private File file;
    private AudioInputStream sourceInputStream;
    private AudioInputStream targetInputStream;
    private AudioFormat sourceFormat;
    private AudioFormat targetFormat;
    private long frameLength;
    private float frameRate;
    private double milliSecondDuration;
    
    public static FormatConverter getInstance(File file) throws IOException, UnsupportedAudioFileException {
        synchronized (file) {
            if(instances.containsKey(file)) {
                return instances.get(file);
            } else {
                JavaSoundSPIConverter converter = new JavaSoundSPIConverter(file);
                instances.put(file, converter);
                return converter;
            }
        }
    }
    
    public static void clear() {
        instances.clear();
    }
    
    private JavaSoundSPIConverter() {
        super();
        
        file = null;
        sourceInputStream = null;
        targetInputStream = null;
    }
    
    private JavaSoundSPIConverter(File file) throws IOException, UnsupportedAudioFileException {
        this();
        
        setFile(file);
    }
    
    /* (non-Javadoc)
	 * @see kim.kohlhaas.sone.file.FormatConverter#getFile()
	 */
    @Override
	public File getFile() {
        return file;
    }
    
    private FormatConverter setFile(File file) throws IOException, UnsupportedAudioFileException {
        int sampleSizeInBits;
        long byteCount = 0;
        int bytesRead;
        int frameSize;
        int total = 0;
        
        this.file = file;   
        
        log.info(file.toString());
        
        if (sourceInputStream != null) {
            sourceInputStream.close();
        }
        
        sourceInputStream = AudioSystem.getAudioInputStream(file);
        
        
        
        sourceFormat = this.sourceInputStream.getFormat(); 
        
        log.debug("bytelength: " + AudioSystem.getAudioFileFormat(file).getByteLength());
        AudioFileFormat aff = AudioSystem.getAudioFileFormat(file);
        
        String type = aff.getType().toString();
        if (type.equalsIgnoreCase("mp3")) {
        
	        if (aff instanceof TAudioFileFormat) {
	            Map props = ((TAudioFileFormat) aff).properties();
	            if (props.containsKey("mp3.length.frames")) total = (int) props.get("mp3.length.frames");
	            log.info("MP3-Meta-Data, total frames: {}", total);
	        }
    	}	
        
        if (sourceFormat.getSampleSizeInBits() == AudioSystem.NOT_SPECIFIED) {
            sampleSizeInBits = 16;
        } else {
            sampleSizeInBits = sourceFormat.getSampleSizeInBits();
        }
        
        // TODO isConversion supported
        
        targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 
                sourceFormat.getSampleRate(), 
                sampleSizeInBits, sourceFormat.getChannels(), 
                sourceFormat.getChannels() * (sampleSizeInBits / 8), 
                sourceFormat.getSampleRate(), 
                false);
        
        log.info("Source format: " + sourceFormat.toString());
        log.info("Target format: " + targetFormat.toString());
        log.debug("samplerate: {}, framerate: {}", new Float[]{targetFormat.getSampleRate(), targetFormat.getFrameRate()});
        
        targetInputStream = AudioSystem.getAudioInputStream(targetFormat, sourceInputStream);
        
        log.info("Converted format: " + targetInputStream.getFormat().toString());
        log.info("Converted format: " + targetInputStream.getFrameLength());
        
        frameLength = targetInputStream.getFrameLength();
        frameSize = targetInputStream.getFormat().getFrameSize();
        log.info("Frame length: " + frameLength);
        if (!(frameLength > 0)) {
            log.info("Frame length not declared.");
            if (!(targetInputStream.getFormat().getFrameSize() > 0)) {
                throw new IOException("Neither frame length nor frame size is declared.");
            }
            byte[] countChunk = new byte[10000];
            do {
            	// TODO replace byte array by a reused one
                bytesRead = targetInputStream.read(countChunk);
                if (bytesRead != -1) {
                    byteCount += bytesRead;
                }
            } while (bytesRead != -1);
            
            log.info("Bytes counted: " + byteCount);
            
            if (byteCount % targetInputStream.getFormat().getFrameSize() == 0) {
                frameLength = byteCount / frameSize;
                log.info("Determined frame length: " + frameLength);
            } else {            
                frameLength = -1;
                throw new IOException("Determined byte count " + byteCount + " could not partitioned by declared "
                        + "frame size " + frameSize + ".");
            }
            
        }   
                
        if ((frameRate = targetFormat.getFrameRate()) <= 0) {
            frameRate = targetFormat.getSampleRate();
        }
        
        milliSecondDuration = (frameLength * 1.00 / frameRate) * 1000;
        
        return this;
    }
    
    /* (non-Javadoc)
	 * @see kim.kohlhaas.sone.file.FormatConverter#createInputStream()
	 */
    @Override
	public AudioInputStream createInputStream() throws UnsupportedAudioFileException, IOException {
    	AudioInputStream sourceInputStream = AudioSystem.getAudioInputStream(file);

        AudioInputStream useInputStream = AudioSystem.getAudioInputStream(targetFormat, sourceInputStream);
        return useInputStream;
    }
    
    /* (non-Javadoc)
	 * @see kim.kohlhaas.sone.file.FormatConverter#getSourceFormat()
	 */
    @Override
	public AudioFormat getSourceFormat() {
        return sourceFormat;
    }
    
    /* (non-Javadoc)
	 * @see kim.kohlhaas.sone.file.FormatConverter#getTargetFormat()
	 */
    @Override
	public AudioFormat getTargetFormat() {
        return targetFormat;
    }
    
    /* (non-Javadoc)
	 * @see kim.kohlhaas.sone.file.FormatConverter#getFrameLength()
	 */
    @Override
	public long getFrameLength() {
        return frameLength;
    }
    
    /* (non-Javadoc)
	 * @see kim.kohlhaas.sone.file.FormatConverter#getFrameRate()
	 */
    @Override
	public float getFrameRate() {
        return frameRate;        
    }
    
    /* (non-Javadoc)
	 * @see kim.kohlhaas.sone.file.FormatConverter#getMillisecondDuration()
	 */
    @Override
	public double getMillisecondDuration() {
        return milliSecondDuration;
    }

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
    
}
