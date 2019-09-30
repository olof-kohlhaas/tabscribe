package kim.kohlhaas.sone.harmony;

public class PianoHighKeyToneSet extends PianoToneSet {
	
	public PianoHighKeyToneSet() {
        super(new TwelveToneEqualTemperament(), 21, 99);
    }
	
    @Override
    public Tone getTone(int index) {    	
        return getKeyTone(getKeyNumber(index));
    }
	
    @Override
    public Tone getKeyTone(int keyNumber) {
        return super.getTone(getIndex(keyNumber));
    }

    @Override
    public int getKeyNumber(int index) {
    	if (index < 88) {
    		return index + 1;
    	} else {
    		return index + 10;
    	}
    }

    @Override
    public int getIndex(int keyNumber) {
    	if (keyNumber <= 88) {
    		return keyNumber - 1;
    	} else if (keyNumber >= 98) {
    		return keyNumber - 10; 
    	} else {
    		return -1;
    	}
    }

}
