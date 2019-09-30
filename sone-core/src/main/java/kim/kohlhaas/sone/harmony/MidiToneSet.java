package kim.kohlhaas.sone.harmony;

public class MidiToneSet extends PianoToneSet {
	
	public MidiToneSet() {
		super(new TwelveToneEqualTemperament(), 0, 128);
	}

	@Override
	public Tone getKeyTone(int keyNumber) {
		return super.getTone(keyNumber);
	}

	@Override
	public int getKeyNumber(int index) {
		return index;
	}

	@Override
	public int getIndex(int keyNumber) {
		return keyNumber;
	}

}
