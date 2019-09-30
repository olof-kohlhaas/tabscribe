package kim.kohlhaas.sone.harmony;

public class Key {
	
	private final GenericTone keynote;
	private final Mode mode;
	
	public Key(GenericTone keynote, Mode mode) {
		this.keynote = keynote;
		this.mode = mode;
	}

	public GenericTone getKeynote() {
		return keynote;
	}

	public Mode getMode() {
		return mode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((keynote == null) ? 0 : keynote.hashCode());
		result = prime * result + ((mode == null) ? 0 : mode.hashCode());
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
		Key other = (Key) obj;
		if (keynote == null) {
			if (other.keynote != null)
				return false;
		} else if (!keynote.equals(other.keynote))
			return false;
		if (mode != other.mode)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Key [keynote=" + keynote + ", mode=" + mode + "]";
	}
	
	
	
}
