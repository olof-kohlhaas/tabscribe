package kim.kohlhaas.sone.harmony;

public class Piano88KeyToneSet extends PianoToneSet {
    
    public Piano88KeyToneSet() {
        super(new TwelveToneEqualTemperament(), 21, 88);
    }

    @Override
    public Tone getKeyTone(int keyNumber) {
        return super.getTone(getIndex(keyNumber));
    }

    @Override
    public int getKeyNumber(int index) {
        return index + 1;
    }

    @Override
    public int getIndex(int keyNumber) {
        return keyNumber - 1;
    }

}
