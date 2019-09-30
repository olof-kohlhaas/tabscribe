package kim.kohlhaas.sone.tabscribe.javafx.control.spectrumcontext;

import javafx.beans.binding.ObjectExpression;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.MenuItem;
import kim.kohlhaas.sone.harmony.Tone;
import kim.kohlhaas.sone.tabscribe.model.GuitarString;

public class SpectrumContextMenuItem extends MenuItem {

	private GuitarString guitarString;
	private IntegerProperty tabProperty;

	public SpectrumContextMenuItem(GuitarString guitarString) {
		this.guitarString = guitarString;
		tabProperty = new SimpleIntegerProperty();
	}
	
	public IntegerProperty tabProperty() {
		return tabProperty;
	}

	public GuitarString getGuitarString() {
		return guitarString;
	}

	public void setGuitarString(GuitarString guitarString) {
		this.guitarString = guitarString;
	}
	
	
	
}
