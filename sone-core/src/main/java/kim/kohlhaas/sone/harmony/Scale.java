package kim.kohlhaas.sone.harmony;

import java.util.ArrayList;
import java.util.Arrays;

public class Scale {
	
	private final Key key;
	private final ArrayList<GenericTone> tones;
	
	public Scale(Key key, GenericTone[] tones) {
		if (!key.getKeynote().equals(tones[0])) {
			throw new RuntimeException("Keynote does not match first tone.");
		}	
		
		this.key = key;
		this.tones = new ArrayList(tones.length);
		for (int i = 0; i < tones.length; i++) {
			this.tones.add(tones[i]);
		}
		
	}
	
	public Key getKey() {
		return key;
	}
	
	public int indexOf(Tone tone) {
		return tones.indexOf(tone.getGenericTone());
	}
	
	public GenericTone getGenericTone(int index) {
		return tones.get(index);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((tones == null) ? 0 : tones.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Scale other = (Scale) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (tones == null) {
			if (other.tones != null)
				return false;
		} else if (!tones.equals(other.tones))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Scale [key=" + key + ", tones=" + tones + "]";
	}
	
	
}
