package kim.kohlhaas.javafx.color;

import javafx.scene.paint.Color;

public interface ColorRange {

    void setBackgroundColor (Color backgroundColor);
    
    int getLength();

    Color getColor(int index);

}