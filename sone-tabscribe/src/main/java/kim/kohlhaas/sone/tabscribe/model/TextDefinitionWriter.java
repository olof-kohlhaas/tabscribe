package kim.kohlhaas.sone.tabscribe.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import kim.kohlhaas.sone.event.Updatable;

public class TextDefinitionWriter implements SessionWriter {

	@Override
	public void write(Session session, File file) throws IOException {
		Track currentTrack;
		
		Guitar currentGuitar;
		Lyrics currentLyrics;
		GuitarString currentString;
		TabStatement currentStatement;
		BarLine currentBarLine;
		Iterator<TabStatement> tabIterator;
		Set<Map.Entry<Double, LyricsStatement>> lyricsEntries;
		Iterator<BarLine> barLineIterator;
		PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(file, false)), true);
		printWriter.println("TABSCRIBE_VERSION 0.3.0"); // TODO synchronize version with gradle and git
		printWriter.println("AUDIO_FILE " + URLEncoder.encode(session.getAudioFile().getAbsolutePath(), "UTF-8"));
		for (int i = 0; i < session.getTrackCount(); i++) {
			currentTrack = session.getTrack(i);
			printWriter.println("TRACK " + i + " " + currentTrack.getType() + " " + URLEncoder.encode(session.getTrack(i).getName(), "UTF-8"));
			if (currentTrack.getType() == Track.Type.GUITAR) {
				currentGuitar = (Guitar) currentTrack;
				for (int s = 0; s < currentGuitar.getStringCount(); s++) {
					currentString = currentGuitar.getString(s);
					tabIterator = currentString.tabIterator();
					printWriter.println("STRING " + i + " " + s + " " 
							+ currentString.getEmptyString().getName()
							+ "-" + currentString.getEmptyString().getSemitone()
							+ "-" + currentString.getEmptyString().getOctave()
					);
					while (tabIterator.hasNext()) {
						currentStatement = tabIterator.next();
						printWriter.println("TAB " + i + " " + s + " " 
								+ URLEncoder.encode(currentStatement.getStatement(), "UTF-8")
								+ " " + currentStatement.getMillisecond()
								+ " " + currentStatement.getVelocity()
						);
					}
				}
			} else if (currentTrack.getType() == Track.Type.LYRICS) {
				currentLyrics = (Lyrics) currentTrack;
				lyricsEntries = currentLyrics.entrySet();
				for (Map.Entry<Double, LyricsStatement> lyricsEntry : lyricsEntries) {
					printWriter.println("TEXT " + i + " " 
							+ URLEncoder.encode(lyricsEntry.getValue().getText(), "UTF-8")
							+ " " + lyricsEntry.getKey()
					);
				}
			}
		}
		barLineIterator = session.getBarLines().barLineIterator();
		while (barLineIterator.hasNext()) {
			currentBarLine = barLineIterator.next();
			printWriter.println("BAR_LINE " + currentBarLine.getType() 
					+ " " + currentBarLine.getMillisecond()
					+ " " + currentBarLine.getMeasureMilliOffset()
					+ " " + currentBarLine.getMeasureMilliDuration()
					+ " " + currentBarLine.getBeatsPerMeasure()
			);
		}
		


		if (printWriter != null){ 
            printWriter.flush(); 
            printWriter.close(); 
        } 

		
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
