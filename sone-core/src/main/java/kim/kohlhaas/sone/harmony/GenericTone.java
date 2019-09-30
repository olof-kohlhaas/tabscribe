package kim.kohlhaas.sone.harmony;

import java.util.ArrayList;
import java.util.Objects;


public class GenericTone implements Comparable<GenericTone> {
	
	public enum Name {
        C, D, E, F, G, A, B;
    }
    
    public enum Semitone {
        NONE, FLAT, SHARP; // TODO flat equals sharp
    }
	
    private static final ArrayList<GenericTone> sortOrder = new ArrayList<>();
    
    static {		
    	sortOrder.add(new GenericTone(Name.C, Semitone.NONE)); // TODO unify with temeperament order
    	sortOrder.add(new GenericTone(Name.C, Semitone.SHARP));
    	sortOrder.add(new GenericTone(Name.D, Semitone.NONE));
    	sortOrder.add(new GenericTone(Name.D, Semitone.SHARP));
    	sortOrder.add(new GenericTone(Name.E, Semitone.NONE));
    	sortOrder.add(new GenericTone(Name.F, Semitone.NONE));
    	sortOrder.add(new GenericTone(Name.F, Semitone.SHARP));
    	sortOrder.add(new GenericTone(Name.G, Semitone.NONE));
    	sortOrder.add(new GenericTone(Name.G, Semitone.SHARP));
    	sortOrder.add(new GenericTone(Name.A, Semitone.NONE));
    	sortOrder.add(new GenericTone(Name.A, Semitone.SHARP));
    	sortOrder.add(new GenericTone(Name.B, Semitone.NONE));
    }
    
	final GenericTone.Name name;
	final GenericTone.Semitone semitone;
	
	public GenericTone(GenericTone.Name name, GenericTone.Semitone semitone) {
		this.name = name;
		this.semitone = semitone;
	}
	
	
	
	@Override
	public boolean equals(Object other) {
		if (other == null) { 
			return false;
		}
		
		if (this == other) { 
			return true;
		}
		
		if (this.getClass() != other.getClass()) { 
			return false;
		}
		
		final GenericTone toneKey = (GenericTone) other;
		
		return this.name == toneKey.name && this.semitone == toneKey.semitone;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.name, this.semitone);
	}

	@Override
    public String toString() {
        return this.name.toString() + this.semitone.toString();
    }
	
	public GenericTone.Name getName() {
		return name;
	}

	public GenericTone.Semitone getSemitone() {
		return semitone;
	}

	@Override
	public int compareTo(GenericTone o) {
		return sortOrder.indexOf(this) - sortOrder.indexOf(o);
	}
	
	
			
}
