package kim.kohlhaas.sone.tabscribe.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Session {
	

	private File audioFile = null;
	private String audioFileAsString;
	private File sessionFile = null;
	private List<Track> tracks = new ArrayList<>();
	private BarLines barLines = new BarLines();
	private boolean changed = false;
	
	public Session() {
		
	}
	
	public void addTrack(int trackIndex, Track track) {
		tracks.add(trackIndex, track);
	}
	
	public Track getTrack(int index) {
		return tracks.get(index);
	}
	
	public int getTrackCount() {
		return tracks.size();
	}
	
	public File getAudioFile() {
		return audioFile;
	}

	public void setAudioFile(File audioFile) {
		this.audioFile = audioFile;
	}	

	public String getAudioFileAsString() {
		return audioFileAsString;
	}

	public void setAudioFileAsString(String audioFileAsString) {
		this.audioFileAsString = audioFileAsString;
	}

	public File getSessionFile() {
		return sessionFile;
	}

	public void setSessionFile(File sessionFile) {
		this.sessionFile = sessionFile;
	}
	
	public int getTrackIndex(Track track) {
		return tracks.indexOf(track);
	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public BarLines getBarLines() {
		return barLines;
	}
	
		
}
