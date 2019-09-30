package kim.kohlhaas.javafx.color;

import javafx.scene.paint.Color;

public class HeatColorRange implements ColorRange {
    
    private static final int RANGE_SECTIONS = 7;
    private static final int COLOR_DEPTH = 256;
    
    private enum Level {
        MIN, MAX, INCREASE, DECREASE
    }
    
    private final Level[] blue = {
            Level.INCREASE,
            Level.MAX,
            Level.DECREASE,
            Level.MIN,
            Level.MIN,
            Level.INCREASE,
            Level.MAX
    };
    
    private final Level[] green = {
            Level.MIN,
            Level.INCREASE,
            Level.MAX,
            Level.MAX,
            Level.DECREASE,
            Level.MIN,
            Level.INCREASE
    };
    
    private final Level[] red = {
            Level.MIN,
            Level.MIN,
            Level.MIN,
            Level.INCREASE,
            Level.MAX,
            Level.MAX,
            Level.MAX
    };
    
    private final Color[] colors = new Color[RANGE_SECTIONS * (COLOR_DEPTH - 1) + 1];
    
    public HeatColorRange() {
        assert(blue.length == RANGE_SECTIONS);
        assert(green.length == RANGE_SECTIONS);
        assert(red.length == RANGE_SECTIONS);
        
        int r, g, b;
        int c = 0;
        
        
        colors[c++] = Color.rgb(0, 0, 0);
        
        for (int s = 0; s < RANGE_SECTIONS; s++) {
            for (int i = 1; i < COLOR_DEPTH; i++) {
                r = getColorComponentValue(red[s], i);
                g = getColorComponentValue(green[s], i);
                b = getColorComponentValue(blue[s], i);
                colors[c++] = Color.rgb(r, g, b);
            }
        }
        

    }
    
    @Override
    public void setBackgroundColor (Color backgroundColor) {
    	int c = 0;
        colors[c++] = backgroundColor;

        for (int s = 0; s < RANGE_SECTIONS; s++) {
	        for (int i = 1; i < COLOR_DEPTH; i++) {
	            colors[c++] = Color.rgb(
	                   (int) (backgroundColor.getRed() * 255.0 +  (getColorComponentValue(red[s], i) * (255.0 - backgroundColor.getRed() * 255.0) / 255.0)),
	                   (int) (backgroundColor.getGreen() * 255.0 +  (getColorComponentValue(green[s], i) * (255.0 - backgroundColor.getGreen() * 255.0) / 255.0)),
	                   (int) (backgroundColor.getBlue() * 255.0 +  (getColorComponentValue(blue[s], i) * (255.0 - backgroundColor.getBlue() * 255.0) / 255.0))
	            );
	        }
        }
        

    }
    
    private int getColorComponentValue(Level level, int increment) {
        if (level == Level.MIN) {
            return 0;
        } else if (level == Level.MAX) {
            return COLOR_DEPTH - 1;
        } else if (level == Level.INCREASE) {
            return increment;
        } else if (level == Level.DECREASE) {
            return COLOR_DEPTH - 1 - increment;
        }
        
        return -1;
    }
    
    /* (non-Javadoc)
     * @see kim.kohlhaas.javafx.color.ColorRange#getLength()
     */
    @Override
    public int getLength() {

        return colors.length;
    }
    
    /* (non-Javadoc)
     * @see kim.kohlhaas.javafx.color.ColorRange#getColor(int)
     */
    @Override
    public Color getColor(int index) {
        return colors[index];
    }
    
}
