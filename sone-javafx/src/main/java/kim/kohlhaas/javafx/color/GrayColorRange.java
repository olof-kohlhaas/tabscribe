package kim.kohlhaas.javafx.color;

import javafx.scene.paint.Color;

public class GrayColorRange implements ColorRange {
    
    private final Color[] colors = new Color[256];

    public GrayColorRange() {
        setBackgroundColor(Color.rgb(0, 0, 0));
    }
    
    @Override
    public void setBackgroundColor (Color backgroundColor) {
        for (int i = 0; i < 256; i++) {
            colors[i] = Color.rgb(
                   (int) (backgroundColor.getRed() * 255.0 +  (i * (255.0 - backgroundColor.getRed() * 255.0) / 255.0)),
                   (int) (backgroundColor.getGreen() * 255.0 + (i * (255.0 - backgroundColor.getGreen() * 255.0) / 255.0)),
                   (int) (backgroundColor.getBlue() * 255.0 +  (i * (255.0 - backgroundColor.getBlue() * 255.0) / 255.0))
            );
        }
    }
    
    @Override
    public int getLength() {
        return colors.length;
    }

    @Override
    public Color getColor(int index) {
        return colors[index];
    }

}
