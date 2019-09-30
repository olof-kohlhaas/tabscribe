package kim.kohlhaas.sone.harmony;

import org.apache.commons.lang3.StringUtils;

public class ToneFormatter {
    
    private static String subscriptDigits = "\u2080\u2081\u2082\u2083\u2084\u2085\u2086\u2087\u2088\u2089";
    private static String subscriptMinus = "\u208B";
    private static String flat = "\u266D";
    private static String sharp = "\u266F";
    private static String strichunten = "\u0375";
    private static String strichoben = "\u2032";
    
    public enum PitchNotation {
        SCIENTIFIC, HELMHOLTZ
    }
    
    public enum SemitoneCoincide {
        FLAT, SHARP, BOTH
    }
    
    public static String simpleFormat(PitchNotation notation, SemitoneCoincide semitone, Temperament temperament, Tone tone) {
        String result = "";
        Tone sharpName = null;
        Tone flatName = null;
        int index;
        
        if (tone.getSemitone() == GenericTone.Semitone.NONE) {
            sharpName = null;
            flatName = null;
        } else if (tone.getSemitone() == GenericTone.Semitone.SHARP) {
            index = temperament.getIndex(tone);
            sharpName = tone;
            flatName = temperament.getTone(index + 1);
        } else if (tone.getSemitone() == GenericTone.Semitone.FLAT) {
            index = temperament.getIndex(tone);
            sharpName = temperament.getTone(index - 1);
            flatName = tone;
        }
        
        if (sharpName == null && flatName == null) {            
            result = formatToneName(notation, tone.getOctave(), tone.getName().toString()) 
                    + formatOctave(notation, tone.getOctave());
        } else {
            if (semitone == SemitoneCoincide.BOTH) {
                result = formatToneName(notation, tone.getOctave(), sharpName.getName().toString())
                        + sharp
                        + formatOctave(notation, sharpName.getOctave())
                        + " / " + formatToneName(notation, tone.getOctave(), flatName.getName().toString()) 
                        + flat
                        + formatOctave(notation, flatName.getOctave());
            } else if (semitone == SemitoneCoincide.SHARP) {
                result = formatToneName(notation, tone.getOctave(), sharpName.getName().toString())
                        + sharp
                        + formatOctave(notation, sharpName.getOctave());
            } else if (semitone == SemitoneCoincide.FLAT) {
                result = formatToneName(notation, tone.getOctave(), flatName.getName().toString()) 
                        + flat
                        + formatOctave(notation, flatName.getOctave());
            }
        }
        
        
        return result;
    }
    
    public static String simpleFormat(GenericTone.Name toneName, GenericTone.Semitone semitone) {
    	String result = toneName.toString();
    	
    	if (semitone == GenericTone.Semitone.SHARP) {
    		result += sharp;
    	} else if (semitone == GenericTone.Semitone.FLAT) {
    		result += flat;
    	}
    	
    	return result;
    }
    
    private static String formatToneName(PitchNotation notation, int octave, String toneName) {
        if (notation == PitchNotation.SCIENTIFIC) {
            return toneName.toUpperCase();
        } else if (notation == PitchNotation.HELMHOLTZ) {
            if (octave >= 3) {
                return toneName.toLowerCase();
            } else if (octave <= 2) {
                return toneName.toUpperCase();
            }
        }
        return toneName;
    }
    
    private static String formatOctave(PitchNotation notation, int octave) {
        String sign = "";
        
        if (octave < 0) {
            sign = subscriptMinus;
        }
        
        int lowSignificance = Math.abs(octave) % 10;
        int highSignificance = Math.floorDiv(Math.abs(octave), 10);
        
        if (notation == PitchNotation.SCIENTIFIC) {
            if (highSignificance == 0) {
                return sign + subscriptDigits.charAt(lowSignificance);
            } else {
                return sign + subscriptDigits.charAt(highSignificance) + "" + subscriptDigits.charAt(lowSignificance);
            }
        } else if (notation == PitchNotation.HELMHOLTZ) {
            if (octave == 2 || octave == 3) {
                return "";
            } else if (octave >= 4) {
                return StringUtils.repeat(strichoben, octave - 3);
            } else if (octave <= 1) {
                return StringUtils.repeat(strichunten, 2 - octave);
            }
        }
        return "";
    }

}
