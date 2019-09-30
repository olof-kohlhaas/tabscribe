package kim.kohlhaas.sone.harmony;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kim.kohlhaas.sone.util.HarmonicsUtil;
import kim.kohlhaas.sone.util.MathUtils;

public class TwelveToneEqualTemperament implements Temperament {
    
    private static final int MIDI_TONE_COUNT = 128;
    
    // for low- and high-pitching a 88-key-piano by 2 octaves plus 1 tone buffer for fuzzy mouse overs at the borders
    // and plus extra keys
    private static final int MIDI_TONE_COUNT_EXCEEDED = 11 + 5 + 1;
    private static final int MIDI_TONE_COUNT_DECEEDED = 9 + 3 + 1;
    
    private static final int OCTAVE_TONE_COUNT = 12;
        
    private static final GenericTone.Name[] TONE_ORDER = {
            GenericTone.Name.C, GenericTone.Name.C,
            GenericTone.Name.D, GenericTone.Name.D,
            GenericTone.Name.E,
            GenericTone.Name.F, GenericTone.Name.F,
            GenericTone.Name.G, GenericTone.Name.G,
            GenericTone.Name.A, GenericTone.Name.A,
            GenericTone.Name.B
    };
    
    private static final GenericTone.Semitone[] SEMITONE_ORDER = {
            GenericTone.Semitone.NONE, GenericTone.Semitone.SHARP,
            GenericTone.Semitone.NONE, GenericTone.Semitone.SHARP,
            GenericTone.Semitone.NONE,
            GenericTone.Semitone.NONE, GenericTone.Semitone.SHARP,
            GenericTone.Semitone.NONE, GenericTone.Semitone.SHARP,
            GenericTone.Semitone.NONE, GenericTone.Semitone.SHARP,
            GenericTone.Semitone.NONE
    };
    
    private final HashMap<Tone, Double> frequencies = new HashMap<>();
    private final List<Tone> tones = new ArrayList<Tone>();
    private final List<Tone> tonesDeceeded = new ArrayList<Tone>();
    
    public TwelveToneEqualTemperament() {
        int octave;
        int indexWithinOctave;
        int pianoKeyNumber;
        double frequency;
        Tone tone;

        
        for (int i = 0; i < MIDI_TONE_COUNT + MIDI_TONE_COUNT_EXCEEDED; i++) {
            octave = (int) (i / OCTAVE_TONE_COUNT) - 1;
            indexWithinOctave = i % OCTAVE_TONE_COUNT;
            pianoKeyNumber = i - 20;
            frequency = HarmonicsUtil.getPianoKeyFrequency(pianoKeyNumber);
            tone = new Tone(
                    TONE_ORDER[indexWithinOctave], 
                    SEMITONE_ORDER[indexWithinOctave], 
                    octave, this);
            tones.add(tone);
            frequencies.put(tone, frequency);
        }
        
        for (int i = 0; i < MIDI_TONE_COUNT_DECEEDED; i++) {
            octave = ((int) (i / OCTAVE_TONE_COUNT) + 2) * (-1);
            indexWithinOctave = OCTAVE_TONE_COUNT - 1 - i  % OCTAVE_TONE_COUNT;
            pianoKeyNumber = (i + 1) * (-1) - 20;
            frequency = HarmonicsUtil.getPianoKeyFrequency(pianoKeyNumber);
            tone = new Tone(
                    TONE_ORDER[indexWithinOctave], 
                    SEMITONE_ORDER[indexWithinOctave], 
                    octave, this);            
            tonesDeceeded.add(tone);
            frequencies.put(tone, frequency);
        }
        
    }
    
    /* (non-Javadoc)
     * @see kim.kohlhaas.sone.harmony.Temperament#getToneCount()
     */
    @Override
    public int getToneCount() {
        return MIDI_TONE_COUNT;
    }
    
    /* (non-Javadoc)
     * @see kim.kohlhaas.sone.harmony.Temperament#getTone(int)
     */
    @Override
    public Tone getTone(int index) {
        if (index >= 0) {
            return tones.get(index);
        } else {
            return tonesDeceeded.get(index * (-1) - 1);
        }
    }

    @Override
    public int getIndex(Tone tone) {
        int index = tones.indexOf(tone);
        if (index >= 0) {
            return index;
        } else {
            index = tonesDeceeded.indexOf(tone);
            if (index >= 0) {
                return (index + 1) * (-1);
            }
        }
        return getNearestIndex(tone.getFrequency());
    }
    
    @Override
    public int getOctaveToneCount() {
        return OCTAVE_TONE_COUNT;
    }

    @Override
    public Tone getNearestTone(double frequency) {
        return getTone(getNearestIndex(frequency));
    }

    @Override
    public int getNearestIndex(double frequency) {
        return (int) Math.round(getGradientIndex(frequency));
    }

    @Override
    public double getGradientIndex(double frequency) {
        return MathUtils.lb(frequency / 440.0) * 12 + 69;
    }

    @Override
    public double getTonePrecision(double frequency) {
        return HarmonicsUtil.getTonePrecision(getNearestIndex(frequency), getGradientIndex(frequency));
    }

    @Override
    public Tone getPitchShiftedTone(Tone tone, int semitones) {
        if (semitones == 0.0) {
            return tone;
        } else {
            return getTone(getIndex(tone) + semitones);
        }
    }

	@Override
	public double getFrequency(Tone tone) {
		return frequencies.get(tone);
	}
    
	@Override
	public String toString() {
		return "twelve-tone-equal-temperament";
	}
    
}
