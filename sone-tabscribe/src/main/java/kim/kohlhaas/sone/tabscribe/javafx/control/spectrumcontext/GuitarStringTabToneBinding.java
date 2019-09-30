package kim.kohlhaas.sone.tabscribe.javafx.control.spectrumcontext;

import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.ObjectExpression;
import kim.kohlhaas.sone.harmony.Tone;
import kim.kohlhaas.sone.tabscribe.model.GuitarString;

public class GuitarStringTabToneBinding extends IntegerBinding {

	private ObjectExpression<Tone> toneProperty;
	private GuitarString guitarString;
	
	public GuitarStringTabToneBinding(ObjectExpression<Tone> toneProperty, GuitarString guitarString) {
		super.bind(toneProperty);
		this.toneProperty = toneProperty;
		this.guitarString = guitarString;
	}
	
	@Override
	protected int computeValue() {
		if (toneProperty.get() == null) {
			return 0;
		} else {
			return (guitarString.getGuitar().getTemperament().getIndex(toneProperty.get()) - guitarString.getGuitar().getTemperament().getIndex(guitarString.getEmptyString()));
		}
	}
	
	public void unbind() {
		super.unbind(toneProperty);
	}

}
