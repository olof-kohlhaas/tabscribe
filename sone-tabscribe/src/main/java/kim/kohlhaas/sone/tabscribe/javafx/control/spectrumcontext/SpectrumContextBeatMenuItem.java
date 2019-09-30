package kim.kohlhaas.sone.tabscribe.javafx.control.spectrumcontext;

import javafx.scene.control.MenuItem;
import kim.kohlhaas.sone.tabscribe.model.Guitar;

public class SpectrumContextBeatMenuItem extends MenuItem {

	private Guitar guitar;
	
	SpectrumContextBeatMenuItem(String text, Guitar guitar) {
		super(text);
		
		this.guitar = guitar;
	}

	public Guitar getGuitar() {
		return guitar;
	}

	public void setGuitar(Guitar guitar) {
		this.guitar = guitar;
	}
	
}
