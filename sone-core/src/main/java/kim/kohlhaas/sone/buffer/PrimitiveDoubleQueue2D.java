package kim.kohlhaas.sone.buffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrimitiveDoubleQueue2D {
    
    final static Logger log = LoggerFactory.getLogger(PrimitiveDoubleQueue2D.class);
    
    private int channels;
    private int length;
    private int size;
    private double[][] data;
    private String logName;

    public PrimitiveDoubleQueue2D(int initialSize, int channels, String logName) {
        this.channels = channels;
        this.size = initialSize;
        this.logName = logName;
        
        data = new double[channels][initialSize];
        length = 0;
    }
    
    public PrimitiveDoubleQueue2D(int initialSize, int channels) {
        this(initialSize, channels, "");
    }
    
    public synchronized void push(double[][] values, int pos, int length) {        
        while (this.length + length > this.size) {
            enlarge();
        }

        for (int c = 0; c < channels; c++) {
            System.arraycopy(values[c], pos, data[c], this.length, length);
        }

        this.length += length;
    }
    
    
    //values enthält 1 frame mit der entsprechenden Anzahl von Kanälen
    public synchronized void push(double[] values) {        
        while (this.length + 1 > this.size) {
            enlarge();
        }

        for (int c = 0; c < channels; c++) {
             data[c][this.length] = values[c];
        }

        this.length += 1;
    }
    
    // TODO kann evtl entfernt werden, wenn entsprechend 1D-Queue existiert.
    public synchronized void push(double value) {        
        while (this.length + 1 > this.size) {
            enlarge();
        }


        data[0][this.length] = value;


        this.length += 1;
    }
    
    
    public synchronized int copy(double[][] carrier, int pos, int length) {
        int copyLength = Math.min(length, this.length);
        
        for (int c = 0; c < channels; c++) {
            System.arraycopy(data[c], 0, carrier[c], pos, copyLength);
        }
        
        return copyLength;
    }
    
    public synchronized int shift(double[][] carrier, int pos, int length) {
        int shiftLength = Math.min(length, this.length);
        
        for (int c = 0; c < channels; c++) {
            System.arraycopy(data[c], 0, carrier[c], pos, shiftLength);
        }
        
        discard(shiftLength);
        
        return shiftLength;
    }
    
    public synchronized int shift(double[][] carrier, int length) {
        return shift(carrier, 0, length);
    }
    
    public synchronized double[][] shift(int length) {
        double result[][];
        
        // 0 equals all
        if (length == 0 || length > this.length) {
            length = this.length;
        }
        
        result = new double[channels][length];
        
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
        double[][] tmp = new double[channels][size];
        
        
        
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
