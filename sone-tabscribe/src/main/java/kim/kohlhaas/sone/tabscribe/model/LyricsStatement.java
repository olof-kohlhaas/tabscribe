package kim.kohlhaas.sone.tabscribe.model;

public class LyricsStatement {
	
	private String text;
	private Track track;
	private Double millisecond;

	public LyricsStatement() {
		
	}
	
	public LyricsStatement(String text, double millisecond) {
		this.text = text;
		this.millisecond = millisecond;
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Double getMillisecond() {
		return millisecond;
	}

	public void setMillisecond(Double millisecond) {
		this.millisecond = millisecond;
	}

	public Track getTrack() {
		return track;
	}

	public void setTrack(Track track) {
		this.track = track;
	}	
		
	
}
