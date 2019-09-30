package kim.kohlhaas.sone.buffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrimitiveFloatQueue1D {
    final static Logger log = LoggerFactory.getLogger(PrimitiveFloatQueue1D.class);
    
    private int length;
    private int size;
    private float[] data;
    private String logName;
    
    public PrimitiveFloatQueue1D(int initialSize, String logName) {
        this.size = initialSize;
        this.logName = logName;
        
        data = new float[initialSize];
        length = 0;
    }
    
    public PrimitiveFloatQueue1D(int initialSize) {
        this(initialSize, "");
    }
    
    public synchronized void push(float[] values, int pos, int length) {        
        while (this.length + length > this.size) {
            enlarge();
        }

        System.arraycopy(values, pos, data, this.length, length);

        this.length += length;
    }
    
    public synchronized void push(float value) {        
        while (this.length + 1 > this.size) {
            enlarge();
        }

        data[this.length] = value;

        this.length += 1;
    }
    
    public synchronized int copy(float[] carrier, int pos, int length) {
        int copyLength = Math.min(length, this.length);
        
        System.arraycopy(data, 0, carrier, pos, copyLength);
                
        return copyLength;
    }
    
    public synchronized int shift(float[] carrier, int pos, int length) {
        int shiftLength = Math.min(length, this.length);
        
        System.arraycopy(data, 0, carrier, pos, shiftLength);
        
        discard(shiftLength);
        
        return shiftLength;
    }
    
    public synchronized int shift(float[] carrier, int length) {
        return shift(carrier, 0, length);
    }
    
    public synchronized float[] shift(int length) {
        float result[];
        
        // 0 equals all
        if (length == 0 || length > this.length) {
            length = this.length;
        }
        
        result = new float[length];
        
        shift(result, length);
        
        return result;
    }
    
    public synchronized int length() {
        return length;
    }
    
    public synchronized void resize(int size) {
        if (size < length) {
            size = length;
        }
        
        float[] tmp = new float[size];
        
        System.arraycopy(data, 0, tmp, 0, length);
        
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
            System.arraycopy(data, length, data, 0, newLength);
            this.length = newLength;
        }
    }
    
    
}
