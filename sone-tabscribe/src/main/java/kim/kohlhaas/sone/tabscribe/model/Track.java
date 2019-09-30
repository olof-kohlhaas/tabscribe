package kim.kohlhaas.sone.tabscribe.model;

public interface Track {

	public static enum Type{
		GUITAR, LYRICS
	}
	
	String getName();
	Type getType();
	void setName(String name);
	
}