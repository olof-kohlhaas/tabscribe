package kim.kohlhaas.sone.harmony;

public class Interval {
	
	public enum Type {
		MAJOR, MINOR, AUGMENTED, DIMINISHED, PERFECT
	}
	
	public enum Name {
		UNISON(0), SECOND(1), THIRD(2), FOURTH(3), FIFTH(4), SEVENTH(6), NINTH(1);
		
		private final int scaleIndex;
		
		private Name(final int scaleIndex) {
			this.scaleIndex = scaleIndex;
		}
		
		public int getScaleIndex() {
			return scaleIndex;
		}
	}
	
	private Name name;
	private Type type;
		
	public Interval(Name name, Type type) {
		this.name = name;
		this.type = type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		Interval other = (Interval) obj;
		if (name != other.name)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Interval [name=" + name + ", type=" + type + "]";
	}	
	
}
