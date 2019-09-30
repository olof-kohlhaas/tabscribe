package kim.kohlhaas.sone.harmony;
/**
 * for low- and high-pitching a MIDI tone range 0-128 by 2 octaves -> -3-133
 * 
 * @author Olof Kohlhaas
 *
 */
public class VeryLongStringToneSet extends PianoToneSet {

    
    public VeryLongStringToneSet() {
        super(new TwelveToneEqualTemperament(), -3, 136);
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
