package kim.kohlhaas.sone.buffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrimitiveFloatQueue2D {
    
    final static Logger log = LoggerFactory.getLogger(PrimitiveFloatQueue2D.class);
    
    private int channels;
    private int length;
    private int size;
    private float[][] data;
    private String logName;
    
    public PrimitiveFloatQueue2D(int initialSize, int channels, String logName) {
        this.channels = channels;
        this.size = initialSize;
        this.logName = logName;
        
        data = new float[channels][initialSize];
        length = 0;
    }
    
    public PrimitiveFloatQueue2D(int initialSize, int channels) {
        this(initialSize, channels, "");
    }
    
    public synchronized void push(float[][] values, int pos, int length) {        
        while (this.length + length > this.size) {
            enlarge();
        }

        for (int c = 0; c < channels; c++) {
            System.arraycopy(values[c], pos, data[c], this.length, length);
        }

        this.length += length;
    }
    
    //values enthält 1 frame mit der entsprechenden Anzahl von Kanälen
    public synchronized void push(float[] values) {        
        while (this.length + 1 > this.size) {
            enlarge();
        }

        for (int c = 0; c < channels; c++) {
             data[c][this.length] = values[c];
        }

        this.length += 1;
    }
    
    // TODO kann evtl entfernt werden, wenn entsprechend 1D-Queue existiert.
    public synchronized void push(float value) {        
        while (this.length + 1 > this.size) {
            enlarge();
        }


        data[0][this.length] = value;


        this.length += 1;
    }
    
    public synchronized int copy(float[][] carrier, int pos, int length) {
        int copyLength = Math.min(length, this.length);
        
        for (int c = 0; c < channels; c++) {
            System.arraycopy(data[c], 0, carrier[c], pos, copyLength);
        }
        
        return copyLength;
    }
    
    public synchronized int shift(float[][] carrier, int pos, int length) {
        int shiftLength = Math.min(length, this.length);
        
        for (int c = 0; c < channels; c++) {
            System.arraycopy(data[c], 0, carrier[c], pos, shiftLength);
        }
        
        discard(shiftLength);
        
        return shiftLength;
    }
    
    public synchronized int shift(float[][] carrier, int length) {
        return shift(carrier, 0, length);
    }
    
    public synchronized float[][] shift(int length) {
        float result[][];
        
        // 0 equals all
        if (length == 0 || length > this.length) {
            length = this.length;
        }
        
        result = new float[channels][length];
        
        shift(result, length);
        
        return result;
    }
    
    public synchronized int length() {
        return length;
    }
    
    public int getChannels() {
        return this.channels;
    }
    
    public synchronized void resize(int size) {
        if (size < length) {
            size = length;
        }
        float[][] tmp = new float[channels][size];
        
        
        
        for (int c = 0; c < channels; c++) {
            System.arraycopy(data[c], 0, tmp[c], 0, length);
        }
        
        log.debug("{} resized: {}", logName, size);
        
        
        this.size = size;
        data = tmp;
    }
    
    private void enlarge() {
        resize(size * 2);
    }
    
    public synchronized void discard(int length) {
        int newLength = this.length - length;
        
        if (newLength >= 0) {
            for (int c = 0; c < channels; c++) {
                System.arraycopy(data[c], length, data[c], 0, newLength);
            }
            
            this.length = newLength;
        }
    }

}
