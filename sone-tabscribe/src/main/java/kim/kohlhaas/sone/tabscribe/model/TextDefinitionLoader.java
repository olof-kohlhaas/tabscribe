package kim.kohlhaas.sone.tabscribe.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kim.kohlhaas.sone.event.Updatable;
import kim.kohlhaas.sone.harmony.Temperament;
import kim.kohlhaas.sone.harmony.Tone;
import kim.kohlhaas.sone.harmony.GenericTone;
import kim.kohlhaas.sone.tabscribe.javafx.StatusPlotterProcess;

public class TextDefinitionLoader implements SessionLoader {
	
	final static Logger log = LoggerFactory.getLogger(TextDefinitionLoader.class);
	
	Temperament temperament;
	private File file = null;

	private enum Entity {
		TRACK, TABSCRIBE_VERSION, STRING, TAB, TEXT, AUDIO_FILE, BAR_LINE, IGNORED
	}
	
	public TextDefinitionLoader(Temperament temperament) {
		this.temperament = temperament;
	} 
	
	public Session load(Scanner scanner) throws UnsupportedEncodingException {
		Session session = null;
		String tmp = "";
		String currentStatement;
		String[] tmpSplit;
		Entity currentEntity;
		Track.Type currentTrackType = null;
		int currentTrackIndex = -1;
		String currentTrackName;
		int currentStringIndex = -1;
		File audioFile = null;
		int majorRelease = -1;
		int minorRelease = -1;
		int patchLevel = -1;
		Track currentTrack;
		GuitarString currentGuitarString;
		Guitar currentGuitar;
		Lyrics currentLyrics;
		TabStatement currentTabStatement;
		LyricsStatement currentLyricsStatement;
		Tone currentTone;
		BarLine currentBarLine;
		double currentMillisecond;
		double currentVelocity;
		double measureMilliOffset;
		double measureMilliDuration;
		int beatsPerMeasure;

		scanner.useLocale(Locale.US); // decimal point for double values
		while (scanner.hasNext()) {
			try {
				tmp = scanner.next().trim().toUpperCase();
				currentEntity = Entity.valueOf(tmp);
			} catch (IllegalArgumentException iae) {
				currentEntity = Entity.IGNORED;
			}
			
			log.debug("current entity: {}", currentEntity);
			
			switch (currentEntity) {
			case BAR_LINE:
				tmp = scanner.next().trim().toUpperCase();
				currentMillisecond = scanner.nextDouble();
				
				currentBarLine = new BarLine(currentMillisecond);
				currentBarLine.setType(BarLine.Type.valueOf(tmp));
				
				if (scanner.hasNextDouble()) {
					measureMilliOffset = scanner.nextDouble();
					measureMilliDuration = scanner.nextDouble();
					beatsPerMeasure = scanner.nextInt();
					
					if (currentMillisecond == 0.0) {
						session.getBarLines().getDefaultBarLine().setMeasureMilliOffset(measureMilliOffset);
						session.getBarLines().getDefaultBarLine().setMeasureMilliDuration(measureMilliDuration);
						session.getBarLines().getDefaultBarLine().setBeatsPerMeasure(beatsPerMeasure);
						log.debug("barline default: {}", currentBarLine);
					} else {
						currentBarLine.setMeasureMilliOffset(measureMilliOffset);
						currentBarLine.setMeasureMilliDuration(measureMilliDuration);
						currentBarLine.setBeatsPerMeasure(beatsPerMeasure);
					}
				}
				if (currentMillisecond > 0.0) {
					session.getBarLines().addBarLine(currentBarLine);
					log.debug("barline: {}", currentBarLine);
				}
				break;
			case TEXT:
				currentTrackIndex = scanner.nextInt();
				currentStatement = URLDecoder.decode(scanner.next(), "UTF-8");
				currentMillisecond = scanner.nextDouble();
				currentLyricsStatement = new LyricsStatement();
				currentLyricsStatement.setText(currentStatement);
				currentLyricsStatement.setMillisecond(currentMillisecond);
				
				currentLyrics = (Lyrics) session.getTrack(currentTrackIndex);
				currentLyrics.addStatement(currentLyricsStatement);
				
				log.debug("millisecond: {}, text: {}, track: {}", currentMillisecond, currentStatement, currentLyrics);
				break;
			case TAB:
				currentTrackIndex = scanner.nextInt();
				currentStringIndex = scanner.nextInt();
				currentStatement = URLDecoder.decode(scanner.next(), "UTF-8");
				currentMillisecond = scanner.nextDouble();
				if (scanner.hasNextDouble()) {
					currentVelocity = scanner.nextDouble();
				} else {
					currentVelocity = 1.0;
				}					
				
				currentGuitar = (Guitar) session.getTrack(currentTrackIndex);					
				currentTabStatement = new TabStatement(currentStatement, currentMillisecond);
				currentTabStatement.setVelocity(currentVelocity);
				currentGuitar.addTabStatement(currentStringIndex, currentTabStatement);
				log.debug("tabstatement: {}", currentTabStatement);
				break;
			case STRING:
				currentTrackIndex = scanner.nextInt();
				currentStringIndex = scanner.nextInt();
				tmpSplit = scanner.next().trim().split("\\-");
				// TODO convert FLAT to SHARP
				currentTone = new Tone(GenericTone.Name.valueOf(tmpSplit[0]), GenericTone.Semitone.valueOf(tmpSplit[1]), Integer.parseInt(tmpSplit[2]), temperament);
				log.debug("track index: {}, string index: {}, empty string tone: {}", currentTrackIndex, currentStringIndex, currentTone);
				currentGuitarString = new GuitarString();
				currentGuitarString.setEmptyString(currentTone);
				((Guitar) session.getTrack(currentTrackIndex)).addGuitarString(currentStringIndex, currentGuitarString);
				break;
			case TRACK:
				currentTrackIndex = scanner.nextInt();
				currentTrackType = Track.Type.valueOf(scanner.next().trim().toUpperCase());
				currentTrackName = URLDecoder.decode(scanner.next(), "UTF-8");
				log.debug("track index: {}, track type: {}, track name: {}", currentTrackIndex, currentTrackType, currentTrackName);
				if (currentTrackType == Track.Type.LYRICS) {
					currentTrack = new Lyrics();
					currentTrack.setName(currentTrackName);
				} else if (currentTrackType == Track.Type.GUITAR) {
					currentTrack = new Guitar(temperament);
					currentTrack.setName(currentTrackName);
				}
				else {
					currentTrack = null;
				}
				session.addTrack(currentTrackIndex, currentTrack);
				break;
			case AUDIO_FILE:
				tmp = URLDecoder.decode(scanner.next(), "UTF-8");
				
				if (tmp.equals("null")) {
					audioFile = null;
					log.debug("audio file: {}", "null");
				} else {
					audioFile = new File(tmp);
					log.debug("audio file absolute or relative to application: {}", audioFile.getAbsolutePath());
					if (!audioFile.exists()) {
						 audioFile = new File(this.file.getParent(), tmp);
						 log.debug("audio file relative to session file: {}", audioFile.getAbsolutePath());
					}
				}
				
				session = new Session();
				session.setAudioFileAsString(tmp);
				session.setAudioFile(audioFile);
				session.setSessionFile(file);
				break;				
			case TABSCRIBE_VERSION:
				tmpSplit = scanner.next().trim().split("\\.");
				majorRelease = Integer.parseInt(tmpSplit[0]);
				minorRelease = Integer.parseInt(tmpSplit[1]);
				patchLevel = Integer.parseInt(tmpSplit[2]);
				log.debug("major: {}, minor: {}, patch: {}", majorRelease, minorRelease, patchLevel);
				break;
			case IGNORED:
				log.debug("ignored: {}", tmp);
				break;
			}
		}

		
		return session;
	}
	
	@Override
	public Session load(InputStream inputStream) throws UnsupportedEncodingException {
		Scanner scanner = new Scanner(inputStream, "UTF-8");		
		this.file = null;
		return load(scanner);
	}
	
	@Override
	public Session load(File file) throws UnsupportedEncodingException, FileNotFoundException {

		Scanner scanner = new Scanner(file, "UTF-8");
		this.file = file;
		return load(scanner);
	}

	@Override
	public Updatable getProgress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProgress(Updatable progress) {
		// TODO Auto-generated method stub

	}

}
