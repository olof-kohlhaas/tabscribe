package kim.kohlhaas.sone.signal;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Control.Type;

import kim.kohlhaas.sone.buffer.PrimitiveFloatQueue2D;
import kim.kohlhaas.sone.util.PCMUtils;

import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class FloatSourceDataLine implements SourceDataLine {
    
    private SourceDataLine sourceDataLine;
    private byte[] byteChunk;
    private FloatPCMByteConverter converter;
    private final static int DEFAULT_BUFFER_SIZE_IN_FRAMES = 1000;
    private AudioFormat format;
    private int bytesWritten;
    private int framesPerChunk;
    private int bytesPerChunk;
    
    public void open(AudioFormat format) throws LineUnavailableException {
        open(format, (int) PCMUtils.getByteCount(format, DEFAULT_BUFFER_SIZE_IN_FRAMES));
    }
    
    public void open(AudioFormat format, int bufferSize, boolean bufferSizeInFrames) throws LineUnavailableException {   	
    	if (bufferSizeInFrames) {
            open(format, (int) PCMUtils.getByteCount(format, bufferSize));
        } else {
            open(format, bufferSize);
        }
    } 
    
    public void open(AudioFormat format, int bufferSize) throws LineUnavailableException {
        this.format = format;
        
        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format, bufferSize);
        sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
        if (byteChunk == null || byteChunk.length < bufferSize) {
            byteChunk = new byte[bufferSize];
        }

        converter = new FloatPCMByteConverter(format);
        sourceDataLine.open(format, bufferSize);
    }
    
    public int write(float[][] frames, int offset, int length) {        
        
        // TODO nice to have: collecting frames in a buffer half the wrapped line's buffer's size before converting and passing to wrapped line due to performance reasons
        
        if (length != framesPerChunk) {
            framesPerChunk = length;
            bytesPerChunk = framesPerChunk * format.getFrameSize();
            if (byteChunk.length < bytesPerChunk) {
                byteChunk = new byte[bytesPerChunk];
            }
        }
                
        converter.convert(frames, offset, framesPerChunk, byteChunk);
        
        bytesWritten = sourceDataLine.write(byteChunk, 0, bytesPerChunk);
        
        return bytesWritten / format.getFrameSize();
    }
    
    @Override
    public int write(byte[] b, int off, int len) {
        return sourceDataLine.write(b, off, len);
    }
    
    @Override
    public void drain() {
        sourceDataLine.drain();
    }

    @Override
    public void flush() {
        sourceDataLine.flush();
    }

    @Override
    public void start() {
        sourceDataLine.start();
    }

    @Override
    public void stop() {
        sourceDataLine.stop();
    }

    @Override
    public boolean isRunning() {
        return sourceDataLine.isRunning();
    }

    @Override
    public boolean isActive() {
        return sourceDataLine.isActive();
    }

    @Override
    public AudioFormat getFormat() {
        return sourceDataLine.getFormat();
    }

    @Override
    public int getBufferSize() {
        return sourceDataLine.getBufferSize();
    }

    @Override
    public int available() {
        return sourceDataLine.available();
    }

    @Override
    public int getFramePosition() {
        return sourceDataLine.getFramePosition();
    }

    @Override
    public long getLongFramePosition() {
        return sourceDataLine.getLongFramePosition();
    }

    @Override
    public long getMicrosecondPosition() {
        return sourceDataLine.getMicrosecondPosition();
    }

    @Override
    public float getLevel() {
        return sourceDataLine.getLevel();
    }

    @Override
    public javax.sound.sampled.Line.Info getLineInfo() {
        return sourceDataLine.getLineInfo();
    }

    @Override
    public void open() throws LineUnavailableException {
        sourceDataLine.open();
    }

    @Override
    public void close() {
        sourceDataLine.close();
    }

    @Override
    public boolean isOpen() {
        if (sourceDataLine == null) {
            return false;
        } else {
            return sourceDataLine.isOpen();
        }
    }

    @Override
    public Control[] getControls() {
        return sourceDataLine.getControls();
    }

    @Override
    public boolean isControlSupported(Type control) {
        return sourceDataLine.isControlSupported(control);
    }

    @Override
    public Control getControl(Type control) {
        return sourceDataLine.getControl(control);
    }

    @Override
    public void addLineListener(LineListener listener) {
        sourceDataLine.addLineListener(listener);

    }

    @Override
    public void removeLineListener(LineListener listener) {
        sourceDataLine.removeLineListener(listener);
    }

}
