package kim.kohlhaas.sone.signal;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Converts a PCM byte sequence of both big endian or little endian order into a float sequence normalized to less or 
 * equal to 1.0 and greater or equal to -1.0.
 * The bit depth of the byte sequence can be of 8, 16, 32 or 64 bits. With the result being saved to float values with a
 * range of 2.0 the precision will end up with less than 32 bit, though.
 * 
 */
public class PCMByteFloatConverter {
    
    private ByteBuffer conversionBuffer;
    private AudioFormat format;
    private int bytesPerSample;
    
    /**
     * The constructor of the converter with a given AudioFormat of the byte sequence which should be converted.
     *     
     * @param format the format of the PCM byte stream to be converted
     */
    public PCMByteFloatConverter(AudioFormat audioFormat) {
        this.format = audioFormat;
        
        conversionBuffer = ByteBuffer.allocate(this.format.getFrameSize() / this.format.getChannels());
        conversionBuffer.order(this.format.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        bytesPerSample = format.getSampleSizeInBits() / 8;        
    }
    
    /**
     * Converts the the a byte stream conforming to the audio format passed to the constructor into a float array.
     * The result array must be passed to avoid repeatedly instantiation of return arrays.
     * 
     * @param bytes the bytes to be converted. 
     * @param offset the offset from the beginning of the bytes array from where to start conversion
     * @param length the number of bytes to be converted. Must correspondent to whole frames.
     * @param result the array the result should be written to. The length of the first dimension corresponds to the 
     *          number of channels, the second dimension corresponds to the number of frames and samples, respectively.
     * @throws IllegalArgumentException if the passed bytes do not correspondent to whole frames, in other words 
     *          if the bytes length is not a multiple of the audioFormat.getFrameSize(). Or if the result array has got
     *          less channel entries than the audioFormat. Or if the result array has got less sample entries than the
     *          passed byte array.
     * @throws UnsupportedAudioFileException if the bit depth greater than 64 bit
     * @return the number of frames written into the result array
     */
    public int convert(byte[] bytes, int offset, int length, float[][] result) throws UnsupportedAudioFileException {
        int currentChannel = 0;
        int currentFrame = 0;
        int channels = format.getChannels();
        int bitDepth = format.getSampleSizeInBits();
        byte byteSample;
        short shortSample;
        int intSample;
        long longSample;
        
        if (length % format.getFrameSize() != 0) {
            throw new IllegalArgumentException("the bytes length does not match a multiple of the frame size.");
        }
        
        if (result.length < format.getChannels()) {
            throw new IllegalArgumentException("the result array contains less channel entries"
                    + " than the given audio format");
        }
        
        if (result[0].length < length / format.getFrameSize()) {
            throw new IllegalArgumentException("the result array contains less sample entries"
                    + " than the passed bytes correspondent to");
        }

        conversionBuffer.clear();
        
        // TODO Momentan sorgt PCMStream für passende Chunk-Größe. Zur Wiederververwendbarkeit bytes sammeln
        for (int i = offset; i < bytes.length && i < offset + length; i++) {

            conversionBuffer.put(bytes[i]);
            
         // TODO U-law und a-Law umsetzen
            
            if ((i + 1) % bytesPerSample == 0) {
                // End-Byte eines Channel-Datenworts erreicht                

                
                if (bitDepth <= 8) {
                    byteSample = conversionBuffer.get(0);

                    if (byteSample >= 0) {
                        result[currentChannel][currentFrame] = (float) byteSample / Byte.MAX_VALUE;
                    } else {
                        result[currentChannel][currentFrame] = (float) byteSample / Math.abs(Byte.MIN_VALUE);
                    }
                } else if (bitDepth <= 16) {
                    shortSample = conversionBuffer.getShort(0);

                    if (shortSample >= 0) {
                        result[currentChannel][currentFrame] = (float) shortSample / Short.MAX_VALUE;
                    } else {
                        result[currentChannel][currentFrame] = (float) shortSample / Math.abs(Short.MIN_VALUE);
                    }
                } else if (bitDepth <= 32) {
                	// TODO IEEE-Float 32 umsetzen
                    intSample = conversionBuffer.getInt(0);

                    if (intSample >= 0) {
                        result[currentChannel][currentFrame] = (float) intSample / Integer.MAX_VALUE;
                    } else {
                        result[currentChannel][currentFrame] = (float) intSample / Math.abs(Integer.MIN_VALUE);
                    }
                } else if (bitDepth <= 64) {
                	// TODO IEEE-Float 64 umsetzen
                    longSample = conversionBuffer.getLong(0);

                    if (longSample >= 0) {
                        result[currentChannel][currentFrame] = (float) longSample / Long.MAX_VALUE;
                    } else {
                        result[currentChannel][currentFrame] = (float) longSample / Math.abs(Long.MIN_VALUE);
                    }
                } else {
                    throw new UnsupportedAudioFileException("A bit depth of more than 64 bit is not supported.");
                }
                
                currentChannel++;
                
                if (currentChannel == channels) {
                    // Letzter Kanal entspricht Ende eines Frames
                	currentChannel = 0;
                    currentFrame++;
                }

                conversionBuffer.clear();
            }

        }
        
        return currentFrame;
    }
    
}
