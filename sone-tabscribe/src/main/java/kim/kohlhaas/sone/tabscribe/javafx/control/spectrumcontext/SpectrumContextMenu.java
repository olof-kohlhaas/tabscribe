package kim.kohlhaas.sone.tabscribe.javafx.control.spectrumcontext;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectExpression;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import kim.kohlhaas.sone.harmony.GenericTone;
import kim.kohlhaas.sone.harmony.Mode;
import kim.kohlhaas.sone.harmony.Tone;
import kim.kohlhaas.sone.harmony.ToneFormatter;
import kim.kohlhaas.sone.tabscribe.model.Chord;
import kim.kohlhaas.sone.tabscribe.model.ChordCollection;
import kim.kohlhaas.sone.tabscribe.model.Guitar;
import kim.kohlhaas.sone.tabscribe.model.GuitarString;
import kim.kohlhaas.sone.tabscribe.model.Session;
import kim.kohlhaas.sone.tabscribe.model.TabStatement;
import kim.kohlhaas.sone.tabscribe.model.Track;
import kim.kohlhaas.util.StringUtil;

public class SpectrumContextMenu extends ContextMenu {
	
	private ResourceBundle labelBundle;
	private ToneFormatter.PitchNotation pitchNotation;
	private ChordCollection chords;
	private ObjectExpression<Tone> toneProperty;
	private ObjectExpression<LinkedHashSet<TabStatement>> beatStatements;
	private Session session = null;
	private LinkedList<GuitarStringTabToneBinding> tabBindings = new LinkedList<>();
	private HashMap<Guitar, HashMap<GenericTone.Name, HashMap<GenericTone.Semitone, Menu>>> chordMenusByTone = new HashMap<>();
	private double millisecondPosition;
	private double beatLeftMilli;
	private double beatRightMilli;
	
	public SpectrumContextMenu(ResourceBundle labelBundle, ToneFormatter.PitchNotation pitchNotation, 
			ChordCollection chords, 
			ObjectExpression<Tone> toneProperty, 
			ObjectExpression<LinkedHashSet<TabStatement>> beatStatements) {
		this.labelBundle = labelBundle;
		this.pitchNotation = pitchNotation;
		this.chords = chords;
		this.toneProperty = toneProperty;
		this.beatStatements = beatStatements;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {	
		Guitar guitarTrack;
		GuitarString guitarString;
    	Menu currentMenu;
    	Chord currentChord;
    	Menu currentChordMenu;
    	Menu currentToneSemitoneMenu;
    	SeparatorMenuItem currentSeparatorItem;
    	SpectrumContextBeatMenuItem currentBeatItem;
    	SpectrumContextMenuItem currentMenuItem;
    	SpectrumContextChordMenuItem currentChordMenuItem;
    	HashMap<GenericTone.Name, HashMap<GenericTone.Semitone, Menu>> currentToneSemitoneChordMenu;
    	HashMap<GenericTone.Semitone, Menu> currentSemitoneChordMenu;
		    	
		this.session = session;
		
		
		for (MenuItem item : this.getItems()) {
			item.textProperty().unbind();
			item.visibleProperty().unbind();
			item.setOnAction(null);
			if (item instanceof Menu) {
				clearAllItems((Menu) item);
			}
		}
				
		this.getItems().clear();
				
		for (GuitarStringTabToneBinding binding : tabBindings) {
			binding.unbind();
		}
		
		tabBindings.clear();
		
		chordMenusByTone = new HashMap<>();	

		for (int i = 0; i < session.getTrackCount(); i++) {
    		if (session.getTrack(i).getType() == Track.Type.GUITAR) {
    			guitarTrack = (Guitar) session.getTrack(i);
    			currentMenu = new Menu(StringUtil.replaceByLocale(guitarTrack.getName()));
    			if (!guitarTrack.getName().contains("bass")) { // TODO temporary solution as long as there is only one guitar and bass track each and name of tracks is not changeable
    				currentChordMenu = new Menu(labelBundle.getString("audio_chord"));
    				currentMenu.getItems().add(currentChordMenu);
    				for (int c = 0; c < chords.getChordCount(); c++) {
    					currentChord = chords.getChord(c);
    					currentChordMenuItem = new SpectrumContextChordMenuItem(currentChord, guitarTrack);
    					currentChordMenuItem.getStyleClass().add("chord-menu-item");
    					
    					currentToneSemitoneChordMenu = chordMenusByTone.get(guitarTrack);
    					if(currentToneSemitoneChordMenu == null) {
    						currentToneSemitoneChordMenu = new HashMap<>();
    						chordMenusByTone.put(guitarTrack, currentToneSemitoneChordMenu);
    					}
    					
    					currentSemitoneChordMenu = currentToneSemitoneChordMenu.get(currentChord.getTone());
    					if (currentSemitoneChordMenu == null) {
    						currentSemitoneChordMenu = new HashMap<>();
    						currentToneSemitoneChordMenu.put(currentChord.getTone(), currentSemitoneChordMenu);
    					}
    					
    					currentToneSemitoneMenu = currentSemitoneChordMenu.get(currentChord.getSemitone());
    					if (currentToneSemitoneMenu == null) {
    						currentToneSemitoneMenu = new Menu(ToneFormatter.simpleFormat(currentChord.getTone(), currentChord.getSemitone()));
    						currentChordMenu.getItems().add(currentToneSemitoneMenu);
    						currentSemitoneChordMenu.put(currentChord.getSemitone(), currentToneSemitoneMenu);
    					}
    		
    					currentToneSemitoneMenu.getItems().add(currentChordMenuItem);
    					currentChordMenuItem.setText(ToneFormatter.simpleFormat(chords.getChord(c).getTone(),chords.getChord(c).getSemitone())
    							+ (currentChord.getMode() == Mode.MINOR ? "m" : "")
    							+ (currentChord.getIntervalString().equals("none") ? "" : "" + currentChord.getIntervalString()));
    					currentToneSemitoneMenu.visibleProperty().bind(new ToneChordBinding(toneProperty, chords.getChord(c)));
    					currentChordMenuItem.setOnAction(e -> {
    						SpectrumContextChordMenuItem item = (SpectrumContextChordMenuItem) e.getTarget();
    						item.getParentMenu().getParentMenu().getParentPopup().getOwnerWindow().fireEvent(
        							new SpectrumContextEvent(
        									SpectrumContextEvent.CHORD_SELECTED, 
        									millisecondPosition,
        									item.getChord(),
        									item.getGuitar()));
    					});
    				}
    				
    			}
    			for (int l = guitarTrack.getStringCount() - 1; l >= 0; l--) {
    				guitarString = guitarTrack.getString(l);
    				currentMenuItem = new SpectrumContextMenuItem(guitarString);			
    				tabBindings.addLast(new GuitarStringTabToneBinding(toneProperty, guitarString));
    				currentMenuItem.visibleProperty().bind(tabBindings.getLast().greaterThanOrEqualTo(0).and(tabBindings.getLast().lessThanOrEqualTo(24)));
    				currentMenuItem.tabProperty().bind(tabBindings.getLast());
    				currentMenuItem.textProperty().bind(Bindings.concat(
    						labelBundle.getString("term_string")
    						+ " " + ToneFormatter.simpleFormat(pitchNotation, ToneFormatter.SemitoneCoincide.BOTH, guitarString.getGuitar().getTemperament(), guitarString.getEmptyString())
    						+ " " + labelBundle.getString("term_fret") + " ",
    						tabBindings.getLast()
    						));
    				currentMenuItem.setOnAction(e -> {
    					SpectrumContextMenuItem item = (SpectrumContextMenuItem) e.getTarget();
    					item.getParentPopup().getOwnerWindow().fireEvent(
    							new SpectrumContextEvent(
    									SpectrumContextEvent.FRET_SELECTED, 
    									millisecondPosition,
    									item.tabProperty().get(), 
    									item.getGuitarString()));
    				});
    				
    				currentMenu.getItems().add(currentMenuItem);
    			}
    			this.getItems().add(currentMenu);
    			
    			currentBeatItem = new SpectrumContextBeatMenuItem(labelBundle.getString("command_add_mute_tabs"), guitarTrack);
    			currentBeatItem.setOnAction(e -> {
    				SpectrumContextBeatMenuItem item = (SpectrumContextBeatMenuItem) e.getTarget();
    				item.getParentPopup().getOwnerWindow().fireEvent(
							new SpectrumContextEvent(
									SpectrumContextEvent.ADD_MUTE_TABS, 
									millisecondPosition,
									null,
									item.getGuitar()));
    			});
    			currentMenu.getItems().add(currentBeatItem);
    			
    			currentSeparatorItem = new SeparatorMenuItem();
    			currentSeparatorItem.visibleProperty().bind(Bindings.isNotNull(beatStatements));
    			currentMenu.getItems().add(currentSeparatorItem);
    			
    			currentBeatItem = new SpectrumContextBeatMenuItem(labelBundle.getString("command_delete_tabs"), guitarTrack);
    			currentBeatItem.visibleProperty().bind(Bindings.isNotNull(beatStatements));
    			currentBeatItem.setOnAction(e -> {
    				SpectrumContextBeatMenuItem item = (SpectrumContextBeatMenuItem) e.getTarget();
    				item.getParentPopup().getOwnerWindow().fireEvent(
							new SpectrumContextEvent(
									SpectrumContextEvent.BEAT_DELETED, 
									this.beatLeftMilli,
									this.beatRightMilli,
									this.beatStatements.getValue(),
									item.getGuitar()));
    			});
    			currentMenu.getItems().add(currentBeatItem);
    			
    			
    			currentBeatItem = new SpectrumContextBeatMenuItem(labelBundle.getString("command_arrange_as_upstroke"), guitarTrack);
    			currentBeatItem.visibleProperty().bind(Bindings.isNotNull(beatStatements));
    			currentBeatItem.setOnAction(e -> {
    				SpectrumContextBeatMenuItem item = (SpectrumContextBeatMenuItem) e.getTarget();
    				item.getParentPopup().getOwnerWindow().fireEvent(
							new SpectrumContextEvent(
									SpectrumContextEvent.BEAT_ARRANGE_UPSTROKE, 
									this.beatLeftMilli,
									this.beatRightMilli,
									this.beatStatements.getValue(),
									item.getGuitar()));
    			});
    			currentMenu.getItems().add(currentBeatItem);
    			
    			currentBeatItem = new SpectrumContextBeatMenuItem(labelBundle.getString("command_arrange_as_downstroke"), guitarTrack);
    			currentBeatItem.visibleProperty().bind(Bindings.isNotNull(beatStatements));
    			currentBeatItem.setOnAction(e -> {
    				SpectrumContextBeatMenuItem item = (SpectrumContextBeatMenuItem) e.getTarget();
    				item.getParentPopup().getOwnerWindow().fireEvent(
							new SpectrumContextEvent(
									SpectrumContextEvent.BEAT_ARRANGE_DOWNSTROKE, 
									this.beatLeftMilli,
									this.beatRightMilli,
									this.beatStatements.getValue(),
									item.getGuitar()));
    			});
    			currentMenu.getItems().add(currentBeatItem);
    			
    			currentBeatItem = new SpectrumContextBeatMenuItem(labelBundle.getString("command_arrange_straight"), guitarTrack);
    			currentBeatItem.visibleProperty().bind(Bindings.isNotNull(beatStatements));
    			currentBeatItem.setOnAction(e -> {
    				SpectrumContextBeatMenuItem item = (SpectrumContextBeatMenuItem) e.getTarget();
    				item.getParentPopup().getOwnerWindow().fireEvent(
							new SpectrumContextEvent(
									SpectrumContextEvent.BEAT_ARRANGE_STRAIGHT, 
									this.beatLeftMilli,
									this.beatRightMilli,
									this.beatStatements.getValue(),
									item.getGuitar()));
    			});
    			currentMenu.getItems().add(currentBeatItem);
    			
    			currentBeatItem = new SpectrumContextBeatMenuItem(labelBundle.getString("command_lower_tabs_semitone"), guitarTrack);
    			currentBeatItem.visibleProperty().bind(new TabsSemiDecreasableBinding(beatStatements));
    			currentBeatItem.setOnAction(e -> {
    				SpectrumContextBeatMenuItem item = (SpectrumContextBeatMenuItem) e.getTarget();
    				item.getParentPopup().getOwnerWindow().fireEvent(
							new SpectrumContextEvent(
									SpectrumContextEvent.BEAT_LOWER_SEMI, 
									this.beatLeftMilli,
									this.beatRightMilli,
									this.beatStatements.getValue(),
									item.getGuitar()));
    			});
    			currentMenu.getItems().add(currentBeatItem);
    			
    			currentBeatItem = new SpectrumContextBeatMenuItem(labelBundle.getString("command_raise_tabs_semitone"), guitarTrack);
    			currentBeatItem.visibleProperty().bind(new TabsSemiIncreasableBinding(beatStatements));
    			currentBeatItem.setOnAction(e -> {
    				SpectrumContextBeatMenuItem item = (SpectrumContextBeatMenuItem) e.getTarget();
    				item.getParentPopup().getOwnerWindow().fireEvent(
							new SpectrumContextEvent(
									SpectrumContextEvent.BEAT_RAISE_SEMI, 
									this.beatLeftMilli,
									this.beatRightMilli,
									this.beatStatements.getValue(),
									item.getGuitar()));
    			});
    			currentMenu.getItems().add(currentBeatItem);
    			
    			currentBeatItem = new SpectrumContextBeatMenuItem(labelBundle.getString("command_lower_tabs_octave"), guitarTrack);
    			currentBeatItem.visibleProperty().bind(new TabsOctaveDecreasableBinding(beatStatements));
    			currentBeatItem.setOnAction(e -> {
    				SpectrumContextBeatMenuItem item = (SpectrumContextBeatMenuItem) e.getTarget();
    				item.getParentPopup().getOwnerWindow().fireEvent(
							new SpectrumContextEvent(
									SpectrumContextEvent.BEAT_LOWER_OCTAVE, 
									this.beatLeftMilli,
									this.beatRightMilli,
									this.beatStatements.getValue(),
									item.getGuitar()));
    			});
    			currentMenu.getItems().add(currentBeatItem);
    			
    			currentBeatItem = new SpectrumContextBeatMenuItem(labelBundle.getString("command_raise_tabs_octave"), guitarTrack);
    			currentBeatItem.visibleProperty().bind(new TabsOctaveIncreasableBinding(beatStatements));
    			currentBeatItem.setOnAction(e -> {
    				SpectrumContextBeatMenuItem item = (SpectrumContextBeatMenuItem) e.getTarget();
    				item.getParentPopup().getOwnerWindow().fireEvent(
							new SpectrumContextEvent(
									SpectrumContextEvent.BEAT_RAISE_OCTAVE, 
									this.beatLeftMilli,
									this.beatRightMilli,
									this.beatStatements.getValue(),
									item.getGuitar()));
    			});
    			currentMenu.getItems().add(currentBeatItem);

    			
    		}
    	}
	}
	
	private void clearAllItems(Menu menu) {
		for (MenuItem item : menu.getItems()) {
			item.textProperty().unbind();
			item.visibleProperty().unbind();
			if (item instanceof Menu) {
				clearAllItems((Menu) item);
			}
		}
		menu.getItems().clear();

	}

	public double getMillisecondPosition() {
		return millisecondPosition;
	}

	public void setMillisecondPosition(double millisecondPosition) {
		this.millisecondPosition = millisecondPosition;
	}

	public double getBeatLeftMilli() {
		return beatLeftMilli;
	}

	public void setBeatLeftMilli(double beatLeftMilli) {
		this.beatLeftMilli = beatLeftMilli;
	}

	public double getBeatRightMilli() {
		return beatRightMilli;
	}

	public void setBeatRightMilli(double beatRightMilli) {
		this.beatRightMilli = beatRightMilli;
	}
	
	

}
