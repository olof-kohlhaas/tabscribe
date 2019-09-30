package kim.kohlhaas.sone.tabscribe.javafx;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.DoubleProperty;
import kim.kohlhaas.sone.util.TimeUtils;

public class PositionStringBinding extends StringBinding {
    
    private DoubleProperty positionProperty;
    
    public PositionStringBinding(DoubleProperty positionProperty) {
        super.bind(positionProperty);
        this.positionProperty = positionProperty;
    }

    @Override
    protected String computeValue() {
        return TimeUtils.getFormattedString(positionProperty.get());
    }

}
