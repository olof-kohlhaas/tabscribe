package kim.kohlhaas.sone.javafx.binding;

import javafx.beans.binding.DoubleExpression;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.ObjectExpression;
import kim.kohlhaas.sone.harmony.Temperament;
import kim.kohlhaas.sone.harmony.Tone;

public class PitchShiftedToneBinding extends ObjectBinding<Tone> {
    
    private DoubleExpression pitchProperty;
    private ObjectExpression<Tone> toneProperty;
    private Temperament temperament;
    
    public PitchShiftedToneBinding(Temperament temperament, DoubleExpression pitchProperty, ObjectExpression<Tone> toneProperty) {
        super.bind(pitchProperty, toneProperty);
        this.temperament = temperament;
        this.pitchProperty = pitchProperty;
        this.toneProperty = toneProperty;
    }
    
    @Override 
    protected Tone computeValue() {
        return temperament.getPitchShiftedTone(toneProperty.get(), (int) pitchProperty.get());
    }
    
}
