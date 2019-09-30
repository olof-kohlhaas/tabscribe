package kim.kohlhaas.sone.tabscribe.javafx.control.spectrumcontext;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectExpression;
import kim.kohlhaas.sone.harmony.Tone;
import kim.kohlhaas.sone.tabscribe.model.Chord;

public class ToneChordBinding extends BooleanBinding {
	
	private ObjectExpression<Tone> toneProperty;
	private Chord chord;
	

	public ToneChordBinding(ObjectExpression<Tone> toneProperty, Chord chord) {
		super.bind(toneProperty);
		this.toneProperty = toneProperty;
		this.chord = chord;
	}
	
	@Override
	protected boolean computeValue() {
		if (toneProperty.get() == null) {
			return true;
		} else {
			
			return toneProperty.get().getName() == chord.getTone() && toneProperty.get().getSemitone() == chord.getSemitone();
		}
	}

}
