package kim.kohlhaas.sone.signal;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;

public class FloatPCMByteConverter {

    private ByteBuffer[] channelBuffer;
    private int bitDepth;
    private int bytesPerSample;
    private int channels;
    
    public FloatPCMByteConverter(AudioFormat format) {
        this.channelBuffer = new ByteBuffer[format.getChannels()];
        for (int i = 0; i < format.getChannels(); i++) {
            this.channelBuffer[i] = ByteBuffer.allocate(format.getFrameSize() / format.getChannels());
            this.channelBuffer[i].order(format.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        }
        bitDepth = format.getSampleSizeInBits();
        bytesPerSample = bitDepth / 8;
        channels = format.getChannels();
        
    }
    
    public int convert(float[][] frames, int offset, int length, byte[] result) {
        int currentFrame = 0;
        
        for (int f = offset; f < frames[0].length && f < offset + length; f++) {
            for (int c = 0; c < channels; c++) {
              
               channelBuffer[c].clear();
               if (bitDepth <= 8) {
            	   if (frames[c][f] >= 0) {
                       channelBuffer[c].put((byte) (frames[c][f] * Byte.MAX_VALUE));
                   } else {
                       channelBuffer[c].put((byte) (frames[c][f] * Math.abs(Byte.MIN_VALUE)));
                   }
                   
                   for (int b = 0; b < channelBuffer[c].capacity(); b++) {
                       result[(f - offset) * bytesPerSample * channels + c * bytesPerSample + b] = channelBuffer[c].get(b);
                   }
               } else if (bitDepth <= 16) {
            	   if (frames[c][f] >= 0) {
                       channelBuffer[c].putShort((short) (frames[c][f] * Short.MAX_VALUE));
                   } else {
                       channelBuffer[c].putShort((short) (frames[c][f] * Math.abs(Short.MIN_VALUE)));
                   }
                   
                   for (int b = 0; b < channelBuffer[c].capacity(); b++) {
                       result[(f - offset) * bytesPerSample * channels + c * bytesPerSample + b] = channelBuffer[c].get(b);
                   }
                   
               } else if (bitDepth <= 32) {
            	   if (frames[c][f] >= 0) {
                       channelBuffer[c].putInt((int) (frames[c][f] * Integer.MAX_VALUE));
                   } else {
                       channelBuffer[c].putInt((int) (frames[c][f] * Math.abs(Integer.MIN_VALUE)));
                   }
                   
                   for (int b = 0; b < channelBuffer[c].capacity(); b++) {
                       result[(f - offset) * bytesPerSample * channels + c * bytesPerSample + b] = channelBuffer[c].get(b);
                   }
               } else if (bitDepth <= 64) {
            	   if (frames[c][f] >= 0) {
                       channelBuffer[c].putLong((long) (frames[c][f] * Long.MAX_VALUE));
                   } else {
                       channelBuffer[c].putLong((long) (frames[c][f] * Math.abs(Long.MIN_VALUE)));
                   }
                   
                   for (int b = 0; b < channelBuffer[c].capacity(); b++) {
                       result[(f - offset) * bytesPerSample * channels + c * bytesPerSample + b] = channelBuffer[c].get(b);
                   }
               }
           }
           currentFrame++;
       }
        
        return currentFrame;
    }
    
    
}
