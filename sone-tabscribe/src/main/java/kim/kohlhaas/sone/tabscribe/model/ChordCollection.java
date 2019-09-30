package kim.kohlhaas.sone.tabscribe.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import kim.kohlhaas.sone.harmony.GenericTone;
import kim.kohlhaas.sone.harmony.Mode;
import kim.kohlhaas.sone.harmony.Tone;

//TODO make frets, tones and tuning more generic. this one is meant to be temporarily quick and dirty, for guitar standard tuning only
public class ChordCollection {
	
	private HashMap<GenericTone.Name, HashMap<GenericTone.Semitone, List<Chord>>> chordsByTone = new HashMap<>();
	private List<Chord> allChords = new LinkedList<Chord>(); 
	
	public ChordCollection(InputStream inputStream) {
		Scanner scanner = new Scanner(inputStream, "UTF-8");
		String chordId;
		String chart;
		GenericTone.Name tone;
		GenericTone.Semitone semitone;
		Mode mode;
		String interval;
		String[] chordIdDelimitted;
		Chord chord;
		HashMap<GenericTone.Semitone, List<Chord>> currentSemitoneChords;
		List<Chord> currentChordList;
		
		while (scanner.hasNext()) {
			chordId = scanner.next();
			chart = scanner.next();
						
			chordIdDelimitted = chordId.split("-");
			tone = GenericTone.Name.valueOf(chordIdDelimitted[0].toUpperCase());
			semitone = GenericTone.Semitone.valueOf(chordIdDelimitted[1].toUpperCase());
			mode = Mode.valueOf(chordIdDelimitted[2].toUpperCase());
			interval = chordIdDelimitted[3];
			chord = new Chord(tone, semitone, mode, interval, chart);
			
			
			currentSemitoneChords = chordsByTone.get(tone);
			if (currentSemitoneChords == null) {
				currentSemitoneChords = new HashMap<>();
				chordsByTone.put(tone, currentSemitoneChords);
			}
			
			currentChordList = currentSemitoneChords.get(semitone);
			if (currentChordList == null) {
				currentChordList = new ArrayList<>();
				currentSemitoneChords.put(semitone, currentChordList);
			} 

			currentChordList.add(chord);
			allChords.add(chord);
			
		}
		scanner.close();
	}
	
	public Set<GenericTone.Name> getToneNames() {
		return chordsByTone.keySet();
	}
	
	public Set<GenericTone.Semitone> getSemitones(GenericTone.Name toneName) {
		return chordsByTone.get(toneName).keySet();
	}
	
	public int getChordCount(GenericTone.Name toneName, GenericTone.Semitone semitone) {
		return chordsByTone.get(toneName).get(semitone).size();
	}
	
	public int getChordCount() {
		return allChords.size();
	}
	
	public Chord getChord(GenericTone.Name toneName, GenericTone.Semitone semitone, int index) {
		return chordsByTone.get(toneName).get(semitone).get(index);
	}
	
	public Chord getChord(int index) {
		return allChords.get(index);
	}
	
}
