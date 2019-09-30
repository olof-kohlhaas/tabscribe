package kim.kohlhaas.sone.tabscribe.javafx.control.spectrumcontext;

import javafx.scene.control.MenuItem;
import kim.kohlhaas.sone.tabscribe.model.Chord;
import kim.kohlhaas.sone.tabscribe.model.Guitar;

public class SpectrumContextChordMenuItem extends MenuItem {
	
	private Chord chord;
	private Guitar guitar;
	
	public SpectrumContextChordMenuItem(Chord chord, Guitar guitar) {
		this.chord = chord;
		this.guitar = guitar;
	}

	public Chord getChord() {
		return chord;
	}

	public void setChord(Chord chord) {
		this.chord = chord;
	}

	public Guitar getGuitar() {
		return guitar;
	}

	public void setGuitar(Guitar guitar) {
		this.guitar = guitar;
	}
	
}
