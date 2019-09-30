package kim.kohlhaas.sone.util;

public class ArrayUtils {
    
    public static float getMin(float[] values) {
        float min = Float.MAX_VALUE;
        
        for (int i = 0; i < values.length; i++) {
            if (values[i] < min) {
                min = values[i];
            }
        }
        
        return min;
    }
    
    public static float getMax(float[] values) {
        float max = Float.MIN_VALUE;
        
        for (int i = 0; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];
            }
        }
        
        return max;
    }
    
    public static float getMin(float[][] values) {
        float min = Float.MAX_VALUE;
        
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[i].length; j++) {
                if (values[i][j] < min) {
                    min = values[i][j];
                }
            }
        }
        
        return min;
    }
    
    public static float getMax(float[][] values) {
        float max = Float.MIN_VALUE;
        
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[i].length; j++) {
                if (values[i][j] > max) {
                    max = values[i][j];
                }
            }
        }
        
        return max;
    }
    
    public static float getMin(float[][] values, int index) {
        float min = Float.MAX_VALUE;
        
        for (int i = 0; i < values.length; i++) {
            if (values[i][index] < min) {
                min = values[i][index];
            }
        }
        
        return min;
    }
    
    public static float getMax(float[][] values, int index) {
        float max = Float.MIN_VALUE;
        
        for (int i = 0; i < values.length; i++) {
            if (values[i][index] > max) {
                max = values[i][index];
            }
        }
        
        return max;
    }
    
}
