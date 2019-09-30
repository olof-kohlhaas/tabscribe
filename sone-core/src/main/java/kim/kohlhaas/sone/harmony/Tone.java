package kim.kohlhaas.sone.harmony;

import java.util.Objects;

public class Tone implements Comparable<Tone> {
        
    private final int octave;
    private final Temperament temperament;
    private final GenericTone genericTone;

    
    public Tone(GenericTone.Name name, GenericTone.Semitone semitone, int octave, Temperament temperament) {    	
    	this.genericTone = new GenericTone(name, semitone);
        this.octave = octave;
        this.temperament = temperament;
    }

    public int getOctave() {
        return octave;
    }

    public Temperament getTemperament() {
        return temperament;
    }
    
    public double getFrequency() {
    	return temperament.getFrequency(this);
    }
    
    public GenericTone getGenericTone() {
    	return genericTone;
    }
    
    public GenericTone.Name getName() {
    	return this.genericTone.name;
    }
    
    public GenericTone.Semitone getSemitone() {
    	return genericTone.semitone;
    }

	@Override
    public boolean equals(Object o) { // TODO implement equal semitones with different names e.g. c-sharp/d-flat
        Tone tone = (Tone) o;
        return o != null && tone.getGenericTone().getName() == this.genericTone.name
        		&& tone.getGenericTone().getSemitone() == this.genericTone.semitone
                && tone.getOctave() == this.octave
                && tone.getTemperament().getClass().isInstance(this.temperament);
    }
    
    @Override
    public String toString() {
        return this.genericTone.name.toString() + this.genericTone.semitone.toString() + this.octave + this.temperament;
    }
    
    @Override
	public int hashCode() {
		return Objects.hash(this.genericTone.name, this.genericTone.semitone, this.octave, this.temperament.getClass());
	}
    
    @Override
	public int compareTo(Tone o) {
    	int result = this.octave - o.getOctave();
    	
    	if (result == 0) {
    		result = this.genericTone.compareTo(o.genericTone);
    	}
    	
    	// TODO unify with temperament order (e.g. frequency?)
    	
    	return result;
    }
}
