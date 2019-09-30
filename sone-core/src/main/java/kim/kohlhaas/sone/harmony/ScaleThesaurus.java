package kim.kohlhaas.sone.harmony;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;

public class ScaleThesaurus {
	
	private static final ScaleThesaurus INSTANCE = new ScaleThesaurus();
	
	private HashMap<GenericTone, Scale> unisonScaleMajor = new HashMap<>();
	private HashMap<GenericTone, Scale> secondScaleMajor = new HashMap<>(); 
	private HashMap<GenericTone, Scale> thirdScaleMajor = new HashMap<>();
	private HashMap<GenericTone, Scale> fourthScaleMajor = new HashMap<>();
	private HashMap<GenericTone, Scale> fifthScaleMajor = new HashMap<>();
	
	private HashMap<GenericTone, Scale> unisonScaleMinor = new HashMap<>();
	private HashMap<GenericTone, Scale> secondScaleMinor = new HashMap<>(); 
	private HashMap<GenericTone, Scale> thirdScaleMinor = new HashMap<>();
	private HashMap<GenericTone, Scale> fourthScaleMinor = new HashMap<>();
	private HashMap<GenericTone, Scale> fifthScaleMinor = new HashMap<>();
	
	public static ScaleThesaurus getInstance() {
		return INSTANCE;
	}
	
	ScaleThesaurus() {
		Scale cSharpMajor = new Scale(new Key(C_SHARP_MAJOR[0], Mode.MAJOR), C_SHARP_MAJOR);
		Scale gSharpMajor = new Scale(new Key(G_SHARP_MAJOR[0], Mode.MAJOR), G_SHARP_MAJOR);
		Scale dSharpMajor = new Scale(new Key(D_SHARP_MAJOR[0], Mode.MAJOR), D_SHARP_MAJOR);
		Scale aSharpMajor = new Scale(new Key(A_SHARP_MAJOR[0], Mode.MAJOR), A_SHARP_MAJOR);
		Scale fMajor = new Scale(new Key(F_MAJOR[0], Mode.MAJOR), F_MAJOR);
		Scale cMajor = new Scale(new Key(C_MAJOR[0], Mode.MAJOR), C_MAJOR);
		Scale gMajor = new Scale(new Key(G_MAJOR[0], Mode.MAJOR), G_MAJOR);
		Scale dMajor = new Scale(new Key(D_MAJOR[0], Mode.MAJOR), D_MAJOR);
		Scale aMajor = new Scale(new Key(A_MAJOR[0], Mode.MAJOR), A_MAJOR);
		Scale eMajor = new Scale(new Key(E_MAJOR[0], Mode.MAJOR), E_MAJOR);
		Scale bMajor = new Scale(new Key(B_MAJOR[0], Mode.MAJOR), B_MAJOR);
		Scale fSharpMajor = new Scale(new Key(F_SHARP_MAJOR[0], Mode.MAJOR), F_SHARP_MAJOR);
		
		Scale cSharpMinor = new Scale(new Key(C_SHARP_MINOR[0], Mode.MINOR), C_SHARP_MINOR);
		Scale gSharpMinor = new Scale(new Key(G_SHARP_MINOR[0], Mode.MINOR), G_SHARP_MINOR);
		Scale dSharpMinor = new Scale(new Key(D_SHARP_MINOR[0], Mode.MINOR), D_SHARP_MINOR);
		Scale aSharpMinor = new Scale(new Key(A_SHARP_MINOR[0], Mode.MINOR), A_SHARP_MINOR);
		Scale fMinor = new Scale(new Key(F_MINOR[0], Mode.MINOR), F_MINOR);
		Scale cMinor = new Scale(new Key(C_MINOR[0], Mode.MINOR), C_MINOR);
		Scale gMinor = new Scale(new Key(G_MINOR[0], Mode.MINOR), G_MINOR);
		Scale dMinor = new Scale(new Key(D_MINOR[0], Mode.MINOR), D_MINOR);
		Scale aMinor = new Scale(new Key(A_MINOR[0], Mode.MINOR), A_MINOR);
		Scale eMinor = new Scale(new Key(E_MINOR[0], Mode.MINOR), E_MINOR);
		Scale bMinor = new Scale(new Key(B_MINOR[0], Mode.MINOR), B_MINOR);
		Scale fSharpMinor = new Scale(new Key(F_SHARP_MINOR[0], Mode.MINOR), F_SHARP_MINOR);
		
		unisonScaleMajor.put(C_SHARP_MAJOR[0], cSharpMajor);
		unisonScaleMajor.put(G_SHARP_MAJOR[0], gSharpMajor);
		unisonScaleMajor.put(D_SHARP_MAJOR[0], dSharpMajor);
		unisonScaleMajor.put(A_SHARP_MAJOR[0], aSharpMajor);
		unisonScaleMajor.put(F_MAJOR[0], fMajor);
		unisonScaleMajor.put(C_MAJOR[0], cMajor);
		unisonScaleMajor.put(G_MAJOR[0], gMajor);
		unisonScaleMajor.put(D_MAJOR[0], dMajor);
		unisonScaleMajor.put(A_MAJOR[0], aMajor);
		unisonScaleMajor.put(E_MAJOR[0], eMajor);
		unisonScaleMajor.put(B_MAJOR[0], bMajor);
		unisonScaleMajor.put(F_SHARP_MAJOR[0], fSharpMajor);	
		
		unisonScaleMinor.put(C_SHARP_MINOR[0], cSharpMinor);
		unisonScaleMinor.put(G_SHARP_MINOR[0], gSharpMinor);
		unisonScaleMinor.put(D_SHARP_MINOR[0], dSharpMinor);
		unisonScaleMinor.put(A_SHARP_MINOR[0], aSharpMinor);
		unisonScaleMinor.put(F_MINOR[0], fMinor);
		unisonScaleMinor.put(C_MINOR[0], cMinor);
		unisonScaleMinor.put(G_MINOR[0], gMinor);
		unisonScaleMinor.put(D_MINOR[0], dMinor);
		unisonScaleMinor.put(A_MINOR[0], aMinor);
		unisonScaleMinor.put(E_MINOR[0], eMinor);
		unisonScaleMinor.put(B_MINOR[0], bMinor);
		unisonScaleMinor.put(F_SHARP_MINOR[0], fSharpMinor);
		
		secondScaleMajor.put(C_SHARP_MAJOR[1], cSharpMajor);
		secondScaleMajor.put(G_SHARP_MAJOR[1], gSharpMajor);
		secondScaleMajor.put(D_SHARP_MAJOR[1], dSharpMajor);
		secondScaleMajor.put(A_SHARP_MAJOR[1], aSharpMajor);
		secondScaleMajor.put(F_MAJOR[1], fMajor);
		secondScaleMajor.put(C_MAJOR[1], cMajor);
		secondScaleMajor.put(G_MAJOR[1], gMajor);
		secondScaleMajor.put(D_MAJOR[1], dMajor);
		secondScaleMajor.put(A_MAJOR[1], aMajor);
		secondScaleMajor.put(E_MAJOR[1], eMajor);
		secondScaleMajor.put(B_MAJOR[1], bMajor);
		secondScaleMajor.put(F_SHARP_MAJOR[1], fSharpMajor);	
		
		secondScaleMinor.put(C_SHARP_MINOR[1], cSharpMinor);
		secondScaleMinor.put(G_SHARP_MINOR[1], gSharpMinor);
		secondScaleMinor.put(D_SHARP_MINOR[1], dSharpMinor);
		secondScaleMinor.put(A_SHARP_MINOR[1], aSharpMinor);
		secondScaleMinor.put(F_MINOR[1], fMinor);
		secondScaleMinor.put(C_MINOR[1], cMinor);
		secondScaleMinor.put(G_MINOR[1], gMinor);
		secondScaleMinor.put(D_MINOR[1], dMinor);
		secondScaleMinor.put(A_MINOR[1], aMinor);
		secondScaleMinor.put(E_MINOR[1], eMinor);
		secondScaleMinor.put(B_MINOR[1], bMinor);
		secondScaleMinor.put(F_SHARP_MINOR[1], fSharpMinor);
		
		thirdScaleMajor.put(C_SHARP_MAJOR[2], cSharpMajor);
		thirdScaleMajor.put(G_SHARP_MAJOR[2], gSharpMajor);
		thirdScaleMajor.put(D_SHARP_MAJOR[2], dSharpMajor);
		thirdScaleMajor.put(A_SHARP_MAJOR[2], aSharpMajor);
		thirdScaleMajor.put(F_MAJOR[2], fMajor);
		thirdScaleMajor.put(C_MAJOR[2], cMajor);
		thirdScaleMajor.put(G_MAJOR[2], gMajor);
		thirdScaleMajor.put(D_MAJOR[2], dMajor);
		thirdScaleMajor.put(A_MAJOR[2], aMajor);
		thirdScaleMajor.put(E_MAJOR[2], eMajor);
		thirdScaleMajor.put(B_MAJOR[2], bMajor);
		thirdScaleMajor.put(F_SHARP_MAJOR[2], fSharpMajor);	
		
		thirdScaleMinor.put(C_SHARP_MINOR[2], cSharpMinor);
		thirdScaleMinor.put(G_SHARP_MINOR[2], gSharpMinor);
		thirdScaleMinor.put(D_SHARP_MINOR[2], dSharpMinor);
		thirdScaleMinor.put(A_SHARP_MINOR[2], aSharpMinor);
		thirdScaleMinor.put(F_MINOR[2], fMinor);
		thirdScaleMinor.put(C_MINOR[2], cMinor);
		thirdScaleMinor.put(G_MINOR[2], gMinor);
		thirdScaleMinor.put(D_MINOR[2], dMinor);
		thirdScaleMinor.put(A_MINOR[2], aMinor);
		thirdScaleMinor.put(E_MINOR[2], eMinor);
		thirdScaleMinor.put(B_MINOR[2], bMinor);
		thirdScaleMinor.put(F_SHARP_MINOR[2], fSharpMinor);
		
		fourthScaleMajor.put(C_SHARP_MAJOR[3], cSharpMajor);
		fourthScaleMajor.put(G_SHARP_MAJOR[3], gSharpMajor);
		fourthScaleMajor.put(D_SHARP_MAJOR[3], dSharpMajor);
		fourthScaleMajor.put(A_SHARP_MAJOR[3], aSharpMajor);
		fourthScaleMajor.put(F_MAJOR[3], fMajor);
		fourthScaleMajor.put(C_MAJOR[3], cMajor);
		fourthScaleMajor.put(G_MAJOR[3], gMajor);
		fourthScaleMajor.put(D_MAJOR[3], dMajor);
		fourthScaleMajor.put(A_MAJOR[3], aMajor);
		fourthScaleMajor.put(E_MAJOR[3], eMajor);
		fourthScaleMajor.put(B_MAJOR[3], bMajor);
		fourthScaleMajor.put(F_SHARP_MAJOR[3], fSharpMajor);	
		
		fourthScaleMinor.put(C_SHARP_MINOR[3], cSharpMinor);
		fourthScaleMinor.put(G_SHARP_MINOR[3], gSharpMinor);
		fourthScaleMinor.put(D_SHARP_MINOR[3], dSharpMinor);
		fourthScaleMinor.put(A_SHARP_MINOR[3], aSharpMinor);
		fourthScaleMinor.put(F_MINOR[3], fMinor);
		fourthScaleMinor.put(C_MINOR[3], cMinor);
		fourthScaleMinor.put(G_MINOR[3], gMinor);
		fourthScaleMinor.put(D_MINOR[3], dMinor);
		fourthScaleMinor.put(A_MINOR[3], aMinor);
		fourthScaleMinor.put(E_MINOR[3], eMinor);
		fourthScaleMinor.put(B_MINOR[3], bMinor);
		fourthScaleMinor.put(F_SHARP_MINOR[3], fSharpMinor);
		
		fifthScaleMajor.put(C_SHARP_MAJOR[4], cSharpMajor);
		fifthScaleMajor.put(G_SHARP_MAJOR[4], gSharpMajor);
		fifthScaleMajor.put(D_SHARP_MAJOR[4], dSharpMajor);
		fifthScaleMajor.put(A_SHARP_MAJOR[4], aSharpMajor);
		fifthScaleMajor.put(F_MAJOR[4], fMajor);
		fifthScaleMajor.put(C_MAJOR[4], cMajor);
		fifthScaleMajor.put(G_MAJOR[4], gMajor);
		fifthScaleMajor.put(D_MAJOR[4], dMajor);
		fifthScaleMajor.put(A_MAJOR[4], aMajor);
		fifthScaleMajor.put(E_MAJOR[4], eMajor);
		fifthScaleMajor.put(B_MAJOR[4], bMajor);
		fifthScaleMajor.put(F_SHARP_MAJOR[4], fSharpMajor);	
		
		fifthScaleMinor.put(C_SHARP_MINOR[4], cSharpMinor);
		fifthScaleMinor.put(G_SHARP_MINOR[4], gSharpMinor);
		fifthScaleMinor.put(D_SHARP_MINOR[4], dSharpMinor);
		fifthScaleMinor.put(A_SHARP_MINOR[4], aSharpMinor);
		fifthScaleMinor.put(F_MINOR[4], fMinor);
		fifthScaleMinor.put(C_MINOR[4], cMinor);
		fifthScaleMinor.put(G_MINOR[4], gMinor);
		fifthScaleMinor.put(D_MINOR[4], dMinor);
		fifthScaleMinor.put(A_MINOR[4], aMinor);
		fifthScaleMinor.put(E_MINOR[4], eMinor);
		fifthScaleMinor.put(B_MINOR[4], bMinor);
		fifthScaleMinor.put(F_SHARP_MINOR[4], fSharpMinor);
		
	}
	
	public Interval.Type getIntervalType(Interval.Name intervalName, GenericTone keynote, HashSet<Tone> tones) {
		Interval.Type result = null;
		
		final GenericTone majorIntervalTone = unisonScaleMajor.get(keynote).getGenericTone(intervalName.getScaleIndex());
		
		if (tones.stream().filter(t -> t.getGenericTone().equals(majorIntervalTone)).count() > 0) {
			result = Interval.Type.MAJOR;
		} else {
			if (intervalName != Interval.Name.SECOND && intervalName != Interval.Name.FOURTH && intervalName != Interval.Name.FIFTH && intervalName != Interval.Name.NINTH) {
				final GenericTone minorIntervalTone = unisonScaleMinor.get(keynote).getGenericTone(intervalName.getScaleIndex());
								
				if (tones.stream().filter(t -> t.getGenericTone().equals(minorIntervalTone)).count() > 0) {
					result = Interval.Type.MINOR;
				}
			}
		}
				
		return result;
	}
	
	public HashSet<Scale> getScalesByUnison(HashSet<Tone> unisons) {
		return getScalesByUnison(unisons, Mode.NONE);
	}
	
	public HashSet<Scale> getScalesBySecond(HashSet<Tone> seconds) {
		return getScalesBySecond(seconds, Mode.NONE);
	}
	
	public HashSet<Scale> getScalesByThird(HashSet<Tone> thirds) {
		return getScalesByThird(thirds, Mode.NONE);
	}
	
	public HashSet<Scale> getScalesByFourth(HashSet<Tone> fourths) {
		return getScalesByFourth(fourths, Mode.NONE);
	}
	
	public HashSet<Scale> getScalesByFifth(HashSet<Tone> fifths) {
		return getScalesByFifth(fifths, Mode.NONE);
	}
	
	public HashSet<Scale> getScalesByUnison(HashSet<Tone> unisons, Mode mode) {
		HashSet<Scale> result = new HashSet<>();
		Iterator<Tone> iterator = unisons.iterator();
		GenericTone currentTone;
				
		while(iterator.hasNext()) {
			currentTone = iterator.next().getGenericTone();
			if (mode == Mode.MAJOR || mode == Mode.NONE) {
				result.add(unisonScaleMajor.get(currentTone));
			}
			if (mode == Mode.MINOR || mode == Mode.NONE) {
				result.add(unisonScaleMinor.get(currentTone));
			}
		}
		
		return result;
	}
	
	public HashSet<Scale> getScalesBySecond(HashSet<Tone> seconds, Mode mode) {
		HashSet<Scale> result = new HashSet<>();
		Iterator<Tone> iterator = seconds.iterator();
		GenericTone currentTone;
				
		while(iterator.hasNext()) {
			currentTone = iterator.next().getGenericTone();
			if (mode == Mode.MAJOR || mode == Mode.NONE) {
				result.add(secondScaleMajor.get(currentTone));
			}
			if (mode == Mode.MINOR || mode == Mode.NONE) {
				result.add(secondScaleMinor.get(currentTone));
			}
		}
		
		return result;
	}
	
	public HashSet<Scale> getScalesByThird(HashSet<Tone> thirds, Mode mode) {
		HashSet<Scale> result = new HashSet<>();
		Iterator<Tone> iterator = thirds.iterator();
		GenericTone currentTone;
				
		while(iterator.hasNext()) {
			currentTone = iterator.next().getGenericTone();
			if (mode == Mode.MAJOR || mode == Mode.NONE) {
				result.add(thirdScaleMajor.get(currentTone));
			}
			if (mode == Mode.MINOR || mode == Mode.NONE) {
				result.add(thirdScaleMinor.get(currentTone));
			}
		}
		
		return result;
	}
	
	public HashSet<Scale> getScalesByFourth(HashSet<Tone> fourths, Mode mode) {
		HashSet<Scale> result = new HashSet<>();
		Iterator<Tone> iterator = fourths.iterator();
		GenericTone currentTone;
				
		while(iterator.hasNext()) {
			currentTone = iterator.next().getGenericTone();
			if (mode == Mode.MAJOR || mode == Mode.NONE) {
				result.add(fourthScaleMajor.get(currentTone));
			}
			if (mode == Mode.MINOR || mode == Mode.NONE) {
				result.add(fourthScaleMinor.get(currentTone));
			}
		}
		
		return result;
	}
	
	public HashSet<Scale> getScalesByFifth(HashSet<Tone> fifths, Mode mode) {
		HashSet<Scale> result = new HashSet<>();
		Iterator<Tone> iterator = fifths.iterator();
		GenericTone currentTone;
				
		while(iterator.hasNext()) {
			currentTone = iterator.next().getGenericTone();
			if (mode == Mode.MAJOR || mode == Mode.NONE) {
				result.add(fifthScaleMajor.get(currentTone));
			}
			if (mode == Mode.MINOR || mode == Mode.NONE) {
				result.add(fifthScaleMinor.get(currentTone));
			}
		}
		
		return result;
	}
		
	private final GenericTone[] C_SHARP_MAJOR = {
		new GenericTone(GenericTone.Name.C, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.D, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.F, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.F, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.G, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.A, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.C, GenericTone.Semitone.NONE)
    };
	
	private final GenericTone[] G_SHARP_MAJOR = {
		new GenericTone(GenericTone.Name.G, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.A, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.C, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.C, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.D, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.F, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.G, GenericTone.Semitone.NONE)
    };
	
	private final GenericTone[] D_SHARP_MAJOR = {
		new GenericTone(GenericTone.Name.D, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.F, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.G, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.G, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.A, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.C, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.D, GenericTone.Semitone.NONE)
    };
	
	private final GenericTone[] A_SHARP_MAJOR = {
		new GenericTone(GenericTone.Name.A, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.C, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.D, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.D, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.F, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.G, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.A, GenericTone.Semitone.NONE)
    };
	
	private final GenericTone[] F_MAJOR = {
		new GenericTone(GenericTone.Name.F, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.G, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.A, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.A, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.C, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.D, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.E, GenericTone.Semitone.NONE)
    };
	
	private final GenericTone[] C_MAJOR = {
		new GenericTone(GenericTone.Name.C, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.D, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.E, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.F, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.G, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.A, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.B, GenericTone.Semitone.NONE)
    };
	
	private final GenericTone[] G_MAJOR = {
		new GenericTone(GenericTone.Name.G, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.A, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.B, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.C, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.D, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.E, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.F, GenericTone.Semitone.SHARP)
    };
	
	private final GenericTone[] D_MAJOR = {
		new GenericTone(GenericTone.Name.D, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.E, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.F, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.G, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.A, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.B, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.C, GenericTone.Semitone.SHARP)
    };
	
	private final GenericTone[] A_MAJOR = {
		new GenericTone(GenericTone.Name.A, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.B, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.C, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.D, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.E, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.F, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.G, GenericTone.Semitone.SHARP)
    };
	
	private final GenericTone[] E_MAJOR = {
		new GenericTone(GenericTone.Name.E, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.F, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.G, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.A, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.B, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.C, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.D, GenericTone.Semitone.SHARP)
    };
	
	private final GenericTone[] B_MAJOR = {
		new GenericTone(GenericTone.Name.B, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.C, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.D, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.E, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.F, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.G, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.A, GenericTone.Semitone.SHARP)
    };
	
	private final GenericTone[] F_SHARP_MAJOR = {
		new GenericTone(GenericTone.Name.F, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.G, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.A, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.B, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.C, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.D, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.F, GenericTone.Semitone.NONE)
    };
	
	// -------------------------------------------------------------------
	
	private final GenericTone[] C_SHARP_MINOR = {
		new GenericTone(GenericTone.Name.C, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.D, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.E, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.F, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.G, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.A, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.B, GenericTone.Semitone.NONE)
    };
	
	private final GenericTone[] G_SHARP_MINOR = {
		new GenericTone(GenericTone.Name.G, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.A, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.B, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.C, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.D, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.E, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.F, GenericTone.Semitone.SHARP)
    };
	
	private final GenericTone[] D_SHARP_MINOR = {
		new GenericTone(GenericTone.Name.D, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.F, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.F, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.G, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.A, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.B, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.C, GenericTone.Semitone.SHARP)
    };
	
	private final GenericTone[] A_SHARP_MINOR = {
		new GenericTone(GenericTone.Name.A, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.C, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.C, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.D, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.F, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.F, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.G, GenericTone.Semitone.SHARP)
    };
	
	private final GenericTone[] F_MINOR = {
		new GenericTone(GenericTone.Name.F, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.G, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.G, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.A, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.C, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.C, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.D, GenericTone.Semitone.SHARP)
    };
	
	private final GenericTone[] C_MINOR = {
		new GenericTone(GenericTone.Name.C, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.D, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.D, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.F, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.G, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.G, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.A, GenericTone.Semitone.SHARP)
    };
	
	private final GenericTone[] G_MINOR = {
		new GenericTone(GenericTone.Name.G, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.A, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.A, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.C, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.D, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.D, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.F, GenericTone.Semitone.NONE)
    };
	
	private final GenericTone[] D_MINOR = {
		new GenericTone(GenericTone.Name.D, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.E, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.F, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.G, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.A, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.A, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.C, GenericTone.Semitone.NONE)
    };
	
	private final GenericTone[] A_MINOR = {
		new GenericTone(GenericTone.Name.A, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.B, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.C, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.D, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.E, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.F, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.G, GenericTone.Semitone.NONE)
    };
	
	private final GenericTone[] E_MINOR = {
		new GenericTone(GenericTone.Name.E, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.F, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.G, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.A, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.B, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.C, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.D, GenericTone.Semitone.NONE)
    };
	
	private final GenericTone[] B_MINOR = {
		new GenericTone(GenericTone.Name.B, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.C, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.D, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.E, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.F, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.G, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.A, GenericTone.Semitone.NONE)
    };
	
	private final GenericTone[] F_SHARP_MINOR = {
		new GenericTone(GenericTone.Name.F, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.G, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.A, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.B, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.C, GenericTone.Semitone.SHARP),
		new GenericTone(GenericTone.Name.D, GenericTone.Semitone.NONE),
		new GenericTone(GenericTone.Name.E, GenericTone.Semitone.NONE)
    };
	
}
