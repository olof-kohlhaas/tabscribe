package kim.kohlhaas.sone.harmony;

import org.apache.commons.math3.util.Precision;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;
import kim.kohlhaas.sone.harmony.Temperament;
import kim.kohlhaas.sone.harmony.GenericTone;
import kim.kohlhaas.sone.harmony.TwelveToneEqualTemperament;

public class TestTwelveToneEqualTemperament extends TestCase {

    private Temperament temperament;
    
    @Before
    protected void setUp() throws Exception {
        super.setUp();
        
        temperament = new TwelveToneEqualTemperament();
    }
    
    @Test
    public void testStandardPitch() {
        int midiKey = 69;
        assertEquals("Missing pitch standard A440 at MIDI tone 69.", 
        		GenericTone.Name.A, 
                temperament.getTone(midiKey).getName());
        assertEquals("Semitone on pitch standard A440 is not set to NONE.", 
        		GenericTone.Semitone.NONE, 
                temperament.getTone(midiKey).getSemitone());
        assertEquals("Pitch standard A440 doesn't equal the correct frequency.",
                440.000, 
                Precision.round((temperament.getTone(midiKey).getFrequency()), 3));
        assertEquals("Wrong octave assigned to pitch standard A440 at MIDI tone 69.", 
                4, 
                temperament.getTone(midiKey).getOctave());
    }
    
    @Test
    public void testMiddleC() {
        int midiKey = 60;
        assertEquals("Missing middle C at MIDI tone 60.", 
        		GenericTone.Name.C, 
                temperament.getTone(midiKey).getName());
        assertEquals("Semitone on middle C is not set to NONE.", 
        		GenericTone.Semitone.NONE, 
                temperament.getTone(midiKey).getSemitone());
        assertEquals("Middle C doesn't equal the correct frequency.",
                261.626, 
                Precision.round((temperament.getTone(midiKey).getFrequency()), 3));
        assertEquals("Wrong octave assigned to middle C at MIDI tone 60.", 
                4, 
                temperament.getTone(midiKey).getOctave());
    }
    
    @Test
    public void testFirstKey88Piano() {
        int midiKey = 21;
        assertEquals("Wrong tone at first key of 88-key piano at MIDI tone 21.", 
        		GenericTone.Name.A, 
                temperament.getTone(midiKey).getName());
        assertEquals("Semitone on first key of 88-key piano is not set to NONE.", 
        		GenericTone.Semitone.NONE, 
                temperament.getTone(midiKey).getSemitone());
        assertEquals("First key of 88-key piano doesn't equal the correct frequency.",
                27.500, 
                Precision.round((temperament.getTone(midiKey).getFrequency()), 3));
        assertEquals("Wrong octave assigned to first key of 88-key piano.", 
                0, 
                temperament.getTone(midiKey).getOctave());
    }
    
    @Test
    public void testLastKey88Piano() {
        int midiKey = 108;
        assertEquals("Wrong tone at last key of 88-key piano at MIDI tone 108.", 
        		GenericTone.Name.C, 
                temperament.getTone(midiKey).getName());
        assertEquals("Semitone on last key of 88-key piano is not set to NONE.", 
        		GenericTone.Semitone.NONE, 
                temperament.getTone(midiKey).getSemitone());
        assertEquals("Last key of 88-key piano doesn't equal the correct frequency.",
                4186.01, 
                Precision.round((temperament.getTone(midiKey).getFrequency()), 2));
        assertEquals("Wrong octave assigned to first key of 88-key piano.", 
                8, 
                temperament.getTone(midiKey).getOctave());
    }
    
    @Test
    public void testFirstMIDITone() {
        int midiKey = 0;
        assertEquals("Wrong first MIDI tone.", 
        		GenericTone.Name.C, 
                temperament.getTone(midiKey).getName());
        assertEquals("Semitone on first MIDI tone is not set to NONE.", 
        		GenericTone.Semitone.NONE, 
                temperament.getTone(midiKey).getSemitone());
        assertEquals("First MIDI tone doesn't equal the correct frequency.",
                8.176, 
                Precision.round((temperament.getTone(midiKey).getFrequency()), 3));
        assertEquals("Wrong octave assigned to first MIDI tone.", 
                -1, 
                temperament.getTone(midiKey).getOctave());
    }
    
    @Test
    public void testLastMIDITone() {
        int midiKey = 127;
        assertEquals("Wrong last MIDI tone.", 
        		GenericTone.Name.G, 
                temperament.getTone(midiKey).getName());
        assertEquals("Semitone on last MIDI tone is not set to NONE.", 
        		GenericTone.Semitone.NONE, 
                temperament.getTone(midiKey).getSemitone());
        assertEquals("Last MIDI tone doesn't equal the correct frequency.",
                12543.9, 
                Precision.round((temperament.getTone(midiKey).getFrequency()), 1));
        assertEquals("Wrong octave assigned to last MIDI tone.", 
                9, 
                temperament.getTone(midiKey).getOctave());
    }

}
